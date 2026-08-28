package com.example.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.local.entity.TransactionEntity
import com.example.data.model.ExpenseCategory
import com.example.data.model.TransactionType
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

enum class ExportDateRange(val displayName: String) {
    ALL_TIME("All Time"),
    THIS_MONTH("This Month (Aug 2026)"),
    LAST_30_DAYS("Last 30 Days"),
    EXPENSES_ONLY("Only Expenses"),
    INCOME_ONLY("Only Incomes")
}

object CsvExportHelper {

    private val dateFormat = SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH)
    private val timeFormat = SimpleDateFormat("hh:mm a", Locale.ENGLISH)
    private val fileTimestampFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH)

    /**
     * Generates a standard RFC 4180 CSV string from a list of transactions.
     */
    fun generateCsvString(
        transactions: List<TransactionEntity>,
        title: String = "axio Expense Report"
    ): String {
        val sb = StringBuilder()

        // Metadata Header
        val now = Date()
        val totalIncome = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val totalExpense = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        val netSavings = totalIncome - totalExpense

        sb.append("# $title\n")
        sb.append("# Generated on: ${dateFormat.format(now)} ${timeFormat.format(now)}\n")
        sb.append("# Currency: INR (₹)\n")
        sb.append("# Total Records: ${transactions.size}\n")
        sb.append("# Total Income: ₹${String.format(Locale.US, "%.2f", totalIncome)}\n")
        sb.append("# Total Expense: ₹${String.format(Locale.US, "%.2f", totalExpense)}\n")
        sb.append("# Net Savings: ₹${String.format(Locale.US, "%.2f", netSavings)}\n")
        sb.append("\n")

        // CSV Header Row
        sb.append("Transaction ID,Date,Time,Title / Payee,Type,Category,Amount (INR),Payment Mode,Account,Tags & Notes,SMS Auto-Parsed\n")

        // Rows
        for (txn in transactions) {
            val dateStr = dateFormat.format(Date(txn.timestamp))
            val timeStr = timeFormat.format(Date(txn.timestamp))
            val escapedTitle = escapeCsv(txn.title)
            val typeStr = txn.type.name
            val categoryStr = escapeCsv(txn.category.displayName)
            val amountStr = String.format(Locale.US, "%.2f", txn.amount)
            val paymentModeStr = escapeCsv(txn.paymentMode.displayName)
            val accountStr = escapeCsv(txn.accountName)
            val notesStr = escapeCsv(txn.notes.ifBlank { "-" })
            val isSmsStr = if (txn.isSmsParsed) "YES" else "NO"

            sb.append("${txn.id},$dateStr,$timeStr,$escapedTitle,$typeStr,$categoryStr,$amountStr,$paymentModeStr,$accountStr,$notesStr,$isSmsStr\n")
        }

        return sb.toString()
    }

    private fun escapeCsv(value: String): String {
        var res = value.replace("\"", "\"\"")
        if (res.contains(",") || res.contains("\"") || res.contains("\n")) {
            res = "\"$res\""
        }
        return res
    }

    /**
     * Writes CSV data to cache directory and shares it via Android system chooser.
     */
    fun shareCsvReport(context: Context, transactions: List<TransactionEntity>, filterName: String = "All"): Boolean {
        return try {
            val csvContent = generateCsvString(transactions, "axio Report - $filterName")
            val fileName = "axio_expenses_${fileTimestampFormat.format(Date())}.csv"
            val exportDir = File(context.cacheDir, "exports")
            if (!exportDir.exists()) exportDir.mkdirs()

            val file = File(exportDir, fileName)
            val writer = FileWriter(file)
            writer.write(csvContent)
            writer.flush()
            writer.close()

            val authority = "${context.packageName}.fileprovider"
            val fileUri: Uri = try {
                FileProvider.getUriForFile(context, authority, file)
            } catch (e: Exception) {
                // If fileprovider is not set up, fall back to plain text sharing
                null
            } ?: run {
                shareCsvAsText(context, csvContent, fileName)
                return true
            }

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_SUBJECT, "axio Financial Statement ($filterName)")
                putExtra(Intent.EXTRA_TEXT, "Here is my exported Indian Rupee expense report from axio.\nTotal records: ${transactions.size}")
                putExtra(Intent.EXTRA_STREAM, fileUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val chooser = Intent.createChooser(shareIntent, "Share Expense Report (CSV)")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
            true
        } catch (e: Exception) {
            // Fallback to text intent
            shareCsvAsText(context, generateCsvString(transactions), "axio_expenses.csv")
            true
        }
    }

    fun shareCsvAsText(context: Context, csvContent: String, title: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "axio Expense Report (CSV)")
            putExtra(Intent.EXTRA_TEXT, csvContent)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val chooser = Intent.createChooser(shareIntent, "Share Expense Report (CSV)")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    fun copyCsvToClipboard(context: Context, transactions: List<TransactionEntity>) {
        val csv = generateCsvString(transactions)
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("axio Expenses CSV", csv)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "CSV data copied to clipboard!", Toast.LENGTH_SHORT).show()
    }
}
