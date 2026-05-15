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
    fun shouldExtractGoogleMessagesBodyFromSnapshot() {
        val snapshot = NotificationTextSnapshot(
            title = "1 new message",
            text = "",
            textLines = listOf("AXIS Mutual Fund", "Confirmation of your SIP transaction"),
            messageTexts = listOf("Rs 35000 credited via UPI txn 12345")
        )

        val candidateTexts = NotificationTextExtractor.candidateTextsFromSnapshot(snapshot)

        assertTrue(candidateTexts.contains("Rs 35000 credited via UPI txn 12345"))
        assertTrue(
            NotificationTransactionFilter.shouldProcess(
                packageName = "com.google.android.apps.messaging",
                title = snapshot.title,
                text = snapshot.text,
                extraTexts = candidateTexts
            )
        )
    }

    @Test
    fun shouldAcceptGoogleMessagesTransactionFromMessageBody() {
        val snapshot = NotificationTextSnapshot(
            title = "Punjab National Bank",
            text = "1 new message",
            messageTexts = listOf(
                "A/c XX9770 debited INR 55.00 thru UPI 606155288039 Bal INR 31279.42"
            )
        )

        val text = NotificationTextExtractor.fromSnapshot(snapshot.copy(title = ""))
        val candidateTexts = NotificationTextExtractor.candidateTextsFromSnapshot(snapshot)

        assertTrue(
            NotificationTransactionFilter.shouldProcess(
                packageName = "com.google.android.apps.messaging",
                title = snapshot.title,
                text = text,
                extraTexts = candidateTexts
            )
        )
    }

    @Test
    fun shouldAcceptGroupedGoogleMessagesTransactionBodies() {
        val snapshot = NotificationTextSnapshot(
            title = "Punjab National Bank",
            text = "3 messages",
            messageTexts = listOf(
                "A/c XX9770 debited INR 21.00 Dt 15-05-26 00:57:56 thru UPI: 613507000211.Bal INR 31253.42",
                "A/c XX9770 debited INR 70.00 Dt 15-05-26 13:24:27 thru UPI: 206191080871.Bal INR 31143.42",
                "A/c XX9770 debited INR 30.00 Dt 15-05-26 13:35:56 thru UPI: 606183867231.Bal INR 3113.42"
            )
        )

        val text = NotificationTextExtractor.fromSnapshot(snapshot.copy(title = ""))
        val candidateTexts = NotificationTextExtractor.candidateTextsFromSnapshot(snapshot)

        assertTrue(
            NotificationTransactionFilter.shouldProcess(
                packageName = "com.google.android.apps.messaging",
                title = snapshot.title,
                text = text,
                extraTexts = candidateTexts
            )
        )
    }

    @Test
    fun shouldAcceptUnicodeStyledPnbDebitMessage() {
        assertTrue(
            NotificationTransactionFilter.shouldProcess(
                packageName = "com.google.android.apps.messaging",
                title = "Punjab National Bank",
                text = "𝖠/𝖼 XX9770 𝖽𝖾𝖻𝗂𝗍𝖾𝖽 INR 16.00 𝖣𝗍 15-05-26 19:54:16 𝗍𝗁𝗋𝗎 𝖴𝖯𝖨: 306209968584.Bal INR 31097.42"
            )
        )
    }

    @Test
    fun shouldAcceptCompletedPaymentNotification() {
        assertTrue(
            NotificationTransactionFilter.shouldProcess(
                packageName = "net.one97.paytm",
                title = "Automatic Payment for Paytm Money Limited",
                text = "Payment of Rs.21 has been successfully completed from your Punjab National Bank - 9770 towards Paytm Money Limited"
            )
        )
    }
}
