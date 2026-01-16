package com.lms.domain.model.user

import com.lms.domain.common.DomainContext
import java.time.Instant

/**
 * User Aggregate Root
 * 인증 및 인가 정보만 관리 (순수 Kotlin, Spring/JPA 의존 금지)
 */
data class User private constructor(
    val id: UserId,
    val email: Email,
    val password: Password,
    val role: Role,
    val isActive: Boolean,
    val createdAt: Instant,
    val lastLoginAt: Instant?
) {
    companion object {
        /**
         * 새로운 사용자 생성
         */
        fun create(
            context: DomainContext,
            email: Email,
            password: Password,
            role: Role
        ): User {
            require(role != Role.SUPER_ADMIN) {
                "SUPER_ADMIN은 시스템에서만 생성 가능합니다."
            }

            return User(
                id = UserId.generate(),
                email = email,
                password = password,
                role = role,
                isActive = true,
                createdAt = context.requestedAt,
                lastLoginAt = null
            )
        }

        /**
         * 기존 사용자 재구성 (Repository에서 조회 시)
         */
        fun reconstruct(
            id: UserId,
            email: Email,
            password: Password,
            role: Role,
            isActive: Boolean,
            createdAt: Instant,
            lastLoginAt: Instant?
        ): User = User(id, email, password, role, isActive, createdAt, lastLoginAt)
    }

    /**
     * 로그인 처리
     */
    fun login(context: DomainContext): User {
        require(isActive) { "비활성화된 사용자입니다." }
        return this.copy(lastLoginAt = context.requestedAt)
    }

    /**
     * 비밀번호 변경
     */
    fun changePassword(
        context: DomainContext,
        newPassword: Password
    ): User {
        return this.copy(password = newPassword)
    }

    /**
     * 사용자 비활성화
     */
    fun deactivate(context: DomainContext): User {
        require(isActive) { "이미 비활성화된 사용자입니다." }
        return this.copy(isActive = false)
    }

    /**
     * 사용자 활성화
     */
    fun activate(context: DomainContext): User {
        require(!isActive) { "이미 활성화된 사용자입니다." }
        return this.copy(isActive = true)
    }
}
