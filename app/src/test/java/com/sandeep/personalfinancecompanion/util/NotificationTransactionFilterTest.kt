package com.sandeep.personalfinancecompanion.util

import android.os.Bundle
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

    @Test
    fun shouldRejectWhatsappSalaryDiscussion() {
        assertFalse(
            NotificationTransactionFilter.shouldProcess(
                packageName = "com.whatsapp",
                title = "HR",
                text = "Let's discuss salary Rs 50000 tomorrow"
            )
        )
    }

    @Test
    fun shouldRejectOwnAppNotification() {
        assertFalse(
            NotificationTransactionFilter.shouldProcess(
                packageName = "com.sandeep.personalfinancecompanion",
                title = "Transaction Auto-Saved",
                text = "Automatically saved: Rs 250 EXPENSE at Detected: Rs 250"
            )
        )
    }

    @Test
    fun shouldExtractGoogleMessagesBodyFromExtras() {
        val extras = Bundle().apply {
            putCharSequence("android.title", "1 new message")
            putCharSequence("android.text", null)
            putCharSequenceArray(
                "android.textLines",
                arrayOf<CharSequence>("AXIS Mutual Fund", "Confirmation of your SIP transaction")
            )
            putParcelableArray(
                "android.messages",
                arrayOf<android.os.Parcelable>(
                    Bundle().apply {
                        putCharSequence("text", "Rs 35000 credited via UPI txn 12345")
                    }
                )
            )
        }

        val snapshot = NotificationTextExtractor.fromExtras(extras)
        val text = NotificationTextExtractor.fromSnapshot(snapshot)

        assertTrue(text.contains("Rs 35000 credited via UPI txn 12345"))
    }

    @Test
    fun shouldAcceptGoogleMessagesTransactionFromMessageBody() {
        val extras = Bundle().apply {
            putCharSequence("android.title", "Punjab National Bank")
            putCharSequence("android.text", "1 new message")
            putParcelableArray(
                "android.messages",
                arrayOf<android.os.Parcelable>(
                    Bundle().apply {
                        putCharSequence(
                            "text",
                            "A/c XX9770 debited INR 55.00 thru UPI 606155288039 Bal INR 31279.42"
                        )
                    }
                )
            )
        }

        val snapshot = NotificationTextExtractor.fromExtras(extras)
        val text = NotificationTextExtractor.fromSnapshot(snapshot.copy(title = ""))

        assertTrue(
            NotificationTransactionFilter.shouldProcess(
                packageName = "com.google.android.apps.messaging",
                title = snapshot.title,
                text = text
            )
        )
    }

    @Test
    fun shouldKeepSummaryOnlyGoogleMessagesRejected() {
        val extras = Bundle().apply {
            putCharSequence("android.title", "1 new message")
            putCharSequence("android.text", null)
            putCharSequenceArray("android.textLines", arrayOf<CharSequence>("New message"))
        }

        val snapshot = NotificationTextExtractor.fromExtras(extras)
        val text = NotificationTextExtractor.fromSnapshot(snapshot.copy(title = ""))

        assertFalse(
            NotificationTransactionFilter.shouldProcess(
                packageName = "com.google.android.apps.messaging",
                title = snapshot.title,
                text = text
            )
        )
    }
}
