package com.example.blackbox.presentation.records_log

import com.example.blackbox.data.recorded_usage_stats.RecordedUsageStats
import com.example.blackbox.domain.util.OrderType
import com.example.blackbox.domain.util.RecordsLogOrder

data class RecordsLogState(
    val records: List<RecordedUsageStats> = emptyList(),
    val order: RecordsLogOrder = RecordsLogOrder.Date(OrderType.Descending),
    val isOrderSectionVisible: Boolean = false
)