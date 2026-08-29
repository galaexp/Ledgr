package com.example.domain

import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

data class ParsedReceiptItem(
    val name: String,
    val price: Double,
    val quantity: Int = 1
)

data class ParsedReceipt(
    val merchantName: String,
    val totalAmount: Double,
    val taxAmount: Double,
    val tipAmount: Double,
    val dateEpoch: Long,
    val items: List<ParsedReceiptItem>,
    val rawText: String
)

object ReceiptOcrEngine {

    /**
     * Parses raw OCR text lines from receipts, extracts merchant names, dates,
     * subtotal, tax, tip, itemized items, and total amount.
     */
    fun parseReceiptText(text: String): ParsedReceipt {
        val lines = text.lines().map { it.trim() }.filter { it.isNotEmpty() }
        if (lines.isEmpty()) {
            return ParsedReceipt("Unknown Store", 0.0, 0.0, 0.0, System.currentTimeMillis(), emptyList(), "")
        }

        // Guess Merchant: Usually the first non-numeric prominent header line
        var merchant = lines.firstOrNull { line ->
            !line.contains(Regex("^(tax|invoice|date|time|total|tel|phone|cashier|order|#)", RegexOption.IGNORE_CASE)) &&
                    line.length > 2 && !line.all { it.isDigit() || it == '.' || it == '-' }
        } ?: "Store Receipt"

        // Clean merchant
        merchant = merchant.replace(Regex("[^a-zA-Z0-9 &'-]"), "").trim()
        if (merchant.isBlank()) merchant = "Retail Merchant"

        var totalAmount = 0.0
        var taxAmount = 0.0
        var tipAmount = 0.0
        var parsedDate = System.currentTimeMillis()
        val items = mutableListOf<ParsedReceiptItem>()

        val priceRegex = Pattern.compile("[$€£₹¥]?\\s*([0-9]+[.,][0-9]{2})\\b")
        val dateRegex = Pattern.compile("(\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4})|(\\d{4}[/-]\\d{1,2}[/-]\\d{1,2})")

        // Parse lines
        for (line in lines) {
            val lower = line.lowercase()

            // Check Date
            val dateMatcher = dateRegex.matcher(line)
            if (dateMatcher.find()) {
                val dateStr = dateMatcher.group(1) ?: dateMatcher.group(2)
                if (dateStr != null) {
                    parsedDate = tryParseDate(dateStr) ?: parsedDate
                }
            }

            // Extract price from line
            val priceMatcher = priceRegex.matcher(line)
            var extractedPrice: Double? = null
            var lastMatchEnd = 0
            while (priceMatcher.find()) {
                val rawVal = priceMatcher.group(1)?.replace(",", ".")
                extractedPrice = rawVal?.toDoubleOrNull()
                lastMatchEnd = priceMatcher.end()
            }

            if (extractedPrice != null) {
                if (lower.contains("total") || lower.contains("grand total") || lower.contains("amount due") || lower.contains("net payable")) {
                    if (totalAmount == 0.0 || extractedPrice > totalAmount) {
                        totalAmount = extractedPrice
                    }
                } else if (lower.contains("tax") || lower.contains("vat") || lower.contains("gst")) {
                    taxAmount = extractedPrice
                } else if (lower.contains("tip") || lower.contains("gratuity")) {
                    tipAmount = extractedPrice
                } else if (lower.contains("subtotal") || lower.contains("sub-total")) {
                    // subtotal
                } else {
                    // Line item
                    val itemName = line.substring(0, priceMatcher.regionStart().coerceAtLeast(0))
                        .replace(priceRegex.toRegex(), "")
                        .replace(Regex("[^a-zA-Z0-9 &'-]"), " ")
                        .trim()
                    if (itemName.isNotBlank() && itemName.length > 1 && extractedPrice > 0.05) {
                        items.add(ParsedReceiptItem(name = itemName.take(35), price = extractedPrice))
                    }
                }
            }
        }

        // If total not found by keyword, pick the max price found or sum of items
        if (totalAmount <= 0.0) {
            if (items.isNotEmpty()) {
                totalAmount = items.sumOf { it.price } + taxAmount + tipAmount
            }
        }

        return ParsedReceipt(
            merchantName = merchant,
            totalAmount = Math.round(totalAmount * 100.0) / 100.0,
            taxAmount = Math.round(taxAmount * 100.0) / 100.0,
            tipAmount = Math.round(tipAmount * 100.0) / 100.0,
            dateEpoch = parsedDate,
            items = items,
            rawText = text
        )
    }

    private fun tryParseDate(dateStr: String): Long? {
        val formats = listOf(
            "MM/dd/yyyy", "MM-dd-yyyy",
            "dd/MM/yyyy", "dd-MM-yyyy",
            "yyyy/MM/dd", "yyyy-MM-dd",
            "MM/dd/yy", "dd/MM/yy"
        )
        for (fmt in formats) {
            try {
                val sdf = SimpleDateFormat(fmt, Locale.US)
                sdf.isLenient = false
                val parsed = sdf.parse(dateStr)
                if (parsed != null && parsed.time <= System.currentTimeMillis() + 86400000L) {
                    return parsed.time
                }
            } catch (_: Exception) {}
        }
        return null
    }
}
