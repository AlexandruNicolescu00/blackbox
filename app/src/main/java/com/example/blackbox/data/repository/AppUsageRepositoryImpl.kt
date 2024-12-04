package com.example.blackbox.data.repository

import androidx.lifecycle.MutableLiveData
import com.example.blackbox.common.RecordingState
import com.example.blackbox.data.app_usage.AppUsage
import com.example.blackbox.data.app_usage.AppUsageDao
import com.example.blackbox.domain.repository.AppUsageRepository
import kotlinx.coroutines.flow.Flow

class AppUsageRepositoryImpl(
    private val appUsageDao: AppUsageDao
) : AppUsageRepository {
    override fun getAppUsageByRecordingId(recordingId: Long): Flow<List<AppUsage>> {
        return appUsageDao.getAppUsageByRecordingId(recordingId)
    }

    override fun getAppUsageByLastTimeUsed(lastTimeUsed: Long): Flow<List<AppUsage>> {
        return appUsageDao.getAppUsageByLastTimeUsed(lastTimeUsed)
    }

    override suspend fun insertAppUsage(appUsage: AppUsage) {
        appUsageDao.insertAppUsage(appUsage)
    }

    override suspend fun countAppUsageByRecordingId(recordingId: Long): Int {
        return appUsageDao.countAppUsageByRecordingId(recordingId)
    }
}