package com.lms.infrastructure.persistence.repository

import com.lms.domain.model.user.Email
import com.lms.domain.model.user.User
import com.lms.domain.model.user.UserId
import com.lms.domain.model.user.UserRepository
import com.lms.infrastructure.persistence.mapper.UserMapper
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

/**
 * User Repository 구현체
 * Domain의 UserRepository 인터페이스를 구현
 */
@Repository
@Transactional
class UserRepositoryImpl(
    private val jpaRepository: UserJpaRepository
) : UserRepository {

    override fun save(user: User): User {
        val entity = UserMapper.toEntity(user)
        val saved = jpaRepository.save(entity)
        return UserMapper.toDomain(saved)
    }

    override fun findById(userId: UserId): User? {
        return jpaRepository.findById(userId.value)
            .map { UserMapper.toDomain(it) }
            .orElse(null)
    }

    override fun findByEmail(email: Email): User? {
        return jpaRepository.findByEmail(email.value)
            ?.let { UserMapper.toDomain(it) }
    }

    override fun existsByEmail(email: Email): Boolean {
        return jpaRepository.existsByEmail(email.value)
    }

    override fun delete(userId: UserId) {
        jpaRepository.deleteById(userId.value)
    }
}
