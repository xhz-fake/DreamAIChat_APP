package com.dreamaichat.backend.dto

import jakarta.validation.constraints.NotBlank

data class RegisterRequest(
    @field:NotBlank(message = "璐﹀彿涓嶈兘涓虹┖")
    val account: String,
    @field:NotBlank(message = "瀵嗙爜涓嶈兘涓虹┖")
    val password: String,
    val username: String? = null
)

data class LoginRequest(
    @field:NotBlank(message = "璐﹀彿涓嶈兘涓虹┖")
    val account: String,
    @field:NotBlank(message = "瀵嗙爜涓嶈兘涓虹┖")
    val password: String
)

data class LoginResponse(
    val userId: Long,
    val username: String,
    val token: String,
    val expireTime: Long,
    val memberType: String
)
