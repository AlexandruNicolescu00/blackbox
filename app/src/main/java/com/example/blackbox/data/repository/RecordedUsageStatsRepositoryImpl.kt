package com.example.blackbox.data.repository

import com.example.blackbox.data.recorded_usage_stats.RecordedUsageStats
import com.example.blackbox.data.recorded_usage_stats.RecordedUsageStatsDao
import com.example.blackbox.domain.repository.RecordedUsageStatsRepository
import kotlinx.coroutines.flow.Flow

class RecordedUsageStatsRepositoryImpl(
    private val recordedUsageStatsDao: RecordedUsageStatsDao
) : RecordedUsageStatsRepository {

    override suspend fun getRecordedUsageStatsById(id: Int): RecordedUsageStats? {
        return recordedUsageStatsDao.getRecordedUsageStatsById(id)
    }

    override fun getAllRecordedUsageStats(): Flow<List<RecordedUsageStats>> {
        return recordedUsageStatsDao.getAllRecordedUsageStats()
    }

    override suspend fun insertRecordedUsageStats(recordedUsageStats: RecordedUsageStats): Long {
        return recordedUsageStatsDao.insertRecordedUsageStats(recordedUsageStats)
    }

    override suspend fun updateRecordedUsageStats(recordedUsageStats: RecordedUsageStats) {
        recordedUsageStatsDao.updateRecordedUsageStats(recordedUsageStats)
    }

    override suspend fun deleteRecordedUsageStats(recordedUsageStats: RecordedUsageStats) {
        recordedUsageStatsDao.deleteRecordedUsageStats(recordedUsageStats)
    }
}