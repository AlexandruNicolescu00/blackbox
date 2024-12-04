package com.example.blackbox.common

import androidx.lifecycle.MutableLiveData
import com.example.blackbox.data.app_usage.AppUsage
import com.example.blackbox.data.usage_event.UsageEvent

object SharedData {
    val recordingState = MutableLiveData<RecordingState>()
}

data class RecordingState(
    val id: Long? = null,
    val startedAt: Long? = null,
    val finishedAt: Long? = null,
    val isRecording: Boolean = false,
    val appUsages: List<AppUsage> = emptyList(),
    val usageEvents: List<UsageEvent> = emptyList()
)