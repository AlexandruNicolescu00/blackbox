package com.example.blackbox.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.example.blackbox.presentation.home.HomeDestination
import com.example.blackbox.presentation.app_usage.LogListDestination
import com.example.blackbox.presentation.record_detail.RecordDetailDestination
import com.example.blackbox.presentation.records_log.RecordsLogDestination
import com.example.blackbox.presentation.settings.SettingsDestination

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationTopBar(
    navController: NavHostController,
    currentRoute: String?,
    actions: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        modifier = modifier,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary
        ),
        actions = { actions() },
        title = {
            Text(
                text = getTitleForRoute(currentRoute),
                style = MaterialTheme.typography.titleLarge
            )
        },
        navigationIcon = {
            if (currentRoute == SettingsDestination.route || currentRoute?.startsWith(RecordDetailDestination.route) == true) {
                IconButton(
                    onClick = {
                        navController.popBackStack()
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = null
                    )
                }
            }
        }
    )
}

@Composable
fun getTitleForRoute(route: String?): String {
    val destinations = listOf(
        HomeDestination,
        LogListDestination,
        SettingsDestination,
        RecordsLogDestination,
        RecordDetailDestination
    )
    for (destination in destinations) {
        if (!route.isNullOrEmpty() && route.startsWith(destination.route)) {
            return destination.getLabel()
        }
    }
    return ""
}