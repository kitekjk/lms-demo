package com.lms.domain.model.payroll

import com.lms.domain.model.store.StoreId
import java.time.Instant

/**
 * 급여 배치 이력 Repository 인터페이스
 */
interface PayrollBatchHistoryRepository {
    /**
     * 배치 이력 저장
     */
    fun save(history: PayrollBatchHistory): PayrollBatchHistory

    /**
     * 배치 이력 조회 (ID)
     */
    fun findById(id: PayrollBatchHistoryId): PayrollBatchHistory?

    /**
     * 배치 이력 전체 조회 (기간 필터, 최신순)
     */
    fun findAll(startDate: Instant?, endDate: Instant?): List<PayrollBatchHistory>

    /**
     * 배치 이력 조회 (매장별)
     */
    fun findByStoreId(storeId: StoreId): List<PayrollBatchHistory>

    /**
     * 배치 이력 조회 (기간별)
     */
    fun findByPeriod(period: PayrollPeriod): List<PayrollBatchHistory>
}
