package com.dreamaichat.backend.service

import com.dreamaichat.backend.dto.LoginRequest
import com.dreamaichat.backend.dto.LoginResponse
import com.dreamaichat.backend.dto.RegisterRequest
import com.dreamaichat.backend.entity.UserEntity
import com.dreamaichat.backend.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService
) {

    fun register(request: RegisterRequest): LoginResponse {
        userRepository.findByAccount(request.account.trim())?.let {
            throw IllegalArgumentException("Account already exists")
        }
        val entity = UserEntity(
            account = request.account.trim(),
            passwordHash = passwordEncoder.encode(request.password),
            username = request.username?.takeUnless { it.isBlank() }
                ?: request.account.substringBefore("@"),
            memberType = "free"
        )
        val saved = userRepository.save(entity)
        return buildLoginResponse(saved)
    }

    fun login(request: LoginRequest): LoginResponse {
        val user = userRepository.findByAccount(request.account.trim())
            ?: throw IllegalArgumentException("Invalid account or password")
        if (!passwordEncoder.matches(request.password, user.passwordHash)) {
            throw IllegalArgumentException("Invalid account or password")
        }
        return buildLoginResponse(user)
    }

    fun verify(token: String): Long =
        jwtService.parseUserId(token) ?: throw IllegalArgumentException("Invalid or expired token")

    private fun buildLoginResponse(user: UserEntity): LoginResponse {
        val jwt = jwtService.generateToken(user.id ?: error("User not persisted"))
        return LoginResponse(
            userId = user.id!!,
            username = user.username,
            token = jwt.token,
            expireTime = jwt.expiresAt.toEpochMilli(),
            memberType = user.memberType
        )
    }
}
