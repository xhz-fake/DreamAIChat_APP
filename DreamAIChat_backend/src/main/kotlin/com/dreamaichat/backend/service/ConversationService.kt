package com.dreamaichat.backend.service

import com.dreamaichat.backend.entity.ChatMessageEntity
import com.dreamaichat.backend.entity.ConversationEntity
import com.dreamaichat.backend.repository.ChatMessageRepository
import com.dreamaichat.backend.repository.ConversationRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class ConversationService(
    private val conversationRepository: ConversationRepository,
    private val chatMessageRepository: ChatMessageRepository
) {

    fun listConversations(userId: Long): List<ConversationEntity> =
        conversationRepository.findAllByUserIdOrderByUpdatedAtDesc(userId)

    @Transactional
    fun createOrFindConversation(
        userId: Long,
        conversationId: Long?,
        provider: String,
        model: String,
        initialMessage: String? = null
    ): ConversationEntity {
        if (conversationId != null) {
            conversationRepository.findByIdAndUserId(conversationId, userId)?.let { return it }
        }
        val title = initialMessage?.take(30)?.ifBlank { "鏂扮殑瀵硅瘽" } ?: "鏂扮殑瀵硅瘽"
        return conversationRepository.save(
            ConversationEntity(
                userId = userId,
                title = title,
                provider = provider,
                model = model
            )
        )
    }

    @Transactional
    fun saveUserMessage(conversationId: Long, content: String): ChatMessageEntity {
        val entity = ChatMessageEntity(
            conversationId = conversationId,
            role = "user",
            content = content
        )
        touchConversation(conversationId)
        return chatMessageRepository.save(entity)
    }

    @Transactional
    fun saveAssistantMessage(conversationId: Long, reply: String, tokens: Int?, latencyMs: Long?): ChatMessageEntity {
        val entity = ChatMessageEntity(
            conversationId = conversationId,
            role = "assistant",
            content = reply,
            tokens = tokens,
            latencyMs = latencyMs
        )
        touchConversation(conversationId)
        return chatMessageRepository.save(entity)
    }

    fun getRecentMessages(conversationId: Long): List<ChatMessageEntity> =
        chatMessageRepository.findTop50ByConversationIdOrderByCreatedAtAsc(conversationId)

    private fun touchConversation(conversationId: Long) {
        conversationRepository.findById(conversationId).ifPresent {
            it.updatedAt = Instant.now()
            conversationRepository.save(it)
        }
    }
}
