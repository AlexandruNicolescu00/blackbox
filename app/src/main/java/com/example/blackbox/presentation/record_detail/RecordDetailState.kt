package com.example.blackbox.presentation.record_detail

import com.example.blackbox.data.app_usage.AppUsage
import com.example.blackbox.data.recorded_usage_stats.RecordedUsageStats

data class RecordDetailState(
    val record: RecordedUsageStats? = null,
    val usageStats: List<AppUsage> = emptyList(),
)
