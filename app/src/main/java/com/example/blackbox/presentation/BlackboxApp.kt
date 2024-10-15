package com.example.blackbox.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.blackbox.R
import com.example.blackbox.presentation.navigation.AppNavHost
import com.example.blackbox.presentation.navigation.NavigationBottomBar
import com.example.blackbox.presentation.navigation.NavigationTopBar
import com.example.blackbox.presentation.records_log.RecordsLogDestination
import com.example.blackbox.presentation.records_log.RecordsLogEvent
import com.example.blackbox.presentation.records_log.RecordsLogViewModel
import com.example.blackbox.presentation.settings.SettingsDestination

@Composable
fun BlackboxApp(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    snackBarHostState: SnackbarHostState = remember { SnackbarHostState() },
    recordsLogViewModel: RecordsLogViewModel = hiltViewModel()
) {
    // Get current back stack entry
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        topBar = {
            NavigationTopBar(
                navController = navController,
                currentRoute = currentRoute,
                actions = {
                    if (currentRoute != SettingsDestination.route) {
                        IconButton(
                            onClick = {
                                navController.navigate(SettingsDestination.route)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = stringResource(id = R.string.settings_nav_label),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        if (currentRoute == RecordsLogDestination.route) {
                            IconButton(
                                onClick = {
                                    recordsLogViewModel.onEvent(RecordsLogEvent.ToggleOrderSection)
                                }
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.baseline_sort_24),
                                    contentDescription = stringResource(id = R.string.sort),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(snackBarHostState) {
                Snackbar(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    snackbarData = it
                )
            }
        },
        bottomBar = {
            if (currentRoute != SettingsDestination.route) {
                NavigationBottomBar(
                    navController = navController,
                    currentRoute = currentRoute
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .padding(innerPadding)
        ) {
            AppNavHost(
                navController = navController,
                snackBarHostState = snackBarHostState,
                modifier = modifier.padding(dimensionResource(R.dimen.padding_medium)),
                recordsLogViewModel = recordsLogViewModel
            )
        }
    }
}