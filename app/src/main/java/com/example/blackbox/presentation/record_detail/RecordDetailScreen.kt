package com.example.blackbox.presentation.record_detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
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
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.space_small))
        ) {
            items(state.usageStats) { usageStats ->
                LogListItem(
                    packageName = usageStats.packageName,
                    lastTimeUsed = usageStats.lastTimeUsed
                )
            }
        }
        if (state.record?.blockId != null) {
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(dimensionResource(R.dimen.space_small))
            )
            Text(
                text = "Block: ${state.record!!.blockId}",
                modifier = Modifier
                    .fillMaxWidth()
            )
            Button(
                onClick = { viewModel.onEvent(RecordDetailEvent.ViewInExplorer) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = dimensionResource(R.dimen.space_medium),
                        vertical = dimensionResource(R.dimen.space_small)
                    )
            ) {
                Text(text = stringResource(R.string.view_in_explorer))
            }
        }
    }

}