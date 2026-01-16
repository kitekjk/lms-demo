package com.lms.infrastructure.persistence.entity

import jakarta.persistence.*
import java.time.Instant
import java.util.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener

/**
 * Store JPA Entity
 * 매장 정보 (Store Aggregate에 대응)
 */
@Entity
@Table(
    name = "stores",
    indexes = [Index(name = "idx_store_name", columnList = "name")]
)
@EntityListeners(AuditingEntityListener::class)
class StoreEntity(

    @Id
    @Column(name = "store_id", nullable = false, length = 36)
    val id: String = UUID.randomUUID().toString(),

    @Column(nullable = false, unique = true, length = 100)
    var name: String,

    @Column(nullable = false, length = 200)
    var location: String

) {
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant = Instant.now()
        protected set

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()
        protected set

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is StoreEntity) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String = "StoreEntity(id='$id', name='$name', location='$location')"
}
