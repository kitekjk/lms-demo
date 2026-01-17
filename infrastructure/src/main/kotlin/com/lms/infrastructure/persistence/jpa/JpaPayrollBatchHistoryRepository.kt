package com.lms.infrastructure.persistence.jpa

import com.lms.infrastructure.persistence.entity.PayrollBatchHistoryEntity
import java.time.Instant
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

/**
 * PayrollBatchHistory JPA Repository
 */
interface JpaPayrollBatchHistoryRepository : JpaRepository<PayrollBatchHistoryEntity, String> {
    /**
     * 매장별 조회
     */
    fun findByStoreIdOrderByStartedAtDesc(storeId: String): List<PayrollBatchHistoryEntity>

    /**
     * 기간별 조회
     */
    fun findByPeriodOrderByStartedAtDesc(period: String): List<PayrollBatchHistoryEntity>

    /**
     * 기간 필터 조회 (startDate와 endDate 사이, 최신순)
     */
    @Query(
        """
        SELECT h FROM PayrollBatchHistoryEntity h
        WHERE (:startDate IS NULL OR h.startedAt >= :startDate)
          AND (:endDate IS NULL OR h.startedAt <= :endDate)
        ORDER BY h.startedAt DESC
        """
    )
    fun findAllByDateRange(
        @Param("startDate") startDate: Instant?,
        @Param("endDate") endDate: Instant?
    ): List<PayrollBatchHistoryEntity>
}
