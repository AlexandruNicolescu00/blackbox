package com.example.blackbox.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.example.blackbox.data.utility.AUTO_START
import com.example.blackbox.data.utility.BACKGROUND
import com.example.blackbox.data.utility.SECONDS_TO_SEND
import com.example.blackbox.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserPreferencesRepositoryImpl(
    private val dataStore: DataStore<Preferences>
) : UserPreferencesRepository {
    override val isBackgroundFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[BACKGROUND] ?: false
    }

    override val isAutoStartFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[AUTO_START] ?: false
    }

    override val getSecondsToSendFlow: Flow<Long> = dataStore.data.map { preferences ->
            preferences[SECONDS_TO_SEND] ?: 30
    }

    override suspend fun toggleBackgroundMode() {
        dataStore.edit { preferences ->
            val currentBackgroundMode = preferences[BACKGROUND] ?: false
            preferences[BACKGROUND] = !currentBackgroundMode
        }
    }

    override suspend fun toggleAutoStartMode() {
        dataStore.edit { preferences ->
            val currentAutoStartMode = preferences[AUTO_START] ?: false
            preferences[AUTO_START] = !currentAutoStartMode
        }
    }

    override suspend fun setSecondsToSend(seconds: Long) {
        dataStore.edit { preferences ->
            preferences[SECONDS_TO_SEND] = seconds
        }

    }

}