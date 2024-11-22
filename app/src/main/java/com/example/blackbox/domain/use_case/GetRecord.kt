package com.example.blackbox.domain.use_case

import com.example.blackbox.data.recorded_usage_stats.RecordedUsageStats
import com.example.blackbox.domain.repository.RecordedUsageStatsRepository

class GetRecord(
    private val repository: RecordedUsageStatsRepository
)  {
    suspend operator fun invoke(recordId: Long): RecordedUsageStats? {
        return repository.getRecordedUsageStatsById(recordId)
    }
}