package com.example.blackbox.domain.repository

import androidx.lifecycle.MutableLiveData
import com.example.blackbox.data.app_usage.AppUsage
import com.example.blackbox.data.repository.RecordingState
import kotlinx.coroutines.flow.Flow

interface AppUsageRepository {
    val recordingState : MutableLiveData<RecordingState>
    fun setRecordingState(state: RecordingState)
    fun getAppUsageByRecordingId(recordingId: Long): Flow<List<AppUsage>>
    fun getAppUsageByLastTimeUsed(lastTimeUsed: Long): Flow<List<AppUsage>>
    suspend fun insertAppUsage(appUsage: AppUsage)
    suspend fun countAppUsageByRecordingId(recordingId: Long): Int
}