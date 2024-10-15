package com.example.blackbox.presentation.home

data class HomeState(
    var isAutoStart: Boolean = false,
    var secondsToSend: Long = 30
)