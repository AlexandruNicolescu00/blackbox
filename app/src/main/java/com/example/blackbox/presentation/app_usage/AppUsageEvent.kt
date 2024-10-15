package com.example.blackbox.presentation.app_usage

sealed class AppUsageEvent {
    data object AutoCollection : AppUsageEvent()
    data object StartRecording : AppUsageEvent()
    data object StopRecording : AppUsageEvent()
    data object SendLogs : AppUsageEvent()
    data object OnResume : AppUsageEvent()
}