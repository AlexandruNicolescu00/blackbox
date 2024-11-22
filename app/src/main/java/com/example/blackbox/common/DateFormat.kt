package com.example.blackbox.common

import java.text.SimpleDateFormat
import java.util.Locale

fun dateFormat(timestamp: Long): String {
    return SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(timestamp)
}