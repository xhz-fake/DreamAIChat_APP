package com.dreamaichat.backend.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Lob
import jakarta.persistence.PrePersist
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "chat_messages")
data class ChatMessageEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "conversation_id", nullable = false)
    var conversationId: Long = 0,

    @Column(nullable = false, length = 16)
    var role: String = "",

    @Lob
    @Column(nullable = false)
    var content: String = "",

    @Column(name = "token_usage")
    var tokens: Int? = null,

    @Column(name = "latency_ms")
    var latencyMs: Long? = null,

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now()
) {
    @PrePersist
    fun onCreate() {
        createdAt = Instant.now()
    }
}
