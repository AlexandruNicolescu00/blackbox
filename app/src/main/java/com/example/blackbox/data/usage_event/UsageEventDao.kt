package com.example.blackbox.data.usage_event

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UsageEventDao {
    @Query("SELECT * FROM usage_event WHERE recordingId = :recordingId ORDER BY timestamp DESC")
    fun getUsageEventByRecordingId(recordingId: Long): Flow<List<UsageEvent>>

    @Query("SELECT COUNT(*) FROM usage_event WHERE recordingId = :recordingId")
    fun countUsageEventByRecordingId(recordingId: Long): Int

    @Query("SELECT * FROM usage_event WHERE timestamp > :timestamp ORDER BY timestamp DESC")
    fun getUsageEventByTimestamp(timestamp: Long): Flow<List<UsageEvent>>

    @Insert
    suspend fun insertUsageEvent(usageEvent: UsageEvent)
}