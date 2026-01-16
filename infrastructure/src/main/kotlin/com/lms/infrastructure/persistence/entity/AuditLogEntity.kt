package com.lms.infrastructure.persistence.entity

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener

/**
 * AuditLog JPA Entity
 * 모든 중요 변경 사항을 추적하는 감사 로그
 */
@Entity
@Table(
    name = "audit_logs",
    indexes = [
        Index(name = "idx_audit_log_entity", columnList = "entity_type,entity_id"),
        Index(name = "idx_audit_log_changed_by", columnList = "changed_by"),
        Index(name = "idx_audit_log_created_at", columnList = "created_at")
    ]
)
@EntityListeners(AuditingEntityListener::class)
class AuditLogEntity(
    @Id
    @Column(nullable = false, length = 36)
    var logId: String = UUID.randomUUID().toString(),

    @Column(name = "entity_type", nullable = false, length = 100)
    var entityType: String,

    @Column(name = "entity_id", nullable = false, length = 100)
    var entityId: String,

    @Column(nullable = false, length = 50)
    var action: String,

    @Column(name = "changed_by", nullable = false, length = 100)
    var changedBy: String,

    @Column(name = "old_value", columnDefinition = "TEXT")
    var oldValue: String? = null,

    @Column(name = "new_value", columnDefinition = "TEXT")
    var newValue: String? = null
) {
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant = Instant.now()
        protected set
}
