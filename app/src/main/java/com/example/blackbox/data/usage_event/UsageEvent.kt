package com.example.blackbox.data.usage_event

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.blackbox.data.recorded_usage_stats.RecordedUsageStats

@Entity(
    tableName = "usage_event",
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
data class UsageEvent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    val timestamp: Long,
    val eventType: String,
    val recordingId: Long
)