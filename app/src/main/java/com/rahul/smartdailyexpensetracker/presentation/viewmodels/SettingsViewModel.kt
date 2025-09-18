package com.rahul.smartdailyexpensetracker.presentation.viewmodels

import android.Manifest
import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rahul.smartdailyexpensetracker.R
import com.rahul.smartdailyexpensetracker.data.preferences.UserPreferences
import com.rahul.smartdailyexpensetracker.domain.usecases.ExportExpensesUseCase
import com.rahul.smartdailyexpensetracker.presentation.uistate.SettingsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferences: UserPreferences,
    private val exportExpensesUseCase: ExportExpensesUseCase, // Updated use case
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private var exportJob: Job? = null

    init {
        createNotificationChannel()
        observePreferences()
    }
    private fun createNotificationChannel() {
        val channelId = "export_channel"
        val channelName = "Export Notifications"
        val channelDescription = "Notifications for export operations"
        val importance = NotificationManager.IMPORTANCE_DEFAULT

        val channel = NotificationChannel(channelId, channelName, importance).apply {
            description = channelDescription
            enableVibration(true)
            enableLights(true)
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun observePreferences() {
        viewModelScope.launch {
            userPreferences.userPreferencesFlow.collect { preferences ->
                _uiState.update { it.copy(preferences = preferences) }
            }
        }
    }


    fun updateDarkTheme(isDark: Boolean) {
        viewModelScope.launch {
            userPreferences.setDarkTheme(isDark)
        }
    }

    fun updateDuplicateCheck(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setDuplicateCheck(enabled)
        }
    }

    fun updateNotifications(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setNotifications(enabled)
        }
    }

    fun updateAutoBackup(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setAutoBackup(enabled)
        }
    }

    fun updateCurrency(currency: String) {
        viewModelScope.launch {
            userPreferences.setCurrency(currency)
        }
    }

    fun updateExportFormat(format: ExportExpensesUseCase.ExportFormat) {
        viewModelScope.launch {
            userPreferences.setExportFormat(format)
        }
    }

    fun updateDecimalPlaces(places: Int) {
        viewModelScope.launch {
            userPreferences.setDecimalPlaces(places)
        }
    }

    fun updateReminderTime(time: String) {
        viewModelScope.launch {
            userPreferences.setDailyReminderTime(time)
        }
    }

    fun showResetDialog() {
        _uiState.update { it.copy(showResetDialog = true) }
    }

    fun hideResetDialog() {
        _uiState.update { it.copy(showResetDialog = false) }
    }

    fun resetAllPreferences() {
        viewModelScope.launch {
            try {
                userPreferences.resetPreferences()
                _uiState.update {
                    it.copy(
                        showResetDialog = false,
                        exportMessage = "Settings reset successfully"
                    )
                }
                // Clear the message after a delay
                delay(3000)
                clearExportStatus()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        showResetDialog = false,
                        exportError = "Failed to reset settings: ${e.message}"
                    )
                }
            }
        }
    }

    // Updated export function to work with the new use case structure
    fun exportExpenses(
        startDate: LocalDate,
        endDate: LocalDate,
        format: ExportExpensesUseCase.ExportFormat
    ) {
        // Cancel any existing export job
        exportJob?.cancel()

        exportJob = viewModelScope.launch {
            try {
                _uiState.update {
                    it.copy(
                        isExporting = true,
                        exportProgress = 0,
                        exportMessage = "Starting export...",
                        exportError = null,
                        exportComplete = false
                    )
                }

                val result = exportExpensesUseCase.execute(
                    ExportExpensesUseCase.Params(
                        startDate = startDate,
                        endDate = endDate,
                        format = format,
                        onProgress = { message, progress ->
                            _uiState.update { currentState ->
                                currentState.copy(
                                    exportMessage = message,
                                    exportProgress = progress
                                )
                            }
                        }
                    )
                )

                _uiState.update {
                    it.copy(
                        isExporting = false,
                        exportComplete = true,
                        exportProgress = 100,
                        exportMessage = "Export completed! ${result.recordCount} expenses exported to ${File(result.filePath).name}"
                    )
                }

                // Show success notification
                showExportNotification(result.filePath, result.recordCount, format)

            } catch (e: Exception) {
                Log.e("SettingsViewModel", "Export failed", e)
                _uiState.update {
                    it.copy(
                        isExporting = false,
                        exportError = e.message ?: "Export failed",
                        exportProgress = 0
                    )
                }
            }
        }
    }

    fun cancelExport() {
        exportJob?.cancel()
        _uiState.update {
            it.copy(
                isExporting = false,
                exportMessage = "Export cancelled",
                exportProgress = 0
            )
        }
    }

    fun clearExportStatus() {
        _uiState.update {
            it.copy(
                exportComplete = false,
                exportError = null,
                exportMessage = "",
                exportProgress = 0
            )
        }
    }

    private fun showExportNotification(filePath: String, recordCount: Int, format: ExportExpensesUseCase.ExportFormat) {
        // Check notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.w("SettingsViewModel", "Notification permission not granted")
                return
            }
        }

        val channelId = "export_channel"
        val fileName = if (filePath.contains("Downloads folder")) {
            filePath
        } else {
            File(filePath).name
        }

        // Create appropriate intent based on format and Android version
        val intent = createOpenFileIntent(fileName)

        val pendingIntent = if (intent != null) {
            PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        } else null

        val actionText = when (format) {
            ExportExpensesUseCase.ExportFormat.PDF -> "Open Downloads"
            ExportExpensesUseCase.ExportFormat.CSV -> "Open Downloads"
            ExportExpensesUseCase.ExportFormat.JSON -> "Open Downloads"
        }

        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setContentTitle("Export Complete")
            .setContentText("$recordCount expenses exported as ${format.name}")
            .setSmallIcon(R.drawable.baseline_check_24)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        // Add content intent if available
        pendingIntent?.let {
            notificationBuilder.setContentIntent(it)
        }

        // Add action button
        val downloadsIntent = Intent(DownloadManager.ACTION_VIEW_DOWNLOADS)
        val downloadsPendingIntent = PendingIntent.getActivity(
            context,
            1,
            downloadsIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        notificationBuilder.addAction(
            R.drawable.baseline_folder_24,
            actionText,
            downloadsPendingIntent
        )

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(1002, notificationBuilder.build())
            Log.d("SettingsViewModel", "Notification shown successfully")
        } catch (e: Exception) {
            Log.e("SettingsViewModel", "Failed to show notification", e)
        }
    }

    private fun createOpenFileIntent(filePath: String): Intent? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && filePath.contains("Downloads folder")) {
                // For Android 10+, just open Downloads folder
                Intent(DownloadManager.ACTION_VIEW_DOWNLOADS)
            } else {
                // For older versions, try to open the specific file
                val file = File(filePath)
                if (file.exists()) {
                    Intent(Intent.ACTION_VIEW).apply {
                        data = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            file
                        )
                        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    }
                } else {
                    Intent(DownloadManager.ACTION_VIEW_DOWNLOADS)
                }
            }
        } catch (e: Exception) {
            Log.e("SettingsViewModel", "Failed to create file intent", e)
            Intent(DownloadManager.ACTION_VIEW_DOWNLOADS)
        }
    }

    override fun onCleared() {
        super.onCleared()
        exportJob?.cancel()
    }
}