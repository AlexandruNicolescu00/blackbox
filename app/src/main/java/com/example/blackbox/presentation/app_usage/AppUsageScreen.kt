package com.example.blackbox.presentation.app_usage

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.provider.Settings.ACTION_USAGE_ACCESS_SETTINGS
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.blackbox.R
import com.example.blackbox.common.dateFormat
import com.example.blackbox.presentation.app_usage.components.LogListItem
import com.example.blackbox.presentation.navigation.NavigationDestination
import com.example.blackbox.presentation.utility.AppUsageTextProvider
import com.example.blackbox.presentation.utility.PermissionCard
import java.text.SimpleDateFormat
import java.util.Locale

object LogListDestination : NavigationDestination {
    override val route = "app_usage"
    override val icon = Icons.Default.Visibility

    @Composable
    override fun getLabel(): String {
        return stringResource(id = R.string.app_usage_nav_label)
    }
}

@Composable
fun LogListScreen(
    modifier: Modifier = Modifier,
    viewModel: AppUsageViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val permissionsState = viewModel.permissionsManager.state.collectAsState()

    // Redirect to settings permission
    fun requestUsageStatsPermission(context: Context) {
        val intent = Intent(ACTION_USAGE_ACCESS_SETTINGS)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        if (state.startedAt != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = dimensionResource(R.dimen.padding_small))
            ) {
                Text(
                    text = "${stringResource(R.string.started_at)} ${dateFormat(state.startedAt!!)}",
                    color = MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.bodySmall
                )
                if (state.finishedAt != null) {
                    Text(
                        text = " - ${dateFormat(state.finishedAt!!)}",
                        color = MaterialTheme.colorScheme.secondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            HorizontalDivider(thickness = dimensionResource(R.dimen.thickness_small))
        }
        Row(
            modifier = Modifier
                .weight(10f)
        ) {
            if (permissionsState.value.hasUsageStatsPermission) {
                LifecycleResumeEffect(Unit) {
                    viewModel.onEvent(AppUsageEvent.OnResume)
                    onPauseOrDispose {
                        // Do something on pause or dispose effect
                    }
                }
                if (state.usageStats.isEmpty()) {
                    Column(
                        modifier = modifier
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = stringResource(id = R.string.no_usage_stats_found)
                        )
                    }
                }
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.space_small))
                ) {
                    items(state.usageStats) { usageStats ->
                        LogListItem(
                            packageName = usageStats.packageName,
                            lastTimeUsed = usageStats.lastTimeUsed
                        )
                    }
                }
            } else {
                PermissionCard(
                    activity = LocalContext.current as Activity,
                    permissionTextProvider = AppUsageTextProvider(),
                    isPermanentlyDeclined = {
                        shouldShowRequestPermissionRationale(
                            context as Activity, ACTION_USAGE_ACCESS_SETTINGS
                        )
                    },
                    onOkClick = {
                        requestUsageStatsPermission(context)
                    }
                )
            }
        }
        if (!state.isAutoStart) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Button(
                    onClick = {
                        if (state.isRecording) { viewModel.onEvent(AppUsageEvent.StopRecording) }
                        else { viewModel.onEvent(AppUsageEvent.StartRecording) }
                              },
                    modifier = Modifier
                        .weight(1f)
                ) {
                    Text(
                        text =
                            if (state.isRecording) stringResource(R.string.stop_recording)
                            else stringResource(R.string.start_recording)
                    )
                }
                Spacer(modifier = Modifier.width(dimensionResource(R.dimen.space_small)))
                Button(
                    onClick = { viewModel.onEvent(AppUsageEvent.SendLogs) },
                    modifier = Modifier
                        .weight(1f)
                ) {
                    Text(
                        text = stringResource(R.string.send_logs)
                    )
                }
            }
        }
    }
}