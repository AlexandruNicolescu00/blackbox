package com.example.blackbox.data.remote.dto

data class Metrics(
    val blocksPerSecond: Double,
    val referencedBlocksPerSecond: Double,
    val referencedRate: Double
)