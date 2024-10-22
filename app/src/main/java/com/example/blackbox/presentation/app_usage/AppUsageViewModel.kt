package com.example.blackbox.presentation.app_usage

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.blackbox.data.manager.PermissionsManager
import com.example.blackbox.domain.repository.AppUsageRepository
import com.example.blackbox.domain.repository.UserPreferencesRepository
import com.example.blackbox.domain.use_case.IOTAUseCases
import com.example.blackbox.domain.use_case.RecordingServiceUseCases
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
    private val iotaUseCases: IOTAUseCases
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
                    isRecording = it.isRecording,
                    startedAt = it.startedAt,
                    finishedAt = it.finishedAt,
                    usageStats = it.appUsages
                )
                Log.d("UsageStatsService", "ViewModel updated with started at: ${it.startedAt}")
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
        viewModelScope.launch {
            iotaUseCases.sendData("test").collect {
                Log.d("AppUsageViewModel", "Response: ${it.data} \n ${it.message}")
            }
        }
        _state.value = state.value.copy(
            usageStats = emptyList()
        )
    }

    private fun recompose() {
        _state.value = state.value.copy(
            recompose = !state.value.recompose
        )
    }
}