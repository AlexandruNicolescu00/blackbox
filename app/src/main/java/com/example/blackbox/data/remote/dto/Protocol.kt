package com.example.blackbox.data.remote.dto

data class Protocol(
    val bech32Hrp: String,
    val belowMaxDepth: Int,
    val minPowScore: Int,
    val networkName: String,
    val rentStructure: RentStructure,
    val tokenSupply: String,
    val version: Int
)