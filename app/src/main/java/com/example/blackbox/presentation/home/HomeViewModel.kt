package com.example.blackbox.presentation.home

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.blackbox.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    private val _state = mutableStateOf(HomeState())
    val state: State<HomeState> = _state

    init {
        viewModelScope.launch {
            userPreferencesRepository.isAutoStartFlow.collect { isAutoStart ->
                _state.value = state.value.copy(
                    isAutoStart = isAutoStart
                )
            }
        }
        // Use different coroutine for getting different flows
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
                    userPreferencesRepository.toggleAutoStartMode()
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