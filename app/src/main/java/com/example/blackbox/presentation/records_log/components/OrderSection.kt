package com.example.blackbox.presentation.records_log.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.example.blackbox.R
import com.example.blackbox.domain.util.OrderType
import com.example.blackbox.domain.util.RecordsLogOrder

@Composable
fun OrderSection(
    modifier: Modifier = Modifier,
    order: RecordsLogOrder = RecordsLogOrder.Date(OrderType.Descending),
    onOrderChange: (RecordsLogOrder) -> Unit
) {
    Column(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            DefaultRadioButton(
                text = stringResource(R.string.date),
                selected = order is RecordsLogOrder.Date,
                onSelect = { onOrderChange(RecordsLogOrder.Date(order.orderType)) }
            )
        }
        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.space_medium)))
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            DefaultRadioButton(
                text = stringResource(R.string.ascending),
                selected = order.orderType is OrderType.Ascending,
                onSelect = { onOrderChange(order.copy(OrderType.Ascending)) }
            )
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.space_small)))
            DefaultRadioButton(
                text = stringResource(R.string.descending),
                selected = order.orderType is OrderType.Descending,
                onSelect = { onOrderChange(order.copy(OrderType.Descending)) }
            )
        }
    }
}