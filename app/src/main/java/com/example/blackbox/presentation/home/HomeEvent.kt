package com.example.blackbox.presentation.home

sealed class HomeEvent {
    data object ToggleAutoStart : HomeEvent()
    data class SetSecondsToSend(val seconds: Long) : HomeEvent()
}