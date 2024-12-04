package com.example.blackbox.presentation.home

import android.Manifest
import android.app.Activity
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.blackbox.R
import com.example.blackbox.domain.use_case.EvaluationEventsNonce
import com.example.blackbox.presentation.navigation.NavigationDestination
import com.example.blackbox.presentation.utility.NotificationsTextProvider
import com.example.blackbox.presentation.utility.PermissionCard

object HomeDestination : NavigationDestination {
    override val route = "home"
    override val icon = Icons.Filled.Home

    @Composable
    override fun getLabel(): String {
        return stringResource(id = R.string.home_nav_label)
    }
}

@Composable
fun HomeScreen(
    snackBarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
    onClick: () -> Unit
) {
    val state = viewModel.state.value

    val permissionState = viewModel.permissionsManager.state.collectAsState()
    val context = LocalContext.current as Activity

    val requestActivityRecognitionPermission =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.app_welcome),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.space_medium)))
        Text(
            text = stringResource(R.string.app_description),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.space_medium)))
        Text(
            text = stringResource(R.string.app_instruction),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.space_medium)))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.manual_recording),
                style = MaterialTheme.typography.bodyLarge,
                color = if (!state.isAutoStart) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = state.isAutoStart,
                onCheckedChange = {
                    viewModel.onEvent(HomeEvent.ToggleAutoStart)
                },
                modifier = Modifier.weight(2f)
            )
            Text(
                text = stringResource(R.string.automatic_recording),
                style = MaterialTheme.typography.bodyLarge,
                color = if (state.isAutoStart) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
        }
        if (state.isAutoStart) {
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.space_medium)))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.time_to_send),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(2f)
                )
                TextField(
                    value = "${state.secondsToSend}",
                    onValueChange = {
                        val seconds = if (it.isEmpty()) 1 else it.toLongOrNull()
                        if (seconds != null && seconds > 0) {
                            viewModel.onEvent(HomeEvent.SetSecondsToSend(seconds))
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    suffix = {
                        Text("sec")
                    },
                    textStyle = TextStyle(
                        textAlign = TextAlign.Center,
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
        } else {
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.space_medium)))
            Button(
                onClick = {
                    onClick()
                },
            ) {
                Text(text = stringResource(R.string.go_to_app_usage))
            }
        }
        if (!permissionState.value.hasNotificationPermission) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val permission = Manifest.permission.POST_NOTIFICATIONS
                PermissionCard(
                    activity = context,
                    permissionTextProvider = NotificationsTextProvider(),
                    isPermanentlyDeclined = {
                        shouldShowRequestPermissionRationale(
                            context,
                            permission
                        )
                    },
                    onOkClick = {
                        requestActivityRecognitionPermission.launch(permission)
                    },
                )
            }
        }
    }
    LaunchedEffect(Unit) {
        viewModel.messageChannel.collect { message ->
            snackBarHostState.showSnackbar(
                message = message
            )
        }
    }
}