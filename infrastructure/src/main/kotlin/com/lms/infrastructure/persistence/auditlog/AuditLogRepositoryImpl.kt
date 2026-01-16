package com.lms.infrastructure.persistence.auditlog

import com.lms.domain.model.auditlog.AuditLog
import com.lms.domain.model.auditlog.AuditLogId
import com.lms.domain.model.auditlog.AuditLogRepository
import com.lms.domain.model.auditlog.EntityType
import java.time.LocalDate
import java.time.ZoneId
import org.springframework.stereotype.Repository

@Repository
class AuditLogRepositoryImpl(private val jpaAuditLogRepository: JpaAuditLogRepository) : AuditLogRepository {

    override fun save(auditLog: AuditLog): AuditLog {
        val entity = AuditLogEntity.from(auditLog)
        val saved = jpaAuditLogRepository.save(entity)
        return saved.toDomain()
    }

    override fun findById(id: AuditLogId): AuditLog? = jpaAuditLogRepository.findById(id.value)
        .map { it.toDomain() }
        .orElse(null)

    override fun findByEntityTypeAndEntityId(entityType: EntityType, entityId: String): List<AuditLog> =
        jpaAuditLogRepository.findByEntityTypeAndEntityId(entityType.value, entityId)
            .map { it.toDomain() }

    override fun findByPerformedBy(userId: String): List<AuditLog> = jpaAuditLogRepository.findByPerformedBy(userId)
        .map { it.toDomain() }

    override fun findByDateRange(startDate: LocalDate, endDate: LocalDate): List<AuditLog> {
        val startInstant = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant()
        val endInstant = endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()
        return jpaAuditLogRepository.findByPerformedAtBetween(startInstant, endInstant)
            .map { it.toDomain() }
    }

    override fun findByEntityTypeAndDateRange(
        entityType: EntityType,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<AuditLog> {
        val startInstant = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant()
        val endInstant = endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()
        return jpaAuditLogRepository.findByEntityTypeAndPerformedAtBetween(
            entityType.value,
            startInstant,
            endInstant
        ).map { it.toDomain() }
    }
}
