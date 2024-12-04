package com.example.blackbox.presentation.app_usage

import com.example.blackbox.data.app_usage.AppUsage
import com.example.blackbox.data.usage_event.UsageEvent

data class AppUsageState(
    val recordingId: Long? = null,
    val usageStats: List<AppUsage> = emptyList(),
    val usageEvents: List<UsageEvent> = emptyList(),
    val currentRecordingId: Long? = null,
    val isRecording: Boolean = false,
    val isAutoStart: Boolean = false,
    val isSending: Boolean = false,
    val secondsToSend: Long = 0,
    val startedAt: Long? = null,
    val finishedAt: Long? = null,
    val recompose:  Boolean = false
)