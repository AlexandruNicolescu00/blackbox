package com.example.blackbox.data.app_usage

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AppUsageDao {

    @Query("SELECT * FROM app_usage WHERE recordingId = :recordingId ORDER BY lastTimeUsed DESC")
    fun getAppUsageByRecordingId(recordingId: Long): Flow<List<AppUsage>>

    @Query("SELECT COUNT(*) FROM app_usage WHERE recordingId = :recordingId")
    fun countAppUsageByRecordingId(recordingId: Long): Int

    @Query("SELECT * FROM app_usage WHERE lastTimeUsed > :lastTimeUsed ORDER BY lastTimeUsed DESC")
    fun getAppUsageByLastTimeUsed(lastTimeUsed: Long): Flow<List<AppUsage>>

    @Insert
    suspend fun insertAppUsage(appUsage: AppUsage)
}