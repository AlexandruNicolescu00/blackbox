package com.example.blackbox.presentation.records_log

import com.example.blackbox.data.recorded_usage_stats.RecordedUsageStats
import com.example.blackbox.domain.common.OrderType
import com.example.blackbox.domain.common.RecordsLogOrder

data class RecordsLogState(
    val records: List<RecordedUsageStats> = emptyList(),
    val order: RecordsLogOrder = RecordsLogOrder.Date(OrderType.Descending),
    val isOrderSectionVisible: Boolean = false
)