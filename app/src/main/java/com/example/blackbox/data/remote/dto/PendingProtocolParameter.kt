package com.example.blackbox.data.remote.dto

data class PendingProtocolParameter(
    val params: String,
    val protocolVersion: Int,
    val targetMilestoneIndex: Int,
    val type: Int
)