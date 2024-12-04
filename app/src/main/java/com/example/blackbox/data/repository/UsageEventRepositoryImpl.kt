package com.example.blackbox.data.repository

import com.example.blackbox.data.usage_event.UsageEvent
import com.example.blackbox.data.usage_event.UsageEventDao
import com.example.blackbox.domain.repository.UsageEventRepository
import kotlinx.coroutines.flow.Flow

class UsageEventRepositoryImpl(
    private val usageEventDao: UsageEventDao
) : UsageEventRepository {
    override fun getUsageEventByRecordingId(recordingId: Long): Flow<List<UsageEvent>> {
        return usageEventDao.getUsageEventByRecordingId(recordingId)
    }

    override fun getUsageEventByTimestamp(timestamp: Long): Flow<List<UsageEvent>> {
        return usageEventDao.getUsageEventByTimestamp(timestamp)
    }

    override suspend fun insertUsageEvent(usageEvent: UsageEvent) {
        return usageEventDao.insertUsageEvent(usageEvent)
    }

    override suspend fun countUsageEventByRecordingId(recordingId: Long): Int {
        return usageEventDao.countUsageEventByRecordingId(recordingId)
    }

}