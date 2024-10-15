package com.example.blackbox.data.recorded_usage_stats

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordedUsageStatsDao {

    @Query("SELECT * FROM recorded_usage_stats WHERE id = :id")
    suspend fun getRecordedUsageStatsById(id: Int): RecordedUsageStats?

    @Query("SELECT * FROM recorded_usage_stats WHERE endedAt IS NOT NULL")
    fun getAllRecordedUsageStats(): Flow<List<RecordedUsageStats>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertRecordedUsageStats(recordedUsageStats: RecordedUsageStats) : Long

    @Update
    suspend fun updateRecordedUsageStats(recordedUsageStats: RecordedUsageStats)

    @Delete
    suspend fun deleteRecordedUsageStats(recordedUsageStats: RecordedUsageStats)

}