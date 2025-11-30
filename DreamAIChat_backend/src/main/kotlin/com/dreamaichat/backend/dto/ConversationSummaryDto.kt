package com.dreamaichat.backend.dto

data class ConversationSummaryDto(
    val id: Long,
    val title: String,
    val provider: String,
    val model: String,
    val updatedAt: Long
)
