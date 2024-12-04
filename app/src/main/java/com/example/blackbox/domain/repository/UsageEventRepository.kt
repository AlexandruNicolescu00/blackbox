package com.example.blackbox.domain.repository

import com.example.blackbox.data.usage_event.UsageEvent
import kotlinx.coroutines.flow.Flow

interface UsageEventRepository {
    fun getUsageEventByRecordingId(recordingId: Long): Flow<List<UsageEvent>>
    fun getUsageEventByTimestamp(timestamp: Long): Flow<List<UsageEvent>>
    suspend fun insertUsageEvent(usageEvent: UsageEvent)
    suspend fun countUsageEventByRecordingId(recordingId: Long): Int
}