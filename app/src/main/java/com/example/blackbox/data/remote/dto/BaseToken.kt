package com.example.blackbox.data.remote.dto

data class BaseToken(
    val decimals: Int,
    val name: String,
    val tickerSymbol: String,
    val unit: String,
    val useMetricPrefix: Boolean
)