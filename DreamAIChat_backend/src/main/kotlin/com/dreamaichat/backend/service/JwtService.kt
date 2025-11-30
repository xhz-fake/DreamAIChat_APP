package com.dreamaichat.backend.service

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.time.Instant
import java.time.temporal.ChronoUnit

data class JwtToken(
    val token: String,
    val expiresAt: Instant
)

@Service
class JwtService(
    @Value("\${security.jwt.secret}") private val secret: String,
    @Value("\${security.jwt.expire-hours:168}") private val expireHours: Long
) {
    private val signingKey = run {
        val secretBytes = secret.toByteArray(StandardCharsets.UTF_8)
        // JWT 要求密钥至少 256 位（32 字节），如果不够则自动扩展
        val keyBytes = if (secretBytes.size < 32) {
            // 使用 SHA-256 哈希扩展密钥到 32 字节
            val md = MessageDigest.getInstance("SHA-256")
            md.update(secretBytes)
            md.digest()
        } else {
            secretBytes.take(32).toByteArray() // 如果太长，只取前 32 字节
        }
        Keys.hmacShaKeyFor(keyBytes)
    }

    fun generateToken(userId: Long): JwtToken {
        val expiresAt = Instant.now().plus(expireHours, ChronoUnit.HOURS)
        val token = Jwts.builder()
            .subject(userId.toString())
            .expiration(java.util.Date.from(expiresAt))
            .signWith(signingKey)
            .compact()
        return JwtToken(token, expiresAt)
    }

    fun parseUserId(token: String): Long? =
        runCatching {
            Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .payload
                .subject
                ?.toLong()
        }.getOrNull()
}
