package com.pennywiseai.parser.core.bank

import com.pennywiseai.parser.core.TransactionType

/**
 * Tejarat Bank parser for Iranian banking SMS messages.
 * Real messages state the transaction type explicitly via "نوع تراکنش:" (rather
 * than the sign-suffixed compact format Parsian uses), and mask the card number
 * with "****", e.g. "کارت: 7034****585983".
 */
class TejaratBankParser : BaseIranianBankParser() {

    override fun getBankName() = "Tejarat Bank"

    override fun canHandle(sender: String): Boolean {
        val upperSender = sender.uppercase()

        val tejaratSenders = setOf(
            "TEJARATBANK",
            "TEJARAT BANK",
            "BANK TEJARAT",
            "TEJARAT"
        )

        return upperSender in tejaratSenders
    }

    /**
     * Messages mention "کارت" but never a separate account number — the masked
     * card digits ARE the only account identifier here. Treating this as a card
     * (the base-class default) routes it through card-linking, which requires a
     * manual step before an account/history shows up. Reporting it as a plain
     * account transaction instead lets it auto-create the account immediately,
     * same as Parsian.
     */
    override fun detectIsCard(message: String): Boolean = false

    private val typeFieldPattern = Regex("""نوع تراکنش\s*:?\s*([^\n]+)""")

    private fun rawTypeField(message: String): String? =
        typeFieldPattern.find(normalizePersian(message))?.groupValues?.get(1)?.trim()

    /**
     * The base class's generic "انتقال" => EXPENSE rule is wrong for Tejarat:
     * across a real sequence of statement SMS, "انتقال به شتابي" consistently
     * *increased* the running balance by the transaction amount (an incoming
     * Shetab transfer), unlike every "خريد شتابي" (purchase), which decreased
     * it. Read the explicit "نوع تراکنش:" field instead of guessing from
     * whole-message keywords.
     *
     * An unrecognized field value returns null rather than falling back to the
     * generic guesser — exactly the mistake that got "انتقال به شتابي" wrong
     * in the first place. [extractRawTypeLabel] exposes the value instead, so
     * a caller-supplied rule (or manual classification) can resolve it.
     */
    override fun extractTransactionType(message: String): TransactionType? {
        val txnType = rawTypeField(message) ?: return super.extractTransactionType(message)

        return when {
            txnType.contains("واریز") -> TransactionType.INCOME
            txnType.contains("انتقال به شتابی") -> TransactionType.INCOME
            txnType.contains("خرید") -> TransactionType.EXPENSE
            txnType.contains("پرداخت") -> TransactionType.EXPENSE
            else -> null
        }
    }

    override fun extractRawTypeLabel(message: String): String? = rawTypeField(message)
}
