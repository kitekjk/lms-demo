package com.lms.infrastructure.persistence.mapper

import com.lms.domain.model.payroll.PayrollBatchHistory
import com.lms.domain.model.payroll.PayrollBatchHistoryId
import com.lms.domain.model.payroll.PayrollPeriod
import com.lms.domain.model.store.StoreId
import com.lms.infrastructure.persistence.entity.PayrollBatchHistoryEntity
import java.time.ZoneId

/**
 * PayrollBatchHistory 도메인 ↔ JPA Entity 매퍼
 */
object PayrollBatchHistoryMapper {
    fun toDomain(entity: PayrollBatchHistoryEntity): PayrollBatchHistory = PayrollBatchHistory.reconstruct(
        id = PayrollBatchHistoryId.from(entity.id),
        period = PayrollPeriod.from(entity.period),
        storeId = entity.storeId?.let { StoreId.from(it) },
        status = entity.status,
        totalCount = entity.totalCount,
        successCount = entity.successCount,
        failureCount = entity.failureCount,
        startedAt = entity.startedAt,
        completedAt = entity.completedAt,
        errorMessage = entity.errorMessage,
        createdAt = entity.createdAt.atZone(ZoneId.systemDefault()).toInstant()
    )

    fun toEntity(domain: PayrollBatchHistory): PayrollBatchHistoryEntity = PayrollBatchHistoryEntity(
        id = domain.id.value,
        period = domain.period.value,
        storeId = domain.storeId?.value,
        status = domain.status,
        totalCount = domain.totalCount,
        successCount = domain.successCount,
        failureCount = domain.failureCount,
        startedAt = domain.startedAt,
        completedAt = domain.completedAt,
        errorMessage = domain.errorMessage
    )
}
