package com.dreamaichat.backend.dto

import jakarta.validation.constraints.NotBlank

data class ChatRequest(
    val conversationId: Long? = null,
    @field:NotBlank(message = "娑堟伅鍐呭涓嶈兘涓虹┖")
    val message: String,
    val model: String? = null
)

data class ChatResponse(
    val conversationId: Long,
    val replyMessage: String,
    val provider: String,
    val latencyMs: Long,
    val tokens: Int?
)
