package com.example.blackbox.domain.use_case

import android.content.Context
import android.content.Intent
import com.example.blackbox.presentation.send_data_service.SendDataService

class StartSendDataService(
    private val context: Context
) {
    operator fun invoke(appUsage: String, recordingId: Long) {
        val serviceIntent = Intent(context, SendDataService::class.java)
            .putExtra("APP_USAGE", appUsage)
            .putExtra("RECORDING_ID", recordingId)
        context.startForegroundService(serviceIntent)
    }
}