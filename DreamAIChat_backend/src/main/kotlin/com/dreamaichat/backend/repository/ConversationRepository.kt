package com.dreamaichat.backend.repository

import com.dreamaichat.backend.entity.ConversationEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ConversationRepository : JpaRepository<ConversationEntity, Long> {
    fun findByIdAndUserId(id: Long, userId: Long): ConversationEntity?
    fun findAllByUserIdOrderByUpdatedAtDesc(userId: Long): List<ConversationEntity>
}
