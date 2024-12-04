package com.example.blackbox.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.blackbox.data.app_usage.AppUsage
import com.example.blackbox.data.app_usage.AppUsageDao
import com.example.blackbox.data.recorded_usage_stats.RecordedUsageStats
import com.example.blackbox.data.recorded_usage_stats.RecordedUsageStatsDao
import com.example.blackbox.data.usage_event.UsageEvent
import com.example.blackbox.data.usage_event.UsageEventDao

@Database(entities = [AppUsage::class, UsageEvent::class, RecordedUsageStats::class], version = 2)
abstract class AppDatabase: RoomDatabase() {
    abstract val appUsageDao: AppUsageDao
    abstract val usageEventDao: UsageEventDao
    abstract val recordedUsageStatsDao: RecordedUsageStatsDao

    companion object {
        const val DATABASE_NAME = "blackbox_db"
    }
}