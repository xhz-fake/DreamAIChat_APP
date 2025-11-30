package com.dreamaichat.backend.repository

import com.dreamaichat.backend.entity.ChatMessageEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ChatMessageRepository : JpaRepository<ChatMessageEntity, Long> {
    fun findTop50ByConversationIdOrderByCreatedAtAsc(conversationId: Long): List<ChatMessageEntity>
}
