package com.pennywiseai.parser.core.bank

import com.pennywiseai.parser.core.TransactionType
import com.pennywiseai.parser.core.test.ExpectedTransaction
import com.pennywiseai.parser.core.test.ParserTestCase
import com.pennywiseai.parser.core.test.ParserTestUtils
import com.pennywiseai.parser.core.test.SimpleTestCase
import org.junit.jupiter.api.DynamicTest
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
            ParserTestCase(
                name = "Tejarat Bank instant transfer (انتقال به شتابي)",
                message = "بانک تجارت\nکارت: 1111****222233\nنوع تراکنش: انتقال به شتابي\nمبلغ: 1,000,000 ريال\nمانده: 2,104,509 ريال\n1405/04/13 - 18:45",
                sender = "TEJARATBANK",
                expected = ExpectedTransaction(
                    amount = BigDecimal("1000000"),
                    currency = "IRR",
                    type = TransactionType.EXPENSE,
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
}
