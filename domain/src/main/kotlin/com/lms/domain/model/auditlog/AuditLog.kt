package com.lms.domain.model.auditlog

import com.lms.domain.common.DomainContext
import java.time.Instant

/**
 * 감사로그 Aggregate Root
 * 시스템 내 중요 데이터 변경 이력을 추적
 */
data class AuditLog private constructor(
    val id: AuditLogId,
    val entityType: EntityType,
    val entityId: String,
    val actionType: ActionType,
    val performedBy: String,
    val performedByName: String,
    val performedAt: Instant,
    val oldValue: String?,
    val newValue: String?,
    val reason: String?,
    val clientIp: String?
) {
    companion object {
        /**
         * 감사로그 생성
         *
         * @param context 도메인 컨텍스트
         * @param entityType 대상 엔티티 타입
         * @param entityId 대상 엔티티 ID
         * @param actionType 액션 타입
         * @param oldValue 변경 전 값 (JSON 형식)
         * @param newValue 변경 후 값 (JSON 형식)
         * @param reason 변경 사유
         */
        fun create(
            context: DomainContext,
            entityType: EntityType,
            entityId: String,
            actionType: ActionType,
            oldValue: String? = null,
            newValue: String? = null,
            reason: String? = null
        ): AuditLog = AuditLog(
            id = AuditLogId.generate(),
            entityType = entityType,
            entityId = entityId,
            actionType = actionType,
            performedBy = context.userId,
            performedByName = context.userName,
            performedAt = context.requestedAt,
            oldValue = oldValue,
            newValue = newValue,
            reason = reason,
            clientIp = context.clientIp
        )
    }
}
