package com.sandeep.personalfinancecompanion.util

import com.sandeep.personalfinancecompanion.domain.model.Currency
import java.text.NumberFormat
import java.util.Locale

object CurrencyFormatter {
    
    fun formatAmount(amount: Double, currency: Currency): String {
        val locale = when (currency) {
            Currency.INR -> Locale("en", "IN")
            Currency.USD -> Locale.US
            Currency.EUR -> Locale.GERMANY
            Currency.GBP -> Locale.UK
            Currency.JPY -> Locale.JAPAN
            Currency.CAD -> Locale.CANADA
            Currency.AUD -> Locale("en", "AU")
            Currency.CHF -> Locale("de", "CH")
            Currency.CNY -> Locale.CHINA
            Currency.AED -> Locale("ar", "AE")
        }

        val format = NumberFormat.getCurrencyInstance(locale)
        // Ensure the symbol from our enum is used if the locale one differs
        // Or just let NumberFormat handle it. Usually, it's better to let NumberFormat handle it 
        // to get correct spacing and placement (e.g. symbol before or after).
        return format.format(amount)
    }

    fun formatWithoutSymbol(amount: Double, currency: Currency): String {
        val locale = when (currency) {
            Currency.INR -> Locale("en", "IN")
            Currency.USD -> Locale.US
            Currency.EUR -> Locale.GERMANY
            Currency.GBP -> Locale.UK
            Currency.JPY -> Locale.JAPAN
            Currency.CAD -> Locale.CANADA
            Currency.AUD -> Locale("en", "AU")
            Currency.CHF -> Locale("de", "CH")
            Currency.CNY -> Locale.CHINA
            Currency.AED -> Locale("ar", "AE")
        }

        val format = NumberFormat.getNumberInstance(locale).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }
        return format.format(amount)
    }
}
