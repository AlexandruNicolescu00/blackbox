package com.example.blackbox.presentation.send_data_service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import com.example.blackbox.R
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.example.blackbox.common.Resource
import com.example.blackbox.domain.repository.IOTARepository
import com.example.blackbox.domain.use_case.EvaluationCongestionDelay
import com.example.blackbox.domain.use_case.EvaluationEventsNonce
import com.example.blackbox.domain.use_case.IOTAUseCases
import com.example.blackbox.domain.use_case.RecordsUseCases
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentLinkedQueue
import javax.inject.Inject

private const val SENDING_DATA_CHANNEL_ID = "SendingDataToIOTA"
private const val SENDING_DATA_CHANNEL_NAME = "Sending Data To IOTA"
private const val SENDING_DATA_NOTIFICATION_ID = 42

@AndroidEntryPoint
class SendDataService : Service() {

    @Inject
    lateinit var iotaUseCases: IOTAUseCases

    @Inject
    lateinit var recordsUseCases: RecordsUseCases

    private val job = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + job)

    private val callQueue = ConcurrentLinkedQueue<Pair<String, Long>>()

    @Volatile
    private var isProcessing = false

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Log.d("SendDataService", "Service Started")
        val notification = createNotification()
        ServiceCompat.startForeground(
            this,
            SENDING_DATA_NOTIFICATION_ID,
            notification,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            } else {
                0
            }
        )

        val usageEvent = intent?.getStringExtra("APP_USAGE")
        val recordingId = intent?.getLongExtra("RECORDING_ID", -1)

        if (usageEvent == null || recordingId == null || recordingId == -1L) {
            stopSelf()
            return START_NOT_STICKY
        }

        enqueueRequest(usageEvent, recordingId)

        return START_STICKY
    }

    private fun enqueueRequest(usageEvent: String, recordingId: Long) {
        callQueue.add(Pair<String, Long>(usageEvent, recordingId))
        if (!isProcessing) {
            isProcessing = true
            processQueue()
        }
    }

    private fun processQueue() {
        serviceScope.launch {
            while (callQueue.isNotEmpty()) {
                val (usageEvent, recordingId) = callQueue.poll()
                sendData(usageEvent, recordingId)
            }
            isProcessing = false
            stopSelf()
        }
    }

    private suspend fun sendData(usageEvent: String, recordingId: Long) {
        iotaUseCases.sendData(usageEvent).collect { result ->
            when (result) {
                is Resource.Success -> {
                    Log.d("SendDataService", "Data sent successfully id: ${result.data}")
                    val record = recordsUseCases.getRecord(recordingId)
                    if (record != null) {
                        val newRecord = record.copy(
                            blockId = result.data
                        )
                        recordsUseCases.updateRecord(newRecord)
                    }
                    return@collect
                }
                is Resource.Error -> {
                    Log.e("SendDataService", "Error sending data: ${result.message}")
                    enqueueRequest(usageEvent, recordingId)
                    return@collect
                }
                is Resource.Loading -> {
                    Log.d("SendDataService", "Loading...")
                }
            }
        }
    }

    private fun createNotification(): Notification {
        val channel = NotificationChannel(
            SENDING_DATA_CHANNEL_ID,
            SENDING_DATA_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)

        return NotificationCompat.Builder(this, SENDING_DATA_CHANNEL_ID)
            .setContentTitle("Sending data")
            .setContentText("The service is sending data to IOTA network.")
            .setSmallIcon(R.drawable.baseline_send_24)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}