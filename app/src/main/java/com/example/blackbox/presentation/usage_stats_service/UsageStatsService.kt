package com.example.blackbox.presentation.usage_stats_service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.app.usage.UsageStats
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.example.blackbox.R
import com.example.blackbox.data.app_usage.AppUsage
import com.example.blackbox.data.repository.RecordingState
import com.example.blackbox.data.recorded_usage_stats.RecordedUsageStats
import com.example.blackbox.data.manager.AppUsageStatsManager
import com.example.blackbox.common.REFRESH_INTERVAL
import com.example.blackbox.domain.repository.AppUsageRepository
import com.example.blackbox.domain.repository.RecordedUsageStatsRepository
import com.example.blackbox.domain.repository.UserPreferencesRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Stack
import javax.inject.Inject

private const val CHANNEL_ID = "UsageStatsService"
private const val CHANNEL_NAME = "Usage Stats Service"

@AndroidEntryPoint
class UsageStatsService : Service() {

    @Inject
    lateinit var usageStatsManager: AppUsageStatsManager
    @Inject
    lateinit var appUsageRepository: AppUsageRepository
    @Inject
    lateinit var recordedUsageStatsRepository: RecordedUsageStatsRepository
    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    private var lastWorkMode: Boolean? = null
    private var recordId: Long = 0
    private var startedAt: Long? = null
    private var finishedAt: Long? = null
    private var record: RecordedUsageStats? = null
    private var seconds: Long = 0
    private var isAutoStart: Boolean = false
    private val currentUsageStats = ArrayDeque<AppUsage>()
    private var getUsageStatsJob: Job? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Log.d("UsageStatsService", "Service started and $lastWorkMode")
        val notification = createNotification()
        ServiceCompat.startForeground(
            this,
            1,
            notification,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            } else {
                0
            }
        )

        serviceScope.launch {
            userPreferencesRepository.getSecondsToSendFlow.collect { secondsToSend ->
                seconds = secondsToSend
            }
        }
        serviceScope.launch {
            userPreferencesRepository.isAutoStartFlow.collect { isAutoStartFlow ->
                isAutoStart = isAutoStartFlow
                if (lastWorkMode != null && lastWorkMode != isAutoStart) { // if toggle automatic mode stop recording
                    stopSelf()
                }
                lastWorkMode = isAutoStart
                if (getUsageStatsJob == null) {
                    if (isAutoStart) {
                        startAutoCollection()
                    } else {
                        startUsageStatsCollection()
                    }
                }
            }
        }
        return START_STICKY
    }

    private fun startAutoCollection() {
        getUsageStatsJob?.cancel()
        getUsageStatsJob = serviceScope.launch {
            while (isAutoStart) {
                createRecord()
                var i = 0
                while (i < seconds / REFRESH_INTERVAL && isAutoStart) {
                    requestUsageStats()
                    delay(REFRESH_INTERVAL * 1000)
                    i += 1
                }
                saveRecord()
            }
            stopSelf()
        }
    }

    private fun startUsageStatsCollection() {
        getUsageStatsJob?.cancel()
        getUsageStatsJob = serviceScope.launch {
            createRecord()
            while (true) {
                requestUsageStats()
                delay(REFRESH_INTERVAL * 1000)
            }
        }
    }

    private suspend fun requestUsageStats() {
        val end = System.currentTimeMillis()
        val begin = startedAt!!
        val usageStats: List<UsageStats> = try {
            usageStatsManager.getUsageStats(begin, end)
                .filter { !it.packageName.contains("launcher") }
                .sortedByDescending { it.lastTimeUsed }
        } catch (e: SecurityException) {
            if (isAutoStart) {
                userPreferencesRepository.toggleAutoStartMode()
            }
            stopSelf()
            emptyList()
        }

        val filteredUsageStats = usageStats.map { usageStat ->
            AppUsage(
                packageName = usageStat.packageName,
                lastTimeUsed = usageStat.lastTimeUsed,
                recordingId = recordId
            )
        }

        var j = 0
        val newElements = Stack<AppUsage>()

        while (filteredUsageStats.size > j && currentUsageStats.firstOrNull()?.packageName != filteredUsageStats[j].packageName) {
            newElements.push(filteredUsageStats[j])
            j += 1
        }

        if (newElements.isNotEmpty()) {
            while (newElements.isNotEmpty()) {
                val newElement = newElements.pop()
                currentUsageStats.addFirst(newElement)
                appUsageRepository.insertAppUsage(newElement)
            }
        }
        updateRecordingState(isRecording = true)
    }

    private fun updateRecordingState(isRecording: Boolean) {
        val scope = CoroutineScope(Dispatchers.Main)
        scope.launch {
            appUsageRepository.setRecordingState(
                RecordingState(
                    appUsages = currentUsageStats.toList(),
                    isRecording = isRecording,
                    startedAt = startedAt,
                    finishedAt = finishedAt
                )
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("UsageStatsService", "Service destroyed")
        lastWorkMode = null
        serviceScope.launch {
            saveRecord()
            serviceJob.cancel()
        }
    }

    private suspend fun createRecord() {
        startedAt = System.currentTimeMillis()
        record = RecordedUsageStats(
            startedAt = startedAt!!
        )
        recordId = recordedUsageStatsRepository.insertRecordedUsageStats(record!!)
        record = record!!.copy(
            id = recordId
        )
        currentUsageStats.clear()
    }

    private suspend fun saveRecord() {
        finishedAt = System.currentTimeMillis()
        updateRecordingState(isRecording = false)
        val recordsNumber = appUsageRepository.countAppUsageByRecordingId(recordId)
        if (recordsNumber == 0) { // Don't save empty records
            recordedUsageStatsRepository.deleteRecordedUsageStats(record!!)
        } else {
            record = record!!.copy(
                endedAt = finishedAt!!
            )
            recordedUsageStatsRepository.updateRecordedUsageStats(record!!)
        }
        finishedAt = null

    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }



}