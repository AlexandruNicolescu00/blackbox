package com.example.blackbox.common

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey

const val USER_PREFERENCES = "user_preferences"
val BACKGROUND = booleanPreferencesKey("background")
val AUTO_START = booleanPreferencesKey("auto_start")
val SECONDS_TO_SEND = longPreferencesKey("seconds_to_send")
const val REFRESH_INTERVAL: Long = 1
const val NETWORK_BASE_URL = "https://api.testnet.iotaledger.net" //"https://api.stardust-mainnet.iotaledger.net"
const val BLOCK_TAG = "Blackbox"
const val NOTIFICATION_EVENT = 10