package com.example.blackbox.domain.repository

import com.example.blackbox.data.app_usage.AppUsage
import kotlinx.coroutines.flow.Flow

interface AppUsageRepository {
    fun getAppUsageByRecordingId(recordingId: Long): Flow<List<AppUsage>>
    fun getAppUsageByLastTimeUsed(lastTimeUsed: Long): Flow<List<AppUsage>>
    suspend fun insertAppUsage(appUsage: AppUsage)
    suspend fun countAppUsageByRecordingId(recordingId: Long): Int
}