package com.example.blackbox.presentation.records_log

import com.example.blackbox.domain.common.RecordsLogOrder

sealed class RecordsLogEvent {
    data object ToggleOrderSection: RecordsLogEvent()
    data class Order(val order: RecordsLogOrder): RecordsLogEvent()
}