package com.rahul.smartdailyexpensetracker.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.rahul.smartdailyexpensetracker.di.dataStore
import com.rahul.smartdailyexpensetracker.domain.models.ExpenseCategory
import com.rahul.smartdailyexpensetracker.domain.usecases.ExportExpensesUseCase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Singleton



@Singleton
class UserPreferences (
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    // Preference Keys
    private object PreferencesKeys {
        val IS_DARK_THEME = booleanPreferencesKey("is_dark_theme")
        val DEFAULT_CURRENCY = stringPreferencesKey("default_currency")
        val LAST_USED_CATEGORY = stringPreferencesKey("last_used_category")
        val ENABLE_DUPLICATE_CHECK = booleanPreferencesKey("enable_duplicate_check")
        val ENABLE_NOTIFICATIONS = booleanPreferencesKey("enable_notifications")
        val EXPORT_FORMAT = stringPreferencesKey("export_format")
        val AUTO_BACKUP = booleanPreferencesKey("auto_backup")
        val DAILY_REMINDER_TIME = stringPreferencesKey("daily_reminder_time")
        val GROUP_BY_CATEGORY = booleanPreferencesKey("group_by_category")
        val SHOW_DECIMAL_PLACES = intPreferencesKey("show_decimal_places")
    }

    // Get all preferences as Flow
    val userPreferencesFlow: Flow<UserPreferencesData> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            mapUserPreferences(preferences)
        }

    // Individual preference flows
    val isDarkTheme: Flow<Boolean> = dataStore.data
        .map { preferences -> preferences[PreferencesKeys.IS_DARK_THEME] ?: false }

    val lastUsedCategory: Flow<ExpenseCategory> = dataStore.data
        .map { preferences ->
            val categoryName = preferences[PreferencesKeys.LAST_USED_CATEGORY]
                ?: ExpenseCategory.STAFF.name
            ExpenseCategory.valueOf(categoryName)
        }

    val groupByCategory: Flow<Boolean> = dataStore.data
        .map { preferences -> preferences[PreferencesKeys.GROUP_BY_CATEGORY] ?: false }

    suspend fun setDarkTheme(isDark: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_DARK_THEME] = isDark
        }
    }

    suspend fun setLastUsedCategory(category: ExpenseCategory) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_USED_CATEGORY] = category.name
        }
    }

    suspend fun setDuplicateCheck(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.ENABLE_DUPLICATE_CHECK] = enabled
        }
    }

    suspend fun setGroupByCategory(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.GROUP_BY_CATEGORY] = enabled
        }
    }

    suspend fun setExportFormat(format: ExportExpensesUseCase.ExportFormat) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.EXPORT_FORMAT] = format.name
        }
    }

    suspend fun setNotifications(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.ENABLE_NOTIFICATIONS] = enabled
        }
    }

    suspend fun setAutoBackup(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTO_BACKUP] = enabled
        }
    }

    suspend fun setDailyReminderTime(time: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.DAILY_REMINDER_TIME] = time
        }
    }

    suspend fun setDecimalPlaces(places: Int) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SHOW_DECIMAL_PLACES] = places
        }
    }

    suspend fun setCurrency(currency: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEFAULT_CURRENCY] = currency
        }
    }

    // Reset all preferences
    suspend fun resetPreferences() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    private fun mapUserPreferences(preferences: Preferences): UserPreferencesData {
        val isDarkTheme = preferences[PreferencesKeys.IS_DARK_THEME] ?: false
        val defaultCurrency = preferences[PreferencesKeys.DEFAULT_CURRENCY] ?: "₹"
        val lastUsedCategoryName = preferences[PreferencesKeys.LAST_USED_CATEGORY]
            ?: ExpenseCategory.STAFF.name
        val enableDuplicateCheck = preferences[PreferencesKeys.ENABLE_DUPLICATE_CHECK] ?: true
        val enableNotifications = preferences[PreferencesKeys.ENABLE_NOTIFICATIONS] ?: true
        val exportFormatName = preferences[PreferencesKeys.EXPORT_FORMAT]
            ?: ExportExpensesUseCase.ExportFormat.CSV.name
        val autoBackup = preferences[PreferencesKeys.AUTO_BACKUP] ?: false
        val dailyReminderTime = preferences[PreferencesKeys.DAILY_REMINDER_TIME] ?: "18:00"
        val groupByCategory = preferences[PreferencesKeys.GROUP_BY_CATEGORY] ?: false
        val showDecimalPlaces = preferences[PreferencesKeys.SHOW_DECIMAL_PLACES] ?: 2

        return UserPreferencesData(
            isDarkTheme = isDarkTheme,
            defaultCurrency = defaultCurrency,
            lastUsedCategory = ExpenseCategory.valueOf(lastUsedCategoryName),
            enableDuplicateCheck = enableDuplicateCheck,
            enableNotifications = enableNotifications,
            exportFormat = ExportExpensesUseCase.ExportFormat.valueOf(exportFormatName),
            autoBackup = autoBackup,
            dailyReminderTime = dailyReminderTime,
            groupByCategory = groupByCategory,
            showDecimalPlaces = showDecimalPlaces
        )
    }
}