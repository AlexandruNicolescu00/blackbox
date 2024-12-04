package com.example.blackbox.presentation.record_detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.blackbox.domain.repository.AppUsageRepository
import com.example.blackbox.domain.repository.UsageEventRepository
import com.example.blackbox.domain.use_case.IOTAUseCases
import com.example.blackbox.domain.use_case.RecordsUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

const val defaultNavigationValue = -1L

@HiltViewModel
class RecordDetailViewModel @Inject constructor(
    private val appUsageRepository: AppUsageRepository,
    private val usageEventRepository: UsageEventRepository,
    private val recordsUseCases: RecordsUseCases,
    private val iotaUseCases: IOTAUseCases,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(RecordDetailState())
    val state: StateFlow<RecordDetailState> = _state.asStateFlow()

    init {
        savedStateHandle.get<Long>("recordId")?.let { recordId ->
            if (recordId != defaultNavigationValue) {
                viewModelScope.launch {
                    appUsageRepository.getAppUsageByRecordingId(recordId)
                        .collect { appUsages ->
                            _state.value = state.value.copy(
                                usageStats = appUsages
                            )
                        }
                }
                viewModelScope.launch {
                    usageEventRepository.getUsageEventByRecordingId(recordId)
                        .collect { usageEvents ->
                            _state.value = state.value.copy(
                                usageEvents = usageEvents
                            )
                        }
                }
                viewModelScope.launch {
                    val record = recordsUseCases.getRecord(recordId)
                    if (record != null) {
                        _state.value = state.value.copy(
                            record = record
                        )
                    }
                }
            }
        }
    }

    fun onEvent(event: RecordDetailEvent) {
        when (event) {
            is RecordDetailEvent.ViewInExplorer -> {
                viewModelScope.launch {
                    iotaUseCases.viewInExplorer(state.value.record!!.blockId!!)
                }
            }
        }
    }
}