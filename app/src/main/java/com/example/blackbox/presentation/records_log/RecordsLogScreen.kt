package com.example.blackbox.presentation.records_log

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.blackbox.R
import com.example.blackbox.presentation.navigation.NavigationDestination
import com.example.blackbox.presentation.record_detail.RecordDetailDestination
import com.example.blackbox.presentation.records_log.components.OrderSection
import com.example.blackbox.presentation.records_log.components.RecordsLogItem
import java.text.SimpleDateFormat
import java.util.Locale

object RecordsLogDestination : NavigationDestination {
    override val route = "records"
    override val icon = Icons.AutoMirrored.Default.List

    @Composable
    override fun getLabel(): String {
        return stringResource(R.string.records_nav_label)
    }
}

@Composable
fun RecordsLogScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    viewModel: RecordsLogViewModel = hiltViewModel()
) {
    val state = viewModel.state.collectAsState()

    val groupedRecords = state.value.records.groupBy {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it.startedAt)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            AnimatedVisibility(
                visible = state.value.isOrderSectionVisible,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                OrderSection(
                    modifier = Modifier
                        .fillMaxWidth(),
                    order = state.value.order,
                    onOrderChange = { viewModel.onEvent(RecordsLogEvent.Order(it)) }
                )
            }
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))
        ) {
            groupedRecords.forEach { (date, records) ->
                item {
                    Text(
                        text = date,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(
                            start = dimensionResource(id = R.dimen.padding_medium),
                            end = dimensionResource(id = R.dimen.padding_small),
                            top = dimensionResource(R.dimen.padding_small)
                        )
                    )
                }

                items(records) { record ->
                    RecordsLogItem(
                        record = record,
                        onClick = {
                            navController.navigate(
                                RecordDetailDestination.route + "?recordId=${record.id}"
                            )
                        }
                    )
                }
            }
        }
    }
}