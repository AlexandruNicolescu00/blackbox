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
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.example.blackbox.R
import com.example.blackbox.data.app_usage.AppUsage
import com.example.blackbox.data.recorded_usage_stats.RecordedUsageStats
import com.example.blackbox.data.usage_stats_manager.AppUsageStatsManager
import com.example.blackbox.data.utility.REFRESH_INTERVAL
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

    private var recordId: Long = 0
    private var startedAt: Long? = null
    private var finishedAt: Long? = null
    private var record: RecordedUsageStats? = null
    private var seconds: Long = 0
    private var isAutoStart: Boolean = false
    private val currentUsageStats = ArrayDeque<AppUsage>()
    private val newElements = Stack<AppUsage>()
    private var getUsageStatsJob: Job? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        val notification = createNotification()
        ServiceCompat.startForeground(
            this,
            856,
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
                if (isAutoStart) {
                    startAutoCollection()
                } else {
                    startUsageStatsCollection()
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
            onDestroy()
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
            onDestroy()
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
    }

    override fun onDestroy() {
        super.onDestroy()
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
    }

    private suspend fun saveRecord() {
        val recordsNumber = appUsageRepository.countAppUsageByRecordingId(recordId)
        if (recordsNumber == 0) { // Don't save empty records
            recordedUsageStatsRepository.deleteRecordedUsageStats(record!!)
        } else {
            finishedAt = System.currentTimeMillis()
            record = record!!.copy(
                endedAt = finishedAt!!
            )
            recordedUsageStatsRepository.updateRecordedUsageStats(record!!)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotification(): Notification {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        )
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setOngoing(true)
            .setContentTitle("Usage Stats Service")
            .setContentText("Collecting usage stats...")
            .setSmallIcon(R.drawable.baseline_visibility_24)
            .build()
    }
}