package com.example.blackbox.domain.repository

import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    val isBackgroundFlow: Flow<Boolean>
    val isAutoStartFlow: Flow<Boolean>
    val getSecondsToSendFlow: Flow<Long>
    suspend fun toggleBackgroundMode()
    suspend fun toggleAutoStartMode()
    suspend fun setSecondsToSend(seconds: Long)
}