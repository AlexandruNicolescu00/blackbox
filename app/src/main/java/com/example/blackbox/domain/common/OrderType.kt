package com.example.blackbox.domain.common

sealed class OrderType {
    data object Ascending: OrderType()
    data object Descending: OrderType()
}