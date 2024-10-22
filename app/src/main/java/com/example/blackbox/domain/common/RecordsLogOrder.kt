package com.example.blackbox.domain.common

sealed class RecordsLogOrder(val orderType: OrderType) {
    class Date(orderType: OrderType): RecordsLogOrder(orderType)

    // Utility function for change order type without creating new object
    fun copy(orderType: OrderType): RecordsLogOrder {
        return when(this) {
            is Date -> Date(orderType)
        }
    }
}