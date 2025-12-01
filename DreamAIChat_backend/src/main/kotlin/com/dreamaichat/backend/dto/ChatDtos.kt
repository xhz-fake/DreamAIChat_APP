package com.dreamaichat.backend.dto

data class ChatRequest(
    val conversationId: Long? = null,
    val message: String = "",
    val model: String? = null,
    val images: List<ImagePayload>? = emptyList()
)

data class ImagePayload(
    val base64: String,
    val mime: String? = null
)

data class ChatResponse(
    val conversationId: Long,
    val replyMessage: String,
    val provider: String,
    val latencyMs: Long,
    val tokens: Int?
)
