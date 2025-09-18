package com.rahul.smartdailyexpensetracker.presentation.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rahul.smartdailyexpensetracker.R
import com.rahul.smartdailyexpensetracker.domain.usecases.ExportExpensesUseCase
import com.rahul.smartdailyexpensetracker.presentation.components.ExportDialog
import com.rahul.smartdailyexpensetracker.presentation.components.ExportProgressDialog
import com.rahul.smartdailyexpensetracker.presentation.components.SettingsButton
import com.rahul.smartdailyexpensetracker.presentation.components.SettingsDropdown
import com.rahul.smartdailyexpensetracker.presentation.components.SettingsSection
import com.rahul.smartdailyexpensetracker.presentation.components.SettingsSlider
import com.rahul.smartdailyexpensetracker.presentation.components.SettingsSwitch
import com.rahul.smartdailyexpensetracker.presentation.viewmodels.SettingsViewModel
import com.rahul.smartdailyexpensetracker.utils.PermissionHelper
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val permissionHelper = remember { PermissionHelper(context) }
    val uiState by viewModel.uiState.collectAsState()
    val preferences = uiState.preferences
    var showExportDialog by remember { mutableStateOf(false) }

    val manageStorageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        if (permissionHelper.hasStoragePermissions()) {
            showExportDialog = true
        } else {
            Toast.makeText(context, "Storage permission is required to export.", Toast.LENGTH_SHORT).show()
        }
    }

    val storagePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.all { it }
        if (granted) {
            showExportDialog = true
        } else {
            Toast.makeText(context, "Storage permission is required to export.", Toast.LENGTH_SHORT).show()
        }
    }

    fun requestAllFilesAccess() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                    data = Uri.parse("package:${context.packageName}")
                }
                manageStorageLauncher.launch(intent)
            } catch (e: Exception) {
                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                manageStorageLauncher.launch(intent)
            }
        } else {
            storagePermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            )
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopAppBar(
            title = { Text("Settings") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        )

        LazyColumn (
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Theme Settings
            item {
                SettingsSection(title = "Appearance") {
                    SettingsSwitch(
                        title = "Dark Theme",
                        subtitle = "Enable dark mode",
                        checked = preferences.isDarkTheme,
                        onCheckedChange = viewModel::updateDarkTheme,
                        icon = if (preferences.isDarkTheme) painterResource(id = R.drawable.baseline_dark_mode_24) else painterResource(id = R.drawable.baseline_light_mode_24)
                    )
                }
            }

            // Currency Settings
            item {
                SettingsSection(title = "Currency") {
                    SettingsDropdown(
                        title = "Default Currency",
                        subtitle = "Select your preferred currency",
                        selectedValue = preferences.defaultCurrency,
                        options = listOf("₹", "$", "€", "£", "¥"),
                        onValueChange = viewModel::updateCurrency,
                        icon = painterResource(id = R.drawable.baseline_currency_exchange_24)
                    )
                }
            }

            // Export Settings
            item {
                SettingsSection(title = "Export") {
                    SettingsDropdown(
                        title = "Default Export Format",
                        subtitle = "Choose export file format",
                        selectedValue = preferences.exportFormat.name,
                        options = ExportExpensesUseCase.ExportFormat.entries.map { it.name },
                        onValueChange = { formatName ->
                            val format = ExportExpensesUseCase.ExportFormat.valueOf(formatName)
                            viewModel.updateExportFormat(format)
                        },
                        icon = painterResource(id = R.drawable.baseline_file_download_24)
                    )

                    SettingsSlider(
                        title = "Decimal Places",
                        subtitle = "Number of decimal places in amounts",
                        value = preferences.showDecimalPlaces.toFloat(),
                        range = 0f..4f,
                        steps = 3,
                        onValueChange = { value ->
                            viewModel.updateDecimalPlaces(value.toInt())
                        },
                        icon = painterResource(id = R.drawable.baseline_numbers_24)
                    )

                    // Export Button
                    SettingsButton(
                        title = "Export Expenses",
                        subtitle = "Export your expense data",
                        onClick = {
                            if (permissionHelper.hasAllFilesAccess()) {
                                showExportDialog = true
                            } else {
                                requestAllFilesAccess()
                            }
                        },
                        icon = painterResource(id = R.drawable.baseline_file_download_24),
                        isDestructive = false
                    )
                }
            }

            item {
                SettingsSection(title = "Behavior") {
                    SettingsSwitch(
                        title = "Duplicate Check",
                        subtitle = "Check for duplicate expenses",
                        checked = preferences.enableDuplicateCheck,
                        onCheckedChange = viewModel::updateDuplicateCheck,
                        icon = painterResource(id = R.drawable.baseline_security_24)
                    )

                    SettingsSwitch(
                        title = "Notifications",
                        subtitle = "Enable expense reminders",
                        checked = preferences.enableNotifications,
                        onCheckedChange = viewModel::updateNotifications,
                        icon = painterResource(id = R.drawable.baseline_notifications_24)
                    )

                    SettingsSwitch(
                        title = "Auto Backup",
                        subtitle = "Automatically backup data",
                        checked = preferences.autoBackup,
                        onCheckedChange = viewModel::updateAutoBackup,
                        icon = painterResource(id = R.drawable.baseline_backup_24)
                    )
                }
            }

            item {
                SettingsSection(title = "Advanced") {
                    SettingsButton(
                        title = "Reset All Settings",
                        subtitle = "Restore default preferences",
                        onClick = viewModel::showResetDialog,
                        icon = painterResource(id = R.drawable.baseline_restore_from_trash_24),
                        isDestructive = true
                    )
                }
            }
        }
    }

    if (showExportDialog) {
        ExportDialog(
            onDismiss = { showExportDialog = false },
            onExport = viewModel::exportExpenses,
            defaultFormat = preferences.exportFormat
        )
    }

    if (uiState.isExporting) {
        ExportProgressDialog(
            message = uiState.exportMessage,
            progress = uiState.exportProgress,
            onCancel = {
                viewModel.cancelExport()
            }
        )
    }

    if (uiState.showResetDialog) {
        AlertDialog(
            onDismissRequest = viewModel::hideResetDialog,
            title = { Text("Reset Settings") },
            text = { Text("Are you sure you want to reset all settings to default values? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = viewModel::resetAllPreferences
                ) {
                    Text("Reset", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::hideResetDialog) {
                    Text("Cancel")
                }
            }
        )
    }

    LaunchedEffect(uiState.exportComplete) {
        if (uiState.exportComplete) {
            delay(3000)
            viewModel.clearExportStatus()
        }
    }

    LaunchedEffect(uiState.exportError) {
        if (uiState.exportError != null) {
            delay(3000)
            viewModel.clearExportStatus()
        }
    }
}