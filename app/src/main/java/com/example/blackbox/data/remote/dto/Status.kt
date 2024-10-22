package com.example.blackbox.data.remote.dto

data class Status(
    val confirmedMilestone: ConfirmedMilestone,
    val isHealthy: Boolean,
    val latestMilestone: LatestMilestone,
    val pruningIndex: Int
)