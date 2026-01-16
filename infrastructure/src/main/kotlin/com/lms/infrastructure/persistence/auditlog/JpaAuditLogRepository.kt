package com.lms.infrastructure.persistence.auditlog

import java.time.Instant
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface JpaAuditLogRepository : JpaRepository<AuditLogEntity, String> {

    @Query(
        "SELECT a FROM AuditLogEntity a WHERE a.entityType = :entityType AND a.entityId = :entityId ORDER BY a.performedAt DESC"
    )
    fun findByEntityTypeAndEntityId(
        @Param("entityType") entityType: String,
        @Param("entityId") entityId: String
    ): List<AuditLogEntity>

    @Query("SELECT a FROM AuditLogEntity a WHERE a.performedBy = :userId ORDER BY a.performedAt DESC")
    fun findByPerformedBy(@Param("userId") userId: String): List<AuditLogEntity>

    @Query(
        "SELECT a FROM AuditLogEntity a WHERE a.performedAt BETWEEN :startDate AND :endDate ORDER BY a.performedAt DESC"
    )
    fun findByPerformedAtBetween(
        @Param("startDate") startDate: Instant,
        @Param("endDate") endDate: Instant
    ): List<AuditLogEntity>

    @Query(
        "SELECT a FROM AuditLogEntity a WHERE a.entityType = :entityType AND a.performedAt BETWEEN :startDate AND :endDate ORDER BY a.performedAt DESC"
    )
    fun findByEntityTypeAndPerformedAtBetween(
        @Param("entityType") entityType: String,
        @Param("startDate") startDate: Instant,
        @Param("endDate") endDate: Instant
    ): List<AuditLogEntity>
}
