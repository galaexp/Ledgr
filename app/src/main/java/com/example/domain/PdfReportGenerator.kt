package com.example.domain

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.data.model.AccountEntity
import com.example.data.model.FriendEntity
import com.example.data.model.GroupEntity
import com.example.data.model.SplitExpenseEntity
import com.example.data.model.SplitParticipantEntity
import com.example.data.model.TransactionEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfReportGenerator {

    fun generateAndShareGroupReport(
        context: Context,
        group: GroupEntity,
        friends: List<FriendEntity>,
        expenses: List<SplitExpenseEntity>,
        participants: List<SplitParticipantEntity>,
        currency: String = "USD"
    ): File? {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 standard
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val titlePaint = Paint().apply {
            color = Color.parseColor("#070D09")
            textSize = 22f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val subPaint = Paint().apply {
            color = Color.parseColor("#00B594")
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val textPaint = Paint().apply {
            color = Color.parseColor("#334155")
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }

        val boldTextPaint = Paint().apply {
            color = Color.parseColor("#0F172A")
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val linePaint = Paint().apply {
            color = Color.parseColor("#E2E8F0")
            strokeWidth = 1f
        }

        val headerBgPaint = Paint().apply {
            color = Color.parseColor("#F1F5F9")
            style = Paint.Style.FILL
        }

        var y = 40f

        // Brand Banner
        canvas.drawText("LEDGR FINANCIAL STATEMENTS", 40f, y, titlePaint)
        y += 20f
        canvas.drawText("Group Bill Split & Expense Statement: ${group.name}", 40f, y, subPaint)
        y += 18f

        val sdf = SimpleDateFormat("MMM dd, yyyy · HH:mm", Locale.US)
        canvas.drawText("Generated on: ${sdf.format(Date())} | Currency: $currency", 40f, y, textPaint)
        y += 25f

        // Divider
        canvas.drawLine(40f, y, 555f, y, linePaint)
        y += 20f

        // Summary Card Box
        val totalGroupSpend = expenses.sumOf { it.totalAmount }
        canvas.drawRoundRect(40f, y, 555f, y + 55f, 8f, 8f, headerBgPaint)
        canvas.drawText("Total Group Expenses: $currency ${String.format(Locale.US, "%.2f", totalGroupSpend)}", 55f, y + 24f, boldTextPaint)
        canvas.drawText("Total Transactions: ${expenses.size} | Active Members: ${friends.size + 1}", 55f, y + 42f, textPaint)
        y += 75f

        // Table Header
        canvas.drawRect(40f, y, 555f, y + 24f, headerBgPaint)
        canvas.drawText("DATE", 48f, y + 16f, boldTextPaint)
        canvas.drawText("DESCRIPTION", 130f, y + 16f, boldTextPaint)
        canvas.drawText("PAID BY", 320f, y + 16f, boldTextPaint)
        canvas.drawText("TOTAL", 430f, y + 16f, boldTextPaint)
        canvas.drawText("TYPE", 495f, y + 16f, boldTextPaint)
        y += 30f

        val dateFmt = SimpleDateFormat("MMM dd", Locale.US)
        val friendMap = friends.associateBy { it.id }

        for (exp in expenses.take(20)) {
            val payerName = if (exp.payerFriendId == 0L) "You" else friendMap[exp.payerFriendId]?.name ?: "Friend"
            canvas.drawText(dateFmt.format(Date(exp.date)), 48f, y, textPaint)
            canvas.drawText(exp.title.take(28), 130f, y, textPaint)
            canvas.drawText(payerName.take(16), 320f, y, textPaint)
            canvas.drawText("$currency ${String.format(Locale.US, "%.2f", exp.totalAmount)}", 430f, y, boldTextPaint)
            canvas.drawText(exp.splitType.name, 495f, y, textPaint)

            y += 8f
            canvas.drawLine(40f, y, 555f, y, linePaint)
            y += 16f

            if (y > 750f) break
        }

        // Simplified Debts Section
        val simplifiedDebts = DebtSimplificationEngine.simplifyGroupDebts(friends, expenses, participants, currency)
        if (simplifiedDebts.isNotEmpty() && y < 700f) {
            y += 20f
            canvas.drawText("RECOMMENDED SETTLEMENTS (MINIMUM TRANSFERS)", 40f, y, subPaint)
            y += 18f
            for (debt in simplifiedDebts.take(5)) {
                canvas.drawText("• ${debt.fromName} pays ${debt.toName}  ->  $currency ${String.format(Locale.US, "%.2f", debt.amount)}", 48f, y, boldTextPaint)
                y += 16f
            }
        }

        // Footer
        canvas.drawText("Powered by Ledgr · Offline-first Intelligence", 40f, 810f, textPaint)

        document.finishPage(page)

        val file = File(context.cacheDir, "Ledgr_Report_${group.name.replace(" ", "_")}.pdf")
        try {
            FileOutputStream(file).use { out ->
                document.writeTo(out)
            }
            document.close()
            return file
        } catch (e: Exception) {
            e.printStackTrace()
            document.close()
            return null
        }
    }

    fun sharePdfFile(context: Context, file: File, title: String) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, "Here is the financial summary generated with Ledgr.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(intent, "Share Statement PDF")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}
