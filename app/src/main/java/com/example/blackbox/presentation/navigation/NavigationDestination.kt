package com.example.blackbox.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

interface NavigationDestination {
    val route: String
    val icon: ImageVector

    @Composable
    fun getLabel(): String
}