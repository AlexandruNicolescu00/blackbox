package com.example.blackbox.data.remote.dto

data class NodeInfo(
    val baseToken: BaseToken,
    val features: List<String>,
    val metrics: Metrics,
    val name: String,
    val pendingProtocolParameters: List<PendingProtocolParameter>,
    val protocol: Protocol,
    val status: Status,
    val supportedProtocolVersions: List<Int>,
    val version: String
)