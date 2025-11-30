package com.dreamaichat.backend.repository

import com.dreamaichat.backend.entity.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<UserEntity, Long> {
    fun findByAccount(account: String): UserEntity?
}
