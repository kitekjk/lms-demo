package com.lms.domain.model.user

/**
 * User Repository 인터페이스 (도메인 계층에 정의)
 * 실제 구현은 infrastructure 계층에서 수행
 */
interface UserRepository {
    fun save(user: User): User
    fun findById(userId: UserId): User?
    fun findByEmail(email: Email): User?
    fun existsByEmail(email: Email): Boolean
    fun delete(userId: UserId)
}
