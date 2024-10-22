package com.example.blackbox.domain.use_case

import com.example.blackbox.data.recorded_usage_stats.RecordedUsageStats
import com.example.blackbox.domain.repository.RecordedUsageStatsRepository
import com.example.blackbox.domain.common.OrderType
import com.example.blackbox.domain.common.RecordsLogOrder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetRecords(
    private val repository: RecordedUsageStatsRepository
) {
    operator fun invoke(
        order: RecordsLogOrder = RecordsLogOrder.Date(OrderType.Descending)
    ): Flow<List<RecordedUsageStats>> {
        return repository.getAllRecordedUsageStats().map { records ->
            when(order.orderType) {
                is OrderType.Ascending -> {
                    when(order) {
                        is RecordsLogOrder.Date -> records.sortedBy { it.startedAt }
                    }
                }
                is OrderType.Descending -> {
                    when(order) {
                        is RecordsLogOrder.Date -> records.sortedByDescending { it.startedAt }
                    }
                }
            }
        }
    }
}