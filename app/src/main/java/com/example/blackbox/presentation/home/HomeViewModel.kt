package com.example.blackbox.presentation.home

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.blackbox.common.SharedData
import com.example.blackbox.data.manager.PermissionsManager
import com.example.blackbox.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    val permissionsManager: PermissionsManager,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val sharedData: SharedData,
) : ViewModel() {

    private val _state = mutableStateOf(HomeState())
    val state: State<HomeState> = _state

    private val _messageChannel = Channel<String>()
    val messageChannel = _messageChannel.receiveAsFlow()

    init {
        viewModelScope.launch {
            userPreferencesRepository.isAutoStartFlow.collect { isAutoStart ->
                _state.value = state.value.copy(
                    isAutoStart = isAutoStart
                )
            }
        }
        viewModelScope.launch {
            userPreferencesRepository.getSecondsToSendFlow.collect { seconds ->
                _state.value = state.value.copy(
                    secondsToSend = seconds
                )
            }
        }
    }

    fun onEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.ToggleAutoStart -> {
                viewModelScope.launch {
                    if (!state.value.isAutoStart && sharedData.recordingState.value?.isRecording == true) {
                        _messageChannel.send("Please stop recording before enabling automatic mode")
                    } else {
                        userPreferencesRepository.toggleAutoStartMode()
                    }
                }
            }
            is HomeEvent.SetSecondsToSend -> {
                viewModelScope.launch {
                    userPreferencesRepository.setSecondsToSend(event.seconds)
                }
            }
        }
    }
}