package com.example.periodtracker.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.periodtracker.PeriodTrackerApp
import com.example.periodtracker.ui.screens.dashboard.DashboardScreen
import com.example.periodtracker.ui.screens.dashboard.DashboardViewModel
import com.example.periodtracker.ui.screens.history.HistoryScreen
import com.example.periodtracker.ui.screens.history.HistoryViewModel

object Routes {
    const val DASHBOARD = "dashboard"
    const val HISTORY = "history"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val app = context.applicationContext as PeriodTrackerApp
    val database = app.database

    NavHost(navController = navController, startDestination = Routes.DASHBOARD) {
        composable(Routes.DASHBOARD) {
            val viewModel: DashboardViewModel = viewModel(
                factory = DashboardViewModel.Factory(database.cycleDao())
            )
            DashboardScreen(viewModel = viewModel, onNavigateToHistory = {
                navController.navigate(Routes.HISTORY)
            })
        }
        composable(Routes.HISTORY) {
            val viewModel: HistoryViewModel = viewModel(
                factory = HistoryViewModel.Factory(database.cycleDao())
            )
            HistoryScreen(viewModel = viewModel, onNavigateBack = {
                navController.popBackStack()
            })
        }
    }
}