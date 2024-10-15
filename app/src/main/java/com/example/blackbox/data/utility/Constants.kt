package com.example.blackbox.data.utility

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey

const val USER_PREFERENCES = "user_preferences"
val BACKGROUND = booleanPreferencesKey("background")
val AUTO_START = booleanPreferencesKey("auto_start")
val SECONDS_TO_SEND = longPreferencesKey("seconds_to_send")
val REFRESH_INTERVAL: Long = 1