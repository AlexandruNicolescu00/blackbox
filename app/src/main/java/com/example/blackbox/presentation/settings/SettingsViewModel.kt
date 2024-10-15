package com.example.blackbox.presentation.settings

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.blackbox.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _state = mutableStateOf(SettingsState())
    val state: State<SettingsState> = _state

    init {
        viewModelScope.launch {
            userPreferencesRepository.isBackgroundFlow.collect { isBackground ->
                _state.value = state.value.copy(
                    isBackground = isBackground
                )
            }
        }
    }

    fun onEvent(event: SettingsEvent) {
        when (event) {
            is SettingsEvent.ToggleBackgroundMode -> {
                viewModelScope.launch {
                    userPreferencesRepository.toggleBackgroundMode()
                }
            }
        }
    }

}