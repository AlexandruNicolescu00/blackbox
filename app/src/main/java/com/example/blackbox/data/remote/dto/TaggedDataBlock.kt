package com.example.blackbox.data.remote.dto

data class TaggedDataBlock(
    val nonce: String,
    val parents: List<String>,
    val payload: Payload,
    val protocolVersion: Int
)