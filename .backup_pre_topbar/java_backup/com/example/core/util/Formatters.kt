package com.example.core.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Formatters {
    private val numberFormat = DecimalFormat("#,##0", DecimalFormatSymbols(Locale.US))
    private val decimalFormat = DecimalFormat("#,##0.##", DecimalFormatSymbols(Locale.US))
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    fun formatMoney(amount: Double, currency: String = "د.ج"): String {
        return "${numberFormat.format(amount)} $currency"
    }

    fun formatQuantity(qty: Double, unit: String): String {
        return "${decimalFormat.format(qty)} $unit"
    }

    fun todayDate(): String = dateFormat.format(Date())
}
