package com.lms.infrastructure.persistence.auditlog

import com.lms.domain.model.auditlog.ActionType
import com.lms.domain.model.auditlog.AuditLog
import com.lms.domain.model.auditlog.AuditLogId
import com.lms.domain.model.auditlog.EntityType
import com.lms.infrastructure.persistence.entity.BaseEntity
import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(
    name = "audit_logs",
    indexes = [
        Index(name = "idx_audit_log_entity", columnList = "entity_type,entity_id"),
        Index(name = "idx_audit_log_performed_by", columnList = "performed_by"),
        Index(name = "idx_audit_log_performed_at", columnList = "performed_at")
    ]
)
class AuditLogEntity(
    @Id
    var id: String,

    @Column(nullable = false, length = 50)
    var entityType: EntityType,

    @Column(nullable = false, length = 100)
    var entityId: String,

    @Column(nullable = false, length = 50)
    var actionType: ActionType,

    @Column(nullable = false, length = 100)
    var performedBy: String,

    @Column(nullable = false, length = 100)
    var performedByName: String,

    @Column(nullable = false)
    var performedAt: Instant,

    @Column(columnDefinition = "TEXT")
    var oldValue: String?,

    @Column(columnDefinition = "TEXT")
    var newValue: String?,

    @Column(length = 500)
    var reason: String?,

    @Column(length = 50)
    var clientIp: String?
) : BaseEntity() {

    companion object {
        fun from(auditLog: AuditLog): AuditLogEntity = AuditLogEntity(
            id = auditLog.id.value,
            entityType = auditLog.entityType,
            entityId = auditLog.entityId,
            actionType = auditLog.actionType,
            performedBy = auditLog.performedBy,
            performedByName = auditLog.performedByName,
            performedAt = auditLog.performedAt,
            oldValue = auditLog.oldValue,
            newValue = auditLog.newValue,
            reason = auditLog.reason,
            clientIp = auditLog.clientIp
        )
    }

    fun toDomain(): AuditLog = AuditLog::class.java
        .getDeclaredConstructor(
            AuditLogId::class.java,
            EntityType::class.java,
            String::class.java,
            ActionType::class.java,
            String::class.java,
            String::class.java,
            Instant::class.java,
            String::class.java,
            String::class.java,
            String::class.java,
            String::class.java
        )
        .apply { isAccessible = true }
        .newInstance(
            AuditLogId(id),
            entityType,
            entityId,
            actionType,
            performedBy,
            performedByName,
            performedAt,
            oldValue,
            newValue,
            reason,
            clientIp
        )
}
