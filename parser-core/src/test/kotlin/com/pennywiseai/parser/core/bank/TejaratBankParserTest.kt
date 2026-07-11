package com.pennywiseai.parser.core.bank

import com.pennywiseai.parser.core.TransactionType
import com.pennywiseai.parser.core.test.ExpectedTransaction
import com.pennywiseai.parser.core.test.ParserTestCase
import com.pennywiseai.parser.core.test.ParserTestUtils
import com.pennywiseai.parser.core.test.SimpleTestCase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import java.math.BigDecimal

class TejaratBankParserTest {

    private val parser = TejaratBankParser()

    @TestFactory
    fun `tejarat bank parser handles key paths`(): List<DynamicTest> {
        val cases = listOf(
            // Real Tejarat messages mix Arabic-script (ي) and Persian-script (ی) Yeh
            // in the same text — "ريال" and "خريد شتابي" below use the Arabic form
            // exactly as received, to exercise the normalizePersian() fix.
            ParserTestCase(
                name = "Tejarat Bank bill payment (پرداخت قبض)",
                message = "بانک تجارت\nکارت: 1111****222233\nنوع تراکنش: پرداخت قبض\nمبلغ: 55,000 ريال\nمانده: 233,509 ريال\n1405/04/12 - 20:32",
                sender = "TEJARATBANK",
                expected = ExpectedTransaction(
                    amount = BigDecimal("55000"),
                    currency = "IRR",
                    type = TransactionType.EXPENSE,
                    balance = BigDecimal("233509"),
                    accountLast4 = "2233",
                    isFromCard = false
                )
            ),
            ParserTestCase(
                name = "Tejarat Bank deposit (واریز)",
                message = "بانک تجارت\nکارت: 1111****222233\nنوع تراکنش: واریز\nمبلغ: 1,000,000 ريال\nمانده: 1,233,509 ريال\n1405/04/13 - 11:01",
                sender = "TEJARATBANK",
                expected = ExpectedTransaction(
                    amount = BigDecimal("1000000"),
                    currency = "IRR",
                    type = TransactionType.INCOME,
                    balance = BigDecimal("1233509"),
                    accountLast4 = "2233",
                    isFromCard = false
                )
            ),
            ParserTestCase(
                name = "Tejarat Bank instant purchase (خريد شتابي, Arabic Yeh)",
                message = "بانک تجارت\nکارت: 1111****222233\nنوع تراکنش: خريد شتابي\nمبلغ: 129,000 ريال\nمانده: 104,509 ريال\n1405/04/13 - 12:23",
                sender = "TEJARATBANK",
                expected = ExpectedTransaction(
                    amount = BigDecimal("129000"),
                    currency = "IRR",
                    type = TransactionType.EXPENSE,
                    balance = BigDecimal("104509"),
                    accountLast4 = "2233",
                    isFromCard = false
                )
            ),
            // "انتقال به شتابي" reads like an outgoing transfer, but tracing the
            // running balance across a real sequence of statement SMS shows it
            // consistently INCREASES the balance by the transaction amount
            // (unlike "خريد شتابي", which always decreases it) — it's an
            // incoming Shetab transfer, not an outgoing one.
            ParserTestCase(
                name = "Tejarat Bank incoming Shetab transfer (انتقال به شتابي)",
                message = "بانک تجارت\nکارت: 1111****222233\nنوع تراکنش: انتقال به شتابي\nمبلغ: 1,000,000 ريال\nمانده: 2,104,509 ريال\n1405/04/13 - 18:45",
                sender = "TEJARATBANK",
                expected = ExpectedTransaction(
                    amount = BigDecimal("1000000"),
                    currency = "IRR",
                    type = TransactionType.INCOME,
                    balance = BigDecimal("2104509"),
                    accountLast4 = "2233",
                    isFromCard = false
                )
            ),
            // Pre-authorization / dynamic-password SMS: carries an amount but is NOT a
            // completed transaction — must never be parsed (رمز exclusion).
            ParserTestCase(
                name = "Tejarat Bank charge authorization code (not a transaction)",
                message = "شارژ \n111*2222 \nمبلغ 50,000 \nرمز 11112222 \nتاریخ 1405/04/12 20:32:28",
                sender = "TEJARATBANK",
                shouldParse = false
            ),
            ParserTestCase(
                name = "Tejarat Bank purchase authorization code (not a transaction)",
                message = "خرید \nهمراه کسب و کارهای ه \nمبلغ 200,000 \nرمز 65777124 \nتاریخ 1405/04/16 13:55:09",
                sender = "TEJARATBANK",
                shouldParse = false
            )
        )

        val handleCases = listOf(
            Pair("TEJARATBANK", true),
            Pair("TEJARAT BANK", true),
            Pair("TEJARAT", true),
            Pair("OTHER", false)
        )

        return ParserTestUtils.runTestSuite(parser, cases, handleCases)
    }

    @TestFactory
    fun `factory resolves tejarat bank`(): List<DynamicTest> {
        val cases = listOf(
            SimpleTestCase(
                bankName = "Tejarat Bank",
                sender = "TEJARATBANK",
                currency = "IRR",
                message = "بانک تجارت\nکارت: 1111****222233\nنوع تراکنش: پرداخت قبض\nمبلغ: 55,000 ريال\nمانده: 233,509 ريال\n1405/04/12 - 20:32",
                expected = ExpectedTransaction(
                    amount = BigDecimal("55000"),
                    currency = "IRR",
                    type = TransactionType.EXPENSE
                ),
                shouldHandle = true
            )
        )

        return ParserTestUtils.runFactoryTestSuite(cases, "Tejarat Bank factory tests")
    }

    // An unmapped "نوع تراکنش:" value (anything besides the four known ones)
    // must never be guessed at — it should surface for manual/rule-based
    // classification instead, via extractRawTypeLabel + isPendingClassification.
    private val unknownTypeMessage =
        "بانک تجارت\nکارت: 1111****222233\nنوع تراکنش: استرداد وجه\nمبلغ: 15,000 ريال\nمانده: 218,509 ريال\n1405/04/12 - 20:40"

    @Test
    fun `unmapped transaction type does not parse and is not guessed at`() {
        assertNull(parser.parse(unknownTypeMessage, "TEJARATBANK", 0L))
    }

    @Test
    fun `unmapped transaction type exposes its raw label for manual classification`() {
        assertEquals("استرداد وجه", parser.extractRawTypeLabel(unknownTypeMessage))
        assertTrue(parser.isPendingClassification(unknownTypeMessage))
    }

    @Test
    fun `parseWithResolvedType resolves an otherwise-unmapped transaction type`() {
        val resolved = parser.parseWithResolvedType(
            unknownTypeMessage,
            "TEJARATBANK",
            0L,
            resolvedType = TransactionType.EXPENSE
        )

        assertEquals(TransactionType.EXPENSE, resolved?.type)
        assertEquals(BigDecimal("15000"), resolved?.amount)
    }

    @Test
    fun `known transaction types are never flagged as pending`() {
        assertTrue(
            !parser.isPendingClassification(
                "بانک تجارت\nکارت: 1111****222233\nنوع تراکنش: واریز\nمبلغ: 1,000,000 ريال\nمانده: 1,233,509 ريال\n1405/04/13 - 11:01"
            )
        )
    }
}
