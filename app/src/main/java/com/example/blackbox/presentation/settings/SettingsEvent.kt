package com.example.blackbox.presentation.settings

sealed class SettingsEvent {
    data object ToggleBackgroundMode : SettingsEvent()
}