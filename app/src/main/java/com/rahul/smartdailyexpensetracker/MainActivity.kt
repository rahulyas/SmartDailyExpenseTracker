package com.rahul.smartdailyexpensetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.rahul.smartdailyexpensetracker.data.preferences.UserPreferences
import com.rahul.smartdailyexpensetracker.data.preferences.UserPreferencesData
import com.rahul.smartdailyexpensetracker.presentation.navigation.ExpenseTrackerNavigation
import com.rahul.smartdailyexpensetracker.ui.theme.ExpenseTrackerTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var userPreferences: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val preferencesState by userPreferences.userPreferencesFlow.collectAsState(
                initial = UserPreferencesData()
            )

            ExpenseTrackerTheme(userPreferences = preferencesState) {
                ExpenseTrackerNavigation()
            }
        }
    }
}

