package com.example.blackbox.presentation.records_log

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.blackbox.domain.use_case.RecordsUseCases
import com.example.blackbox.domain.common.OrderType
import com.example.blackbox.domain.common.RecordsLogOrder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecordsLogViewModel @Inject constructor(
    private val recordsUseCases: RecordsUseCases
) : ViewModel() {

    private val _state = MutableStateFlow(RecordsLogState())
    val state: StateFlow<RecordsLogState> = _state.asStateFlow()

    private var getRecordsJob: Job? = null

    init {
        getRecords(RecordsLogOrder.Date(OrderType.Descending))
    }

    fun onEvent(event: RecordsLogEvent) {
        when (event) {
            is RecordsLogEvent.ToggleOrderSection -> {
                _state.value = state.value.copy(
                    isOrderSectionVisible = !state.value.isOrderSectionVisible
                )
            }
            is RecordsLogEvent.Order -> {
                if (state.value.order::class == event.order::class &&
                    state.value.order.orderType == event.order.orderType
                    ) {
                    return
                }
                getRecords(event.order)
            }
        }
    }

    private fun getRecords(order: RecordsLogOrder) {
        getRecordsJob?.cancel()
        getRecordsJob = viewModelScope.launch {
            recordsUseCases.getRecords(order)
                .collect { records ->
                    _state.value = state.value.copy(
                        records = records,
                        order = order
                    )
                }
        }
    }
}