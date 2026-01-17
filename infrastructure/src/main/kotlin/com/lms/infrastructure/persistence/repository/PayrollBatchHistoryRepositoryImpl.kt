package com.lms.infrastructure.persistence.repository

import com.lms.domain.model.payroll.PayrollBatchHistory
import com.lms.domain.model.payroll.PayrollBatchHistoryId
import com.lms.domain.model.payroll.PayrollBatchHistoryRepository
import com.lms.domain.model.payroll.PayrollPeriod
import com.lms.domain.model.store.StoreId
import com.lms.infrastructure.persistence.jpa.JpaPayrollBatchHistoryRepository
import com.lms.infrastructure.persistence.mapper.PayrollBatchHistoryMapper
import java.time.Instant
import org.springframework.stereotype.Repository

/**
 * PayrollBatchHistoryRepository 구현체
 */
@Repository
class PayrollBatchHistoryRepositoryImpl(private val jpaRepository: JpaPayrollBatchHistoryRepository) :
    PayrollBatchHistoryRepository {
    override fun save(history: PayrollBatchHistory): PayrollBatchHistory {
        val entity = PayrollBatchHistoryMapper.toEntity(history)
        val saved = jpaRepository.save(entity)
        return PayrollBatchHistoryMapper.toDomain(saved)
    }

    override fun findById(id: PayrollBatchHistoryId): PayrollBatchHistory? = jpaRepository.findById(id.value)
        .map { PayrollBatchHistoryMapper.toDomain(it) }
        .orElse(null)

    override fun findAll(startDate: Instant?, endDate: Instant?): List<PayrollBatchHistory> =
        jpaRepository.findAllByDateRange(startDate, endDate)
            .map { PayrollBatchHistoryMapper.toDomain(it) }

    override fun findByStoreId(storeId: StoreId): List<PayrollBatchHistory> =
        jpaRepository.findByStoreIdOrderByStartedAtDesc(storeId.value)
            .map { PayrollBatchHistoryMapper.toDomain(it) }

    override fun findByPeriod(period: PayrollPeriod): List<PayrollBatchHistory> =
        jpaRepository.findByPeriodOrderByStartedAtDesc(period.value)
            .map { PayrollBatchHistoryMapper.toDomain(it) }
}
