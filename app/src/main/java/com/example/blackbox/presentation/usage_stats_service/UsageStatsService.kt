package com.example.blackbox.presentation.usage_stats_service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStats
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.example.blackbox.R
import com.example.blackbox.common.NOTIFICATION_EVENT
import com.example.blackbox.common.REFRESH_INTERVAL
import com.example.blackbox.common.RecordingState
import com.example.blackbox.common.SharedData
import com.example.blackbox.data.app_usage.AppUsage
import com.example.blackbox.data.manager.AppUsageStatsManager
import com.example.blackbox.data.recorded_usage_stats.RecordedUsageStats
import com.example.blackbox.data.usage_event.UsageEvent
import com.example.blackbox.domain.repository.AppUsageRepository
import com.example.blackbox.domain.repository.RecordedUsageStatsRepository
import com.example.blackbox.domain.repository.UsageEventRepository
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
    lateinit var usageEventRepository: UsageEventRepository
    @Inject
    lateinit var recordedUsageStatsRepository: RecordedUsageStatsRepository
    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository

    @Inject
    lateinit var sharedData: SharedData

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
    private val currentUsageEvents = ArrayDeque<UsageEvent>()
    private var getUsageStatsJob: Job? = null
    private var lastEndTime: Long? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

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
                        startUsageEventsCollection()
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
                    requestEvents()
                    delay(REFRESH_INTERVAL * 1000)
                    i += 1
                }
                saveRecord()
            }
            stopSelf()
        }
    }

    private fun startUsageEventsCollection() {
        getUsageStatsJob?.cancel()
        getUsageStatsJob = serviceScope.launch {
            createRecord()
            while (true) {
                requestEvents()
                delay(REFRESH_INTERVAL * 1000)
            }
        }
    }

    val lookingEvents = listOf<Int>(
        UsageEvents.Event.ACTIVITY_RESUMED,
        UsageEvents.Event.FOREGROUND_SERVICE_START,
        UsageEvents.Event.FOREGROUND_SERVICE_STOP,
        NOTIFICATION_EVENT,
    )

    private suspend fun requestEvents() {
        val begin = lastEndTime ?: startedAt!!
        val end = System.currentTimeMillis()
        lastEndTime = end
        var events: List<UsageEvents.Event> = try {
            usageStatsManager.getUserEvents(begin, end) // Need to filter here because in manager event.packageName is null
                .filter { it.packageName != this.packageName && !it.packageName.contains("launcher") &&  it.eventType in lookingEvents }
        } catch (_: SecurityException) {
            if (isAutoStart) {
                userPreferencesRepository.toggleAutoStartMode()
            }
            stopSelf()
            emptyList()
        }
        events = events.distinctBy { Pair(it.timeStamp, it.eventType) }

        for (event in events) {
            val packageName = event.packageName
            val timestamp = event.timeStamp
            var eventType = ""

            when(event.eventType) {
                UsageEvents.Event.ACTIVITY_RESUMED -> {
                    eventType = "APP CHANGED"
                }
                UsageEvents.Event.FOREGROUND_SERVICE_START -> {
                    eventType = "FOREGROUND SERVICE START"
                }
                UsageEvents.Event.FOREGROUND_SERVICE_STOP -> {
                    eventType = "FOREGROUND SERVICE STOP"
                }
                NOTIFICATION_EVENT -> {
                    eventType = "NOTIFICATION ARRIVED"
                }
            }

            val usageEvent = UsageEvent(
                packageName = packageName,
                timestamp = timestamp,
                eventType = eventType,
                recordingId = recordId
            )
            usageEventRepository.insertUsageEvent(usageEvent)
            currentUsageEvents.add(usageEvent)
            updateRecordingState(true)
        }
    }

    private suspend fun requestUsageStats() {
        val begin = startedAt!!
        val end = System.currentTimeMillis()
        lastEndTime = end
        val usageStats: List<UsageStats> = try {
            usageStatsManager.getUsageStats(begin, end)
                .filter { it.packageName != this.packageName && !it.packageName.contains("launcher") }
                .sortedByDescending { it.lastTimeUsed }
        } catch (_: SecurityException) {
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
            updateRecordingState(isRecording = true)
        }
    }

    private fun updateRecordingState(isRecording: Boolean) {
        serviceScope.launch {
            sharedData.recordingState.postValue(
                RecordingState(
                    id = recordId,
                    appUsages = currentUsageStats.toList(),
                    usageEvents = currentUsageEvents.toList(),
                    isRecording = isRecording,
                    startedAt = startedAt,
                    finishedAt = finishedAt
                )
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
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
        currentUsageEvents.clear()
        updateRecordingState(isRecording = true)
    }

    private suspend fun saveRecord() {
        finishedAt = System.currentTimeMillis()
        updateRecordingState(isRecording = false)
        var recordsNumber = appUsageRepository.countAppUsageByRecordingId(recordId)
        recordsNumber += usageEventRepository.countUsageEventByRecordingId(recordId)
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

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}