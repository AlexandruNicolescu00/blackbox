package com.example.blackbox.data.usage_stats_manager

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.example.blackbox.data.permissions.PermissionsManager

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
}