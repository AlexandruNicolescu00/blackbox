package com.example.blackbox.data.manager

import android.app.usage.UsageEvents
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.util.Log

class AppUsageStatsManager(
    private val context: Context,
    permissionsManager: PermissionsManager
) {
    private val permissionsState = permissionsManager.state

    private var usageStatsManager : UsageStatsManager? = null

    private fun getUsageStatsManager(): UsageStatsManager {
        return context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    }

    fun getUsageStats(beginTime: Long, endTime: Long): List<UsageStats> {
        if (!permissionsState.value.hasUsageStatsPermission) {
            throw SecurityException("Usage stats permission not granted ${permissionsState.value.hasUsageStatsPermission}")
        }
        if (usageStatsManager == null) {
            usageStatsManager = getUsageStatsManager()
        }
        val stats = usageStatsManager!!.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, beginTime, endTime).filter{it.lastTimeVisible >= beginTime}

        return stats
    }

    fun getUserEvents(beginTime: Long, endTime: Long): List<UsageEvents.Event> {
        if (!permissionsState.value.hasUsageStatsPermission) {
            throw SecurityException("Usage stats permission not granted ${permissionsState.value.hasUsageStatsPermission}")
        }
        if (usageStatsManager == null) {
            usageStatsManager = getUsageStatsManager()
        }
        val usageEvents = usageStatsManager!!.queryEvents(beginTime, endTime)
        val events = mutableListOf<UsageEvents.Event>()

        if (usageEvents != null) {
            while (usageEvents.hasNextEvent()) {
                val eventAux = UsageEvents.Event()
                if (eventAux.packageName != context.packageName) {
                    events.add(eventAux)
                }
                usageEvents.getNextEvent(eventAux)
            }
        }

        return events
    }

    fun getEventName(eventType: Int): String {
        when(eventType) {
            UsageEvents.Event.NONE -> return "NONE"
            UsageEvents.Event.ACTIVITY_RESUMED -> return "ACTIVITY_RESUMED"
            UsageEvents.Event.ACTIVITY_PAUSED -> return "ACTIVITY_PAUSED"
            UsageEvents.Event.CONFIGURATION_CHANGE -> return "CONFIGURATION_CHANGE"
            UsageEvents.Event.USER_INTERACTION -> return "USER_INTERACTION"
            UsageEvents.Event.SHORTCUT_INVOCATION -> return "SHORTCUT_INVOCATION"
            UsageEvents.Event.STANDBY_BUCKET_CHANGED -> return "STANDBY_BUCKET_CHANGED"
            UsageEvents.Event.SCREEN_INTERACTIVE -> return "SCREEN_INTERACTIVE"
            UsageEvents.Event.SCREEN_NON_INTERACTIVE -> return "SCREEN_NON_INTERACTIVE"
            UsageEvents.Event.KEYGUARD_SHOWN -> return "KEYGUARD_SHOWN"
            UsageEvents.Event.KEYGUARD_HIDDEN -> return "KEYGUARD_HIDDEN"
            UsageEvents.Event.FOREGROUND_SERVICE_START -> return "FOREGROUND_SERVICE_START"
            UsageEvents.Event.FOREGROUND_SERVICE_STOP -> return "FOREGROUND_SERVICE_STOP"
            UsageEvents.Event.ACTIVITY_STOPPED -> return "ACTIVITY_STOPPED"
            UsageEvents.Event.DEVICE_SHUTDOWN -> return "DEVICE_SHUTDOWN"
            UsageEvents.Event.DEVICE_STARTUP -> return "DEVICE_STARTUP"
            else -> return eventType.toString()
        }
    }
}