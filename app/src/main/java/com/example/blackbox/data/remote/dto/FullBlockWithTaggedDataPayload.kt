package com.example.blackbox.data.remote.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class FullBlockWithTaggedDataPayload(
    val protocolVersion: Int,
    val parents: List<String>,
    val payload: Payload? = null,
    @Transient val nonce: String = ""
)