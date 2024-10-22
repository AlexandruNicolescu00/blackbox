package com.example.blackbox.data.remote.dto

data class FullBlockWithTaggedDataPayload(
    val nonce: String,
    val parents: List<String>,
    val payload: Payload,
    val protocolVersion: Int
)