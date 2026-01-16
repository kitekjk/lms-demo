package com.lms.domain.model.auditlog

import java.time.LocalDate

/**
 * 감사로그 Repository 인터페이스
 */
interface AuditLogRepository {
    /**
     * 감사로그 저장
     */
    fun save(auditLog: AuditLog): AuditLog

    /**
     * ID로 감사로그 조회
     */
    fun findById(id: AuditLogId): AuditLog?

    /**
     * 엔티티별 감사로그 조회
     */
    fun findByEntityTypeAndEntityId(entityType: EntityType, entityId: String): List<AuditLog>

    /**
     * 사용자별 감사로그 조회
     */
    fun findByPerformedBy(userId: String): List<AuditLog>

    /**
     * 기간별 감사로그 조회
     */
    fun findByDateRange(startDate: LocalDate, endDate: LocalDate): List<AuditLog>

    /**
     * 엔티티 + 기간별 감사로그 조회
     */
    fun findByEntityTypeAndDateRange(entityType: EntityType, startDate: LocalDate, endDate: LocalDate): List<AuditLog>
}
