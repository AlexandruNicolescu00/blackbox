package com.example.blackbox.domain.repository

import com.example.blackbox.data.recorded_usage_stats.RecordedUsageStats
import kotlinx.coroutines.flow.Flow

interface RecordedUsageStatsRepository {
    fun getAllRecordedUsageStats(): Flow<List<RecordedUsageStats>>
    suspend fun getRecordedUsageStatsById(id: Long): RecordedUsageStats?
    suspend fun insertRecordedUsageStats(recordedUsageStats: RecordedUsageStats): Long
    suspend fun updateRecordedUsageStats(recordedUsageStats: RecordedUsageStats)
    suspend fun deleteRecordedUsageStats(recordedUsageStats: RecordedUsageStats)
}