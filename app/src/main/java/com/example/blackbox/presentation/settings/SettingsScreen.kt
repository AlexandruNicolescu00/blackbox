package com.example.blackbox.presentation.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.blackbox.R
import com.example.blackbox.presentation.navigation.NavigationDestination

object SettingsDestination : NavigationDestination {
    override val route = "settings"
    override val icon = Icons.Default.Settings

    @Composable
    override fun getLabel(): String {
        return stringResource(id = R.string.settings_nav_label)
    }
}

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state = viewModel.state.value

    Column(
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recording in background",
                modifier = Modifier.weight(1f)
            )

            Switch(
                checked = state.isBackground,
                onCheckedChange = {
                    viewModel.onEvent(SettingsEvent.ToggleBackgroundMode)
                }
            )
        }
    }
}