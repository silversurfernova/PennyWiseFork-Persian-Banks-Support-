package com.pennywiseai.parser.core.bank

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
}
