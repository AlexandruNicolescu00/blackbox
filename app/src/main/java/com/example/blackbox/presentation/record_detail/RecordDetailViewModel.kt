package com.example.blackbox.presentation.record_detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.blackbox.domain.repository.AppUsageRepository
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
            }
        }
    }
}