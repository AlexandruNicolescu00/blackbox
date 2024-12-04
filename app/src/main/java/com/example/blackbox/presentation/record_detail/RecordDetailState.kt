package com.example.blackbox.presentation.record_detail

import com.example.blackbox.data.app_usage.AppUsage
import com.example.blackbox.data.recorded_usage_stats.RecordedUsageStats
import com.example.blackbox.data.usage_event.UsageEvent

data class RecordDetailState(
    val record: RecordedUsageStats? = null,
    val usageStats: List<AppUsage> = emptyList(),
    val usageEvents: List<UsageEvent> = emptyList()
)
