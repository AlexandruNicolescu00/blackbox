package com.example.blackbox.presentation.record_detail

sealed class RecordDetailEvent {
    object ViewInExplorer : RecordDetailEvent()
}