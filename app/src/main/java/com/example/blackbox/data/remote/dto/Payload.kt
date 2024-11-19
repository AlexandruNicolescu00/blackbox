package com.example.blackbox.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class Payload(
    val `data`: String,
    val tag: String,
    val type: Int
)