package com.example.blackbox.presentation.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.blackbox.presentation.home.HomeDestination
import com.example.blackbox.presentation.home.HomeScreen
import com.example.blackbox.presentation.app_usage.LogListDestination
import com.example.blackbox.presentation.app_usage.LogListScreen
import com.example.blackbox.presentation.record_detail.RecordDetailDestination
import com.example.blackbox.presentation.record_detail.RecordDetailScreen
import com.example.blackbox.presentation.records_log.RecordsLogDestination
import com.example.blackbox.presentation.records_log.RecordsLogScreen
import com.example.blackbox.presentation.records_log.RecordsLogViewModel
import com.example.blackbox.presentation.settings.SettingsDestination
import com.example.blackbox.presentation.settings.SettingsScreen

@Composable
fun AppNavHost(
    recordsLogViewModel: RecordsLogViewModel,
    navController: NavHostController,
    snackBarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = HomeDestination.route,
        modifier = modifier
    ) {
        composable(route = HomeDestination.route) {
            HomeScreen(
                snackBarHostState = snackBarHostState,
                onClick = {
                    navController.navigate(LogListDestination.route + "?isRecording=true") {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
        composable(route = LogListDestination.route) {
            LogListScreen()
        }
        composable(route = SettingsDestination.route) {
            SettingsScreen()
        }
        composable(route = RecordsLogDestination.route) {
            RecordsLogScreen(
                navController = navController,
                viewModel = recordsLogViewModel
            )
        }
        composable(
            route = RecordDetailDestination.route + "?recordId={recordId}",
            arguments = listOf(
                navArgument("recordId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) {
            RecordDetailScreen()
        }
    }
}