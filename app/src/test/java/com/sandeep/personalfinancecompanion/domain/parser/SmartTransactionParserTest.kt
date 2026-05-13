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
}
