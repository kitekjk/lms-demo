package com.lms.application.auditlog.dto

import com.lms.domain.model.auditlog.ActionType
import com.lms.domain.model.auditlog.AuditLog
import com.lms.domain.model.auditlog.EntityType
import java.time.Instant

/**
 * 감사로그 생성 Command
 */
data class CreateAuditLogCommand(
    val entityType: EntityType,
    val entityId: String,
    val actionType: ActionType,
    val oldValue: String?,
    val newValue: String?,
    val reason: String?
)

/**
 * 감사로그 조회 결과
 */
data class AuditLogResult(
    val id: String,
    val entityType: String,
    val entityId: String,
    val actionType: String,
    val performedBy: String,
    val performedByName: String,
    val performedAt: Instant,
    val oldValue: String?,
    val newValue: String?,
    val reason: String?,
    val clientIp: String?
) {
    companion object {
        fun from(auditLog: AuditLog): AuditLogResult = AuditLogResult(
            id = auditLog.id.value,
            entityType = auditLog.entityType.value,
            entityId = auditLog.entityId,
            actionType = auditLog.actionType.value,
            performedBy = auditLog.performedBy,
            performedByName = auditLog.performedByName,
            performedAt = auditLog.performedAt,
            oldValue = auditLog.oldValue,
            newValue = auditLog.newValue,
            reason = auditLog.reason,
            clientIp = auditLog.clientIp
        )
    }
}
