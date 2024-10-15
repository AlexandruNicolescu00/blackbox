package com.example.blackbox.presentation.app_usage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.blackbox.data.permissions.PermissionsManager
import com.example.blackbox.data.utility.REFRESH_INTERVAL
import com.example.blackbox.domain.repository.AppUsageRepository
import com.example.blackbox.domain.repository.UserPreferencesRepository
import com.example.blackbox.domain.use_case.RecordingServiceUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
    private val preferencesRepository: UserPreferencesRepository
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
    }

    private var getUsageStatsJob: Job? = null

    fun onEvent(event: AppUsageEvent) {
        when (event) {
            is AppUsageEvent.AutoCollection -> {
                if (!state.value.isRecording) {
                    startAutoCollection()
                }
            }
            is AppUsageEvent.StartRecording -> {
                if (!state.value.isRecording) {
                    startUsageStatsCollection()
                }
            }
            is AppUsageEvent.StopRecording -> {
                if (state.value.isRecording) {
                    stopUsageStatsCollection()
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

    private fun startAutoCollection() {
            viewModelScope.launch {
                while (state.value.isAutoStart) {
                    _state.value = state.value.copy(
                        isRecording = true,
                        startedAt = System.currentTimeMillis(),
                        finishedAt = null
                    )
                    getUsageStats()
                    var i = 0
                    while (i < state.value.secondsToSend / REFRESH_INTERVAL && state.value.isAutoStart) {
                        delay(REFRESH_INTERVAL * 1000)
                        i += 1
                    }
                    _state.value = state.value.copy(
                        isRecording = false,
                        finishedAt = System.currentTimeMillis()
                    )
                }
            }
    }

    private fun startUsageStatsCollection() {
        _state.value = state.value.copy(
            isRecording = true,
            startedAt = System.currentTimeMillis(),
            finishedAt = null
        )
            recordingServiceUseCases.startRecordingService()
        getUsageStats()
    }

    private fun getUsageStats() {
        getUsageStatsJob?.cancel()
        getUsageStatsJob = viewModelScope.launch {
            appUsageRepository.getAppUsageByLastTimeUsed(state.value.startedAt!!)
                .collect { appUsages ->
                    _state.value = state.value.copy(
                        usageStats = appUsages
                    )
                }
        }
    }

    private fun stopUsageStatsCollection() {
        _state.value = state.value.copy(
            isRecording = false,
            finishedAt = System.currentTimeMillis()
        )
        recordingServiceUseCases.stopRecordingService()
    }

    private fun sendLogs() {
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