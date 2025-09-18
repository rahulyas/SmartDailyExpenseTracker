package com.rahul.smartdailyexpensetracker.presentation.utility

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.RequiresApi
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Image
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.HorizontalAlignment
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import com.rahul.smartdailyexpensetracker.domain.models.ReportData
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SimplePdfExportManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    suspend fun generateExpenseReportPdf(
        reportData: ReportData,
        dailyChartBitmap: Bitmap? = null,
        categoryChartBitmap: Bitmap? = null
    ): String = withContext(Dispatchers.IO) {

        val fileName = "expense_report_${System.currentTimeMillis()}.pdf"

        // Save to Downloads folder
        val file = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // For Android 10+ use MediaStore for Downloads
            saveToDownloadsWithMediaStore(fileName, reportData, dailyChartBitmap, categoryChartBitmap)
        } else {
            // For older versions, use Environment.getExternalStoragePublicDirectory
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, fileName)
            generatePdfToFile(file, reportData, dailyChartBitmap, categoryChartBitmap)
            file.absolutePath
        }

        file
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private suspend fun saveToDownloadsWithMediaStore(
        fileName: String,
        reportData: ReportData,
        dailyChartBitmap: Bitmap?,
        categoryChartBitmap: Bitmap?
    ): String {
        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }

        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            ?: throw Exception("Failed to create file in Downloads")

        resolver.openOutputStream(uri)?.use { outputStream ->
            val writer = PdfWriter(outputStream)
            val pdf = PdfDocument(writer)
            val document = Document(pdf, PageSize.A4)

            generatePdfContent(document, reportData, dailyChartBitmap, categoryChartBitmap)

            document.close()
        } ?: throw Exception("Failed to open output stream")

        // Return the file path for display purposes
        return "$fileName (Downloads folder)"
    }

    private fun generatePdfToFile(
        file: File,
        reportData: ReportData,
        dailyChartBitmap: Bitmap?,
        categoryChartBitmap: Bitmap?
    ) {
        val writer = PdfWriter(file)
        val pdf = PdfDocument(writer)
        val document = Document(pdf, PageSize.A4)

        generatePdfContent(document, reportData, dailyChartBitmap, categoryChartBitmap)

        document.close()
    }

    private fun generatePdfContent(
        document: Document,
        reportData: ReportData,
        dailyChartBitmap: Bitmap?,
        categoryChartBitmap: Bitmap?
    ) {
        try {
            // Add title
            document.add(
                Paragraph("Expense Report")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(24f)
                    .setBold()
                    .setMarginBottom(20f)
            )

            // Add summary
            document.add(
                Paragraph("Total Amount: ₹${String.format("%.2f", reportData.totalAmount)}")
                    .setFontSize(14f)
                    .setMarginBottom(10f)
            )

            document.add(
                Paragraph("Total Expenses: ${reportData.expenseCount}")
                    .setFontSize(14f)
                    .setMarginBottom(10f)
            )

            document.add(
                Paragraph("Daily Average: ₹${String.format("%.2f", reportData.averageDaily)}")
                    .setFontSize(14f)
                    .setMarginBottom(20f)
            )

            // Add daily summary
            document.add(
                Paragraph("Daily Summary")
                    .setFontSize(16f)
                    .setBold()
                    .setMarginBottom(10f)
            )

            val dailyTable = Table(3)
            dailyTable.addHeaderCell("Date")
            dailyTable.addHeaderCell("Expenses")
            dailyTable.addHeaderCell("Amount")

            reportData.dailySummaries.forEach { summary ->
                dailyTable.addCell(summary.date.format(DateTimeFormatter.ofPattern("MMM dd")))
                dailyTable.addCell(summary.expenseCount.toString())
                dailyTable.addCell("₹${String.format("%.2f", summary.totalAmount)}")
            }

            document.add(dailyTable)
            document.add(Paragraph().setMarginBottom(20f))

            // Add category summary
            document.add(
                Paragraph("Category Breakdown")
                    .setFontSize(16f)
                    .setBold()
                    .setMarginBottom(10f)
            )

            val categoryTable = Table(3)
            categoryTable.addHeaderCell("Category")
            categoryTable.addHeaderCell("Expenses")
            categoryTable.addHeaderCell("Amount")

            reportData.categorySummaries.forEach { summary ->
                categoryTable.addCell(summary.category.displayName)
                categoryTable.addCell(summary.expenseCount.toString())
                categoryTable.addCell("₹${String.format("%.2f", summary.totalAmount)}")
            }

            document.add(categoryTable)

            // Add charts if available
            dailyChartBitmap?.let { bitmap ->
                val stream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                val imageData = ImageDataFactory.create(stream.toByteArray())
                val image = Image(imageData)
                image.setWidth(UnitValue.createPercentValue(80f))
                image.setHorizontalAlignment(HorizontalAlignment.CENTER)
                document.add(image)
            }

            categoryChartBitmap?.let { bitmap ->
                val stream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                val imageData = ImageDataFactory.create(stream.toByteArray())
                val image = Image(imageData)
                image.setWidth(UnitValue.createPercentValue(80f))
                image.setHorizontalAlignment(HorizontalAlignment.CENTER)
                document.add(image)
            }

        } catch (e: Exception) {
            Log.e("SimplePdfExportManager", "Failed to generate PDF content", e)
            throw Exception("Failed to generate PDF content: ${e.message}")
        }
    }

    suspend fun shareExpenseReportPdf(
        reportData: ReportData,
        dailyChartBitmap: Bitmap? = null,
        categoryChartBitmap: Bitmap? = null
    ): String = withContext(Dispatchers.IO) {

        val fileName = "expense_report_${System.currentTimeMillis()}.pdf"
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)

        try {
            val writer = PdfWriter(file)
            val pdf = PdfDocument(writer)
            val document = Document(pdf, PageSize.A4)

            // Add title
            document.add(
                Paragraph("Expense Report")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(24f)
                    .setBold()
                    .setMarginBottom(20f)
            )

            // Add summary
            document.add(
                Paragraph("Total Amount: ₹${String.format("%.2f", reportData.totalAmount)}")
                    .setFontSize(14f)
                    .setMarginBottom(10f)
            )

            document.add(
                Paragraph("Total Expenses: ${reportData.expenseCount}")
                    .setFontSize(14f)
                    .setMarginBottom(10f)
            )

            document.add(
                Paragraph("Daily Average: ₹${String.format("%.2f", reportData.averageDaily)}")
                    .setFontSize(14f)
                    .setMarginBottom(20f)
            )

            // Add daily summary
            document.add(
                Paragraph("Daily Summary")
                    .setFontSize(16f)
                    .setBold()
                    .setMarginBottom(10f)
            )

            val dailyTable = Table(3)
            dailyTable.addHeaderCell("Date")
            dailyTable.addHeaderCell("Expenses")
            dailyTable.addHeaderCell("Amount")

            reportData.dailySummaries.forEach { summary ->
                dailyTable.addCell(summary.date.format(DateTimeFormatter.ofPattern("MMM dd")))
                dailyTable.addCell(summary.expenseCount.toString())
                dailyTable.addCell("₹${String.format("%.2f", summary.totalAmount)}")
            }

            document.add(dailyTable)
            document.add(Paragraph().setMarginBottom(20f))

            // Add category summary
            document.add(
                Paragraph("Category Breakdown")
                    .setFontSize(16f)
                    .setBold()
                    .setMarginBottom(10f)
            )

            val categoryTable = Table(3)
            categoryTable.addHeaderCell("Category")
            categoryTable.addHeaderCell("Expenses")
            categoryTable.addHeaderCell("Amount")

            reportData.categorySummaries.forEach { summary ->
                categoryTable.addCell(summary.category.displayName)
                categoryTable.addCell(summary.expenseCount.toString())
                categoryTable.addCell("₹${String.format("%.2f", summary.totalAmount)}")
            }

            document.add(categoryTable)

            // Add charts if available
            dailyChartBitmap?.let { bitmap ->
                val stream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                val imageData = ImageDataFactory.create(stream.toByteArray())
                val image = Image(imageData)
                image.setWidth(UnitValue.createPercentValue(80f))
                image.setHorizontalAlignment(HorizontalAlignment.CENTER)
                document.add(image)
            }

            document.close()
            file.absolutePath

        } catch (e: Exception) {
            Log.e("SimplePdfExportManager", "Failed to generate PDF", e)
            throw Exception("Failed to generate PDF: ${e.message}")
        }
    }
}