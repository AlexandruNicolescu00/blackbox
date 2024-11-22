package com.example.blackbox.presentation.app_usage

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.blackbox.common.dateFormat
import com.example.blackbox.data.manager.PermissionsManager
import com.example.blackbox.domain.repository.AppUsageRepository
import com.example.blackbox.domain.repository.UserPreferencesRepository
import com.example.blackbox.domain.use_case.RecordingServiceUseCases
import com.example.blackbox.domain.use_case.StartSendDataService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppUsageViewModel @Inject constructor(
    val permissionsManager: PermissionsManager,
    private val recordingServiceUseCases: RecordingServiceUseCases,
    private val appUsageRepository: AppUsageRepository,
    private val preferencesRepository: UserPreferencesRepository,
    private val startSendDataService: StartSendDataService,
) : ViewModel() {

    private val _state = MutableStateFlow(AppUsageState())
    val state: StateFlow<AppUsageState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            preferencesRepository.isAutoStartFlow.collect {
                _state.value = state.value.copy(
                    isAutoStart = it
                )
            }
        }
        viewModelScope.launch {
            preferencesRepository.getSecondsToSendFlow.collect {
                _state.value = state.value.copy(
                    secondsToSend = it
                )
            }
        }
        viewModelScope.launch {
            appUsageRepository.recordingState.observeForever {
                _state.value = state.value.copy(
                    recordingId = it.id,
                    isRecording = it.isRecording,
                    startedAt = it.startedAt,
                    finishedAt = it.finishedAt,
                    usageStats = it.appUsages
                )
            }
        }
    }

    fun onEvent(event: AppUsageEvent) {
        when (event) {
            is AppUsageEvent.StartRecording -> {
                if (!state.value.isRecording) {
                    recordingServiceUseCases.startRecordingService()
                }
            }
            is AppUsageEvent.StopRecording -> {
                if (state.value.isRecording) {
                    recordingServiceUseCases.stopRecordingService()
                }
            }
            is AppUsageEvent.SendLogs -> {
                sendLogs()
            }
            is AppUsageEvent.OnResume -> {
                recompose()
            }
        }
    }

    private fun sendLogs() {
        var appUsage = ""
        for (usage in state.value.usageStats) {
            appUsage += usage.packageName + " at " + dateFormat(usage.lastTimeUsed) + "\n"
        }

        Log.d("SendDataService", "Sending logs: $appUsage")

        if(appUsage != "") {
            startSendDataService.invoke(appUsage, state.value.recordingId!!)
        }

        onEvent(AppUsageEvent.StopRecording)
    }

    private fun recompose() {
        _state.value = state.value.copy(
            recompose = !state.value.recompose
        )
    }
}