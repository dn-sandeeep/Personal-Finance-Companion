package com.sandeep.personalfinancecompanion.domain.parser

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SmartTransactionParserTest {

    private val parser = SmartTransactionParser()

    @Test
    fun anchoredAmountExtraction_ignoresAccountLast4Digits() {
        val message = "Acct XX1234 debited by Rs. 560.00 via UPI. Avl bal INR 8,912.45"

        val amount = parser.extractPrimaryFinancialAmountForTesting(message)

        assertEquals(560.0, amount ?: -1.0, 0.0)
    }

    @Test
    fun financialMessageDetection_marksBankNotificationAsFinancial() {
        val message = "Your a/c XX4321 has been credited with INR 2,500.00 on 13-05-2026"

        assertTrue(parser.isLikelyFinancialMessageForTesting(message))
    }

    @Test
    fun primaryFinancialAmount_ignoresAvailableBalance() {
        val message = "Your a/c **4321 credited with INR 2,500.00. Avl bal INR 8,912.45"

        val amount = parser.extractPrimaryFinancialAmountForTesting(message)

        assertEquals(2500.0, amount ?: -1.0, 0.0)
    }

    @Test
    fun primaryFinancialAmount_returnsNullWhenOnlyAccountDigitsExist() {
        val message = "A/c XX4321 txn alert. Account ending 4321 updated."

        val amount = parser.extractPrimaryFinancialAmountForTesting(message)

        assertNull(amount)
    }

    @Test
    fun anchoredAmountExtraction_extractsMultipleTransactionsFromGroupedMessage() {
        val message = """
            A/c XX9770 debited INR 21.00 Dt 15-05-26 00:57:56 thru UPI: 613507000211.Bal INR 31253.42.
            A/c XX9770 debited INR 70.00 Dt 15-05-26 13:24:27 thru UPI: 206191080871.Bal INR 31143.42.
            A/c XX9770 debited INR 30.00 Dt 15-05-26 13:35:56 thru UPI: 606183867231.Bal INR 3113.42.
        """.trimIndent()

        val amounts = parser.extractAnchoredAmountValuesForTesting(message)

        assertEquals(
            listOf(21.0, 31253.42, 70.0, 31143.42, 30.0, 3113.42),
            amounts
        )
    }
}
