package com.rahul.smartdailyexpensetracker.data.repository

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import com.rahul.smartdailyexpensetracker.domain.models.CategorySummary
import com.rahul.smartdailyexpensetracker.domain.models.DailyExpensesSummary
import com.rahul.smartdailyexpensetracker.domain.models.Expense
import com.rahul.smartdailyexpensetracker.domain.models.ReportData
import com.rahul.smartdailyexpensetracker.presentation.utility.ExpenseExporter
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.OutputStream
import java.io.Writer
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpenseExporterImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : ExpenseExporter {

    override suspend fun exportToCsv(
        expenses: List<Expense>,
        onProgress: ((String, Int) -> Unit)?
    ): String = withContext(Dispatchers.IO) {
        val fileName = "expenses_${System.currentTimeMillis()}.csv"

        val filePath = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveToDownloadsWithMediaStore(fileName, "text/csv") { outputStream ->
                writeCsvContent(outputStream, expenses, onProgress)
            }
        } else {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, fileName)
            writeCsvToFile(file, expenses, onProgress)
            file.absolutePath
        }

        onProgress?.invoke("CSV export complete!", 90)
        filePath
    }

    override suspend fun exportToJson(
        expenses: List<Expense>,
        onProgress: ((String, Int) -> Unit)?
    ): String = withContext(Dispatchers.IO) {
        val fileName = "expenses_${System.currentTimeMillis()}.json"

        val filePath = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveToDownloadsWithMediaStore(fileName, "application/json") { outputStream ->
                writeJsonContent(outputStream, expenses, onProgress)
            }
        } else {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, fileName)
            writeJsonToFile(file, expenses, onProgress)
            file.absolutePath
        }

        onProgress?.invoke("JSON export complete!", 90)
        filePath
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private suspend fun saveToDownloadsWithMediaStore(
        fileName: String,
        mimeType: String,
        writeContent: (OutputStream) -> Unit
    ): String {
        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }

        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            ?: throw Exception("Failed to create file in Downloads")

        resolver.openOutputStream(uri)?.use { outputStream ->
            writeContent(outputStream)
        } ?: throw Exception("Failed to open output stream")

        return "$fileName (Downloads folder)"
    }

    private fun writeCsvContent(
        outputStream: OutputStream,
        expenses: List<Expense>,
        onProgress: ((String, Int) -> Unit)?
    ) {
        outputStream.writer().use { writer ->
            onProgress?.invoke("Writing CSV data...", 50)

            // Write header
            writer.write("Date,Category,Amount,Description,Tags\n")

            expenses.forEachIndexed { index, expense ->
                writer.write("${expense.date},${expense.category.displayName},${expense.amount}}\"\n")

                // Update progress
                val progress = 50 + ((index + 1) * 40 / expenses.size)
                onProgress?.invoke("Writing expense ${index + 1} of ${expenses.size}...", progress)
            }
        }
    }

    private fun writeCsvToFile(
        file: File,
        expenses: List<Expense>,
        onProgress: ((String, Int) -> Unit)?
    ) {
        file.printWriter().use { writer ->
            onProgress?.invoke("Writing CSV data...", 50)

            // Write header
            writer.println("Date,Category,Amount,Description,Tags")

            expenses.forEachIndexed { index, expense ->
                writer.println("${expense.date},${expense.category.displayName},${expense.amount},}\"")

                // Update progress
                val progress = 50 + ((index + 1) * 40 / expenses.size)
                onProgress?.invoke("Writing expense ${index + 1} of ${expenses.size}...", progress)
            }
        }
    }

    private fun writeJsonContent(
        outputStream: OutputStream,
        expenses: List<Expense>,
        onProgress: ((String, Int) -> Unit)?
    ) {
        outputStream.writer().use { writer ->
            onProgress?.invoke("Converting to JSON...", 50)

            val jsonData = expenses.map { expense ->
                mapOf(
                    "id" to expense.id,
                    "title" to expense.title,
                    "date" to expense.date.toString(),
                    "category" to expense.category.displayName,
                    "amount" to expense.amount
                )
            }

            onProgress?.invoke("Writing JSON file...", 80)
            writeJsonString(writer, expenses, jsonData)
        }
    }

    private fun writeJsonToFile(
        file: File,
        expenses: List<Expense>,
        onProgress: ((String, Int) -> Unit)?
    ) {
        file.writer().use { writer ->
            onProgress?.invoke("Converting to JSON...", 50)

            val jsonData = expenses.map { expense ->
                mapOf(
                    "id" to expense.id,
                    "title" to expense.title,
                    "date" to expense.date.toString(),
                    "category" to expense.category.displayName,
                    "amount" to expense.amount
                )
            }

            onProgress?.invoke("Writing JSON file...", 80)
            writeJsonString(writer, expenses, jsonData)
        }
    }

    private fun writeJsonString(
        writer: Writer,
        expenses: List<Expense>,
        jsonData: List<Map<String, Any>>
    ) {
        writer.write("{\n")
        writer.write("  \"exportDate\": \"${LocalDate.now()}\",\n")
        writer.write("  \"totalExpenses\": ${expenses.size},\n")
        writer.write("  \"totalAmount\": ${expenses.sumOf { it.amount }},\n")
        writer.write("  \"expenses\": [\n")

        jsonData.forEachIndexed { index, expense ->
            writer.write("    {\n")
            expense.forEach { (key, value) ->
                val formattedValue = when (value) {
                    is String -> "\"$value\""
                    is List<*> -> value.joinToString(prefix = "[\"", postfix = "\"]", separator = "\", \"")
                    else -> value.toString()
                }
                writer.write("      \"$key\": $formattedValue")
                if (key != expense.keys.last()) writer.write(",")
                writer.write("\n")
            }
            writer.write("    }")
            if (index < jsonData.size - 1) writer.write(",")
            writer.write("\n")
        }

        writer.write("  ]\n")
        writer.write("}")
    }

    override suspend fun generateReportData(expenses: List<Expense>): ReportData {
        val totalAmount = expenses.sumOf { it.amount }
        val expenseCount = expenses.size

        // Calculate daily summaries
        val dailySummaries = expenses
            .groupBy { it.date }
            .map { (date, dayExpenses) ->
                DailyExpensesSummary(
                    date = date,
                    expenseCount = dayExpenses.size,
                    totalAmount = dayExpenses.sumOf { it.amount },
                    expenses = expenses
                )
            }
            .sortedBy { it.date }

        // Calculate category summaries
        val categorySummaries = expenses
            .groupBy { it.category }
            .map { (category, categoryExpenses) ->
                CategorySummary(
                    category = category,
                    expenseCount = categoryExpenses.size,
                    totalAmount = categoryExpenses.sumOf { it.amount }
                )
            }
            .sortedByDescending { it.totalAmount }

        // Calculate average daily amount
        val dateRange = if (dailySummaries.isNotEmpty()) {
            ChronoUnit.DAYS.between(dailySummaries.first().date, dailySummaries.last().date) + 1
        } else 1L
        val averageDaily = totalAmount / dateRange

        return ReportData(
            totalAmount = totalAmount,
            expenseCount = expenseCount,
            averageDaily = averageDaily,
            dailySummaries = dailySummaries,
            categorySummaries = categorySummaries
        )
    }
}