package com.lms.infrastructure.persistence.repository

import com.lms.infrastructure.persistence.entity.AuditLogEntity
import java.time.Instant
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

/**
 * AuditLog JPA Repository
 */
@Repository
interface AuditLogJpaRepository : JpaRepository<AuditLogEntity, String> {
    /**
     * Entity 타입으로 감사 로그 조회
     */
    fun findByEntityType(entityType: String): List<AuditLogEntity>

    /**
     * Entity ID로 감사 로그 조회
     */
    fun findByEntityId(entityId: String): List<AuditLogEntity>

    /**
     * Entity 타입과 ID로 조회
     */
    fun findByEntityTypeAndEntityId(entityType: String, entityId: String): List<AuditLogEntity>

    /**
     * 변경자로 조회
     */
    fun findByChangedBy(changedBy: String): List<AuditLogEntity>

    /**
     * 액션 타입으로 조회
     */
    fun findByAction(action: String): List<AuditLogEntity>

    /**
     * 기간별 감사 로그 조회
     */
    @Query("SELECT a FROM AuditLogEntity a WHERE a.createdAt BETWEEN :startTime AND :endTime")
    fun findByDateRange(
        @Param("startTime") startTime: Instant,
        @Param("endTime") endTime: Instant
    ): List<AuditLogEntity>

    /**
     * Entity 타입과 기간으로 조회
     */
    @Query(
        "SELECT a FROM AuditLogEntity a WHERE a.entityType = :entityType AND a.createdAt BETWEEN :startTime AND :endTime"
    )
    fun findByEntityTypeAndDateRange(
        @Param("entityType") entityType: String,
        @Param("startTime") startTime: Instant,
        @Param("endTime") endTime: Instant
    ): List<AuditLogEntity>

    /**
     * 특정 Entity의 최근 변경 이력 조회
     */
    @Query(
        "SELECT a FROM AuditLogEntity a WHERE a.entityType = :entityType AND a.entityId = :entityId ORDER BY a.createdAt DESC"
    )
    fun findRecentByEntityTypeAndId(
        @Param("entityType") entityType: String,
        @Param("entityId") entityId: String
    ): List<AuditLogEntity>

    /**
     * 변경자별 액션 통계 조회
     */
    @Query(
        "SELECT a.changedBy, a.action, COUNT(a) FROM AuditLogEntity a WHERE a.createdAt BETWEEN :startTime AND :endTime GROUP BY a.changedBy, a.action"
    )
    fun getActionStatsByUserAndPeriod(
        @Param("startTime") startTime: Instant,
        @Param("endTime") endTime: Instant
    ): List<Array<Any>>

    /**
     * Entity 타입별 변경 통계
     */
    @Query(
        "SELECT a.entityType, COUNT(a) FROM AuditLogEntity a WHERE a.createdAt BETWEEN :startTime AND :endTime GROUP BY a.entityType"
    )
    fun getChangeStatsByEntityTypeAndPeriod(
        @Param("startTime") startTime: Instant,
        @Param("endTime") endTime: Instant
    ): List<Array<Any>>
}
