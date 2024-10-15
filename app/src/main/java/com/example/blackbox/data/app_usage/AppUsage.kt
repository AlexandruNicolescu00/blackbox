package com.example.blackbox.data.app_usage

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.blackbox.data.recorded_usage_stats.RecordedUsageStats

@Entity(
    tableName = "app_usage",
    foreignKeys = [
        ForeignKey(
            entity = RecordedUsageStats::class,
            parentColumns = ["id"],
            childColumns = ["recordingId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("recordingId")
    ]
)
data class AppUsage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    val lastTimeUsed: Long,
    val recordingId: Long
)
