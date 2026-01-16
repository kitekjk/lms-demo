package com.lms.infrastructure.persistence.mapper

import com.lms.domain.model.user.*
import com.lms.infrastructure.persistence.entity.UserEntity

/**
 * User Domain ↔ Entity Mapper
 */
object UserMapper {
    /**
     * Entity → Domain 변환
     */
    fun toDomain(entity: UserEntity): User = User.reconstruct(
        id = UserId.from(entity.id),
        email = Email(entity.email),
        password = Password(entity.password),
        role = entity.role,
        isActive = entity.isActive,
        createdAt = entity.createdAt,
        lastLoginAt = entity.lastLoginAt
    )

    /**
     * Domain → Entity 변환
     * Note: createdAt은 JPA Auditing에 의해 자동 설정됨
     */
    fun toEntity(domain: User): UserEntity = UserEntity(
        id = domain.id.value,
        email = domain.email.value,
        password = domain.password.encodedValue,
        role = domain.role,
        isActive = domain.isActive,
        lastLoginAt = domain.lastLoginAt
    )

    /**
     * Domain 변경사항을 Entity에 반영
     */
    fun updateEntity(entity: UserEntity, domain: User) {
        entity.email = domain.email.value
        entity.password = domain.password.encodedValue
        entity.role = domain.role
        entity.isActive = domain.isActive
        entity.lastLoginAt = domain.lastLoginAt
    }
}
