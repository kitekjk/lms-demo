package com.lms.infrastructure.persistence.entity

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant
import java.util.UUID

/**
 * User JPA Entity
 * 인증/인가 정보 (User Aggregate에 대응)
 */
@Entity
@Table(
    name = "users",
    indexes = [Index(name = "idx_user_email", columnList = "email", unique = true)]
)
@EntityListeners(AuditingEntityListener::class)
class UserEntity(
    @Id
    @Column(name = "user_id", length = 36)
    val id: String = UUID.randomUUID().toString(),

    @Column(nullable = false, unique = true, length = 100)
    var email: String,

    @Column(nullable = false, length = 255)
    var password: String,

    @Column(nullable = false, length = 20)
    var role: String,  // Role enum의 name

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true,

    @Column(name = "last_login_at")
    var lastLoginAt: Instant? = null
) {
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant = Instant.now()
        protected set

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()
        protected set
}
