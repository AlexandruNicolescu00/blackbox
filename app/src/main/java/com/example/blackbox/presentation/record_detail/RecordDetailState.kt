package com.example.blackbox.presentation.record_detail

import com.example.blackbox.data.app_usage.AppUsage

data class RecordDetailState(
    val usageStats: List<AppUsage> = emptyList(),
)
