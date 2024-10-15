package com.example.blackbox.presentation.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.navigation.NavHostController
import com.example.blackbox.R
import com.example.blackbox.presentation.app_usage.LogListDestination
import com.example.blackbox.presentation.home.HomeDestination
import com.example.blackbox.presentation.records_log.RecordsLogDestination

@Composable
fun NavigationBottomBar(
    navController: NavHostController,
    currentRoute: String?,
    modifier: Modifier = Modifier
) {
    val navBarDestinations = listOf(
        HomeDestination,
        LogListDestination,
        RecordsLogDestination
    )

    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.primary,
        tonalElevation = dimensionResource(R.dimen.tonal_elevation)
    ) {
        navBarDestinations.forEach { destination ->
            NavigationBarItem(
                selected = currentRoute == destination.route,
                label = { Text(text = destination.getLabel()) },
                icon = {
                        Icon(
                            imageVector = destination.icon,
                            contentDescription = destination.getLabel()
                        )
                },
                onClick = {
                    navController.navigate(destination.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    unselectedTextColor =  MaterialTheme.colorScheme.onSecondaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            )
        }
    }
}