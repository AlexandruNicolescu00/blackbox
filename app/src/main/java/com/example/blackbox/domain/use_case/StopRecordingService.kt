package com.example.blackbox.domain.use_case

import android.content.Context
import android.content.Intent
import com.example.blackbox.presentation.usage_stats_service.UsageStatsService

class StopRecordingService(
    private val context: Context,
    ) {
    operator fun invoke() {
        val serviceIntent = Intent(context, UsageStatsService::class.java)
        context.stopService(serviceIntent)
    }
}