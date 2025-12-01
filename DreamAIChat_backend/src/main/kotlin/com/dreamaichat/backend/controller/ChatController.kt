package com.dreamaichat.backend.controller

import com.dreamaichat.backend.config.ModelProviderProperties
import com.dreamaichat.backend.dto.ApiResponse
import com.dreamaichat.backend.dto.ChatRequest
import com.dreamaichat.backend.dto.ChatResponse
import com.dreamaichat.backend.dto.ConversationSummaryDto
import com.dreamaichat.backend.service.AuthService
import com.dreamaichat.backend.service.ConversationService
import com.dreamaichat.backend.service.ModelRouterService
import jakarta.validation.Valid
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/chat")
class ChatController(
    private val authService: AuthService,
    private val conversationService: ConversationService,
    private val modelRouterService: ModelRouterService,
    private val providerProperties: ModelProviderProperties
) {

    @GetMapping("/conversations")
    fun listConversations(
        @RequestHeader(HttpHeaders.AUTHORIZATION) authorization: String?
    ): ResponseEntity<ApiResponse<List<ConversationSummaryDto>>> {
        val userId = resolveUserId(authorization)
        val summaries = conversationService.listConversations(userId).map {
            ConversationSummaryDto(
                id = it.id ?: 0,
                title = it.title,
                provider = it.provider,
                model = it.model,
                updatedAt = it.updatedAt.toEpochMilli()
            )
        }
        return ResponseEntity.ok(ApiResponse.ok(summaries))
    }

    @PostMapping("/send")
    fun sendMessage(
        @RequestHeader(HttpHeaders.AUTHORIZATION) authorization: String?,
        @Valid @RequestBody request: ChatRequest
    ): ResponseEntity<ApiResponse<ChatResponse>> {
        val userId = resolveUserId(authorization)
        val providerKey = request.model ?: providerProperties.defaultProvider
        val providerModel = providerProperties.providers[providerKey]?.model ?: providerKey

        val initialMessage = when {
            request.message.isNotBlank() -> request.message
            !request.images.isNullOrEmpty() -> "[图片]"
            else -> ""
        }

        val conversation = conversationService.createOrFindConversation(
            userId = userId,
            conversationId = request.conversationId,
            provider = providerKey,
            model = providerModel,
            initialMessage = initialMessage
        )

        conversationService.saveUserMessage(conversation.id!!, initialMessage)
        val history = conversationService.getRecentMessages(conversation.id!!)
            .map {
                ModelRouterService.MessagePayload(
                    role = it.role,
                    parts = listOf(ModelRouterService.MessagePart.text(it.content))
                )
            }
            .toMutableList()

        history.add(
            ModelRouterService.MessagePayload(
                role = "user",
                parts = buildUserParts(request)
            )
        )

        val result = modelRouterService.generateReply(providerKey, history)
        conversationService.saveAssistantMessage(conversation.id!!, result.reply, result.tokens, result.latency)

        val response = ChatResponse(
            conversationId = conversation.id!!,
            replyMessage = result.reply,
            provider = result.provider,
            latencyMs = result.latency,
            tokens = result.tokens
        )
        return ResponseEntity.ok(ApiResponse.ok(response))
    }

    private fun resolveUserId(authorization: String?): Long {
        val token = authorization?.removePrefix("Bearer ")?.trim()
            ?: throw IllegalArgumentException("Missing Authorization header")
        return authService.verify(token)
    }

    private fun buildUserParts(request: ChatRequest): List<ModelRouterService.MessagePart> {
        val parts = mutableListOf<ModelRouterService.MessagePart>()
        if (request.message.isNotBlank()) {
            parts.add(ModelRouterService.MessagePart.text(request.message))
        }
        request.images?.forEach {
            parts.add(
                ModelRouterService.MessagePart.image(
                    it.base64,
                    it.mime ?: "image/jpeg"
                )
            )
        }
        if (parts.isEmpty()) {
            parts.add(ModelRouterService.MessagePart.text("[图片]"))
        }
        return parts
    }
}
