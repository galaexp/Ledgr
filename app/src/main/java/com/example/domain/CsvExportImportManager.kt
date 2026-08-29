package com.example.domain

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.data.model.*
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CsvExportImportManager {

    fun exportTransactionsToCsv(context: Context, transactions: List<TransactionEntity>, accounts: List<AccountEntity>, categories: List<CategoryEntity>): File? {
        val accountMap = accounts.associateBy { it.id }
        val categoryMap = categories.associateBy { it.id }
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

        val csvBuilder = StringBuilder()
        csvBuilder.append("ID,Date,Type,Amount,Currency,Account,Category,Notes,Tags,CountryProfile\n")

        for (tx in transactions) {
            val accName = accountMap[tx.accountId]?.name ?: "Account #${tx.accountId}"
            val catName = categoryMap[tx.categoryId]?.name ?: "Category #${tx.categoryId}"
            val dateStr = sdf.format(Date(tx.date))
            val cleanNotes = tx.notes.replace(",", ";").replace("\n", " ")
            val cleanTags = tx.tags.replace(",", ";")

            csvBuilder.append("${tx.id},\"$dateStr\",${tx.type},${tx.amount},${tx.currency},\"$accName\",\"$catName\",\"$cleanNotes\",\"$cleanTags\",${tx.countryProfile}\n")
        }

        val file = File(context.cacheDir, "Ledgr_Transactions_Export_${System.currentTimeMillis()}.csv")
        return try {
            FileOutputStream(file).use { out ->
                out.write(csvBuilder.toString().toByteArray(Charsets.UTF_8))
            }
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun shareCsvFile(context: Context, file: File, subject: String = "Ledgr Data Export") {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, "Exported financial data from Ledgr.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(intent, "Share CSV Export")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    fun parseImportedCsv(csvContent: String): List<TransactionEntity> {
        val lines = csvContent.lines().filter { it.isNotBlank() }
        if (lines.size <= 1) return emptyList()

        val parsed = mutableListOf<TransactionEntity>()
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

        for (line in lines.drop(1)) {
            try {
                val tokens = line.split(",")
                if (tokens.size >= 5) {
                    val typeStr = tokens.getOrNull(2)?.trim()?.uppercase() ?: "EXPENSE"
                    val type = try { TransactionType.valueOf(typeStr) } catch (_: Exception) { TransactionType.EXPENSE }
                    val amount = tokens.getOrNull(3)?.toDoubleOrNull() ?: 0.0
                    val currency = tokens.getOrNull(4)?.trim() ?: "USD"
                    val notes = tokens.getOrNull(7)?.replace("\"", "") ?: ""
                    val country = tokens.getOrNull(9)?.trim() ?: "HOME"

                    parsed.add(
                        TransactionEntity(
                            accountId = 1,
                            type = type,
                            amount = amount,
                            currency = currency,
                            categoryId = 1,
                            date = System.currentTimeMillis(),
                            notes = notes,
                            countryProfile = country
                        )
                    )
                }
            } catch (_: Exception) {}
        }
        return parsed
    }
}
