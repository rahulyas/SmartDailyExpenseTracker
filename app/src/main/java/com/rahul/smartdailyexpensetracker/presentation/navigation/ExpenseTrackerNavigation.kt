package com.rahul.smartdailyexpensetracker.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.rahul.smartdailyexpensetracker.presentation.screens.ExpenseDetailsScreen
import com.rahul.smartdailyexpensetracker.presentation.screens.ExpenseEntryScreen
import com.rahul.smartdailyexpensetracker.presentation.screens.ExpenseListScreen
import com.rahul.smartdailyexpensetracker.presentation.screens.ExpenseReportScreen
import com.rahul.smartdailyexpensetracker.presentation.screens.SettingsScreen

@Composable
fun ExpenseTrackerNavigation(
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = ExpenseScreen.Entry.route
    ) {
        composable(ExpenseScreen.Entry.route) {
            ExpenseEntryScreen(
                onNavigateToList = {
                    navController.navigate(ExpenseScreen.List.route)
                },
                onNavigateToSettings = {
                    navController.navigate(ExpenseScreen.Settings.route)
                }
            )
        }

        composable(ExpenseScreen.List.route) {
            ExpenseListScreen(
                onNavigateToReports = {
                    navController.navigate(ExpenseScreen.Reports.route)
                },
                onNavigateBack = {
                    navController.popBackStack()
                },
                navController = navController
            )
        }

        composable(ExpenseScreen.Reports.route) {
            ExpenseReportScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(ExpenseScreen.Settings.route) {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = ExpenseScreen.Details.route,
            arguments = listOf(navArgument("expenseId") { type = NavType.StringType })
        ) { backStackEntry ->
            val expenseId = backStackEntry.arguments?.getString("expenseId") ?: return@composable
            ExpenseDetailsScreen(
                expenseId = expenseId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
