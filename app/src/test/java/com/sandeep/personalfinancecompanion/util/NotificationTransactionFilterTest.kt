package com.sandeep.personalfinancecompanion.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationTransactionFilterTest {
    @Test
    fun shouldRejectPromotionalPaymentAppNotification() {
        assertFalse(
            NotificationTransactionFilter.shouldProcess(
                packageName = "net.one97.paytm",
                title = "Special offer",
                text = "Get flat Rs 100 cashback on recharge. Limited time deal."
            )
        )
    }

    @Test
    fun shouldAcceptBankTransactionNotification() {
        assertTrue(
            NotificationTransactionFilter.shouldProcess(
                packageName = "com.phonepe.app",
                title = "Payment successful",
                text = "Rs 250 paid to Rahul via UPI txn 12345 from account XX1234"
            )
        )
    }

    @Test
    fun shouldRejectAmountOnlyNotification() {
        assertFalse(
            NotificationTransactionFilter.shouldProcess(
                packageName = "com.google.android.apps.nbu.paisa.user",
                title = "Rewards",
                text = "You have rewards worth Rs 500 waiting for you"
            )
        )
    }
}
