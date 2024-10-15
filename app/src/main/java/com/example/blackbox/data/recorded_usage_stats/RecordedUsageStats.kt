package com.example.blackbox.data.recorded_usage_stats

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "recorded_usage_stats"
)
data class RecordedUsageStats(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startedAt: Long,
    val endedAt: Long? = null
)
