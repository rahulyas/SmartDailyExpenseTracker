package com.rahul.smartdailyexpensetracker.presentation.utility

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.core.content.FileProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class PhotoManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "ExpenseWise")

        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }

        return File.createTempFile(
            "EXPENSE_${timeStamp}_",
            ".jpg",
            storageDir
        )
    }

    fun getImageUri(file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    fun copyImageToAppStorage(sourceUri: Uri): Uri? {
        return try {
            val fileName = "expense_${System.currentTimeMillis()}.jpg"
            val destinationFile = File(
                File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "ExpenseWise"),
                fileName
            )

            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                destinationFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            Uri.fromFile(destinationFile)
        } catch (e: Exception) {
            Log.e("PhotoManager", "Failed to copy image", e)
            null
        }
    }

    fun deleteImage(uri: Uri) {
        try {
            val file = File(uri.path ?: return)
            if (file.exists()) {
                file.delete()
            }
        } catch (e: Exception) {
            Log.e("PhotoManager", "Failed to delete image", e)
        }
    }

    fun getImageSize(uri: Uri): Long {
        return try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                input.available().toLong()
            } ?: 0L
        } catch (e: Exception) {
            0L
        }
    }
}
