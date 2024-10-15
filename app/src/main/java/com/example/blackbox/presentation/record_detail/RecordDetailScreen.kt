package com.example.blackbox.presentation.record_detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.blackbox.R
import com.example.blackbox.presentation.app_usage.components.LogListItem
import com.example.blackbox.presentation.navigation.NavigationDestination

object RecordDetailDestination : NavigationDestination {
    override val route = "record_detail"
    override val icon = Icons.AutoMirrored.Default.ExitToApp

    @Composable
    override fun getLabel(): String {
        return stringResource(id = R.string.record_detail_nav_label)
    }
}

@Composable
fun RecordDetailScreen(
    modifier: Modifier = Modifier,
    viewModel: RecordDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

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
}