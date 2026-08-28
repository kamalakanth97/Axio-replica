package com.example.util

import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object RupeeFormatter {

    private val indianRupeeFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
        currency = java.util.Currency.getInstance("INR")
        maximumFractionDigits = 2
        minimumFractionDigits = 0
    }

    private val compactDecimalFormat = DecimalFormat("0.#")

    /**
     * Formats an amount into standard Indian Rupee representation (e.g., ₹1,499 or ₹1,25,000.50)
     */
    fun formatRupees(amount: Double, showDecimals: Boolean = false): String {
        return try {
            val format = NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
                currency = java.util.Currency.getInstance("INR")
                maximumFractionDigits = if (showDecimals) 2 else if (amount % 1.0 == 0.0) 0 else 2
                minimumFractionDigits = if (showDecimals) 2 else 0
            }
            format.format(amount)
        } catch (e: Exception) {
            "₹${String.format(Locale("en", "IN"), "%,.2f", amount)}"
        }
    }

    /**
     * Compact representation for large amounts (e.g. ₹1.5L, ₹2.4Cr, ₹45k)
     */
    fun formatCompactRupees(amount: Double): String {
        val absAmount = Math.abs(amount)
        val sign = if (amount < 0) "-" else ""
        return when {
            absAmount >= 10_000_000 -> "$sign₹${compactDecimalFormat.format(absAmount / 10_000_000.0)}Cr"
            absAmount >= 100_000 -> "$sign₹${compactDecimalFormat.format(absAmount / 100_000.0)}L"
            absAmount >= 1_000 -> "$sign₹${compactDecimalFormat.format(absAmount / 1_000.0)}k"
            else -> "$sign₹${formatRupees(absAmount, false)}"
        }
    }

    fun formatDate(timestamp: Long, pattern: String = "dd MMM yyyy"): String {
        val sdf = SimpleDateFormat(pattern, Locale("en", "IN"))
        return sdf.format(Date(timestamp))
    }

    fun formatTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("hh:mm a", Locale("en", "IN"))
        return sdf.format(Date(timestamp))
    }
}
