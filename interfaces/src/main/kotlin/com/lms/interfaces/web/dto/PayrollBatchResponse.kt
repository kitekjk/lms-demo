package com.lms.interfaces.web.dto

import com.lms.application.payroll.dto.PayrollBatchHistoryResult
import com.lms.domain.model.payroll.BatchStatus
import java.time.Instant
import java.time.YearMonth

/**
 * 급여 배치 이력 응답
 */
data class PayrollBatchHistoryResponse(
    val id: String,
    val period: YearMonth,
    val storeId: String?,
    val status: BatchStatus,
    val totalCount: Int,
    val successCount: Int,
    val failureCount: Int,
    val startedAt: Instant,
    val completedAt: Instant?,
    val errorMessage: String?,
    val createdAt: Instant
) {
    companion object {
        fun from(result: PayrollBatchHistoryResult): PayrollBatchHistoryResponse = PayrollBatchHistoryResponse(
            id = result.id,
            period = result.period,
            storeId = result.storeId,
            status = result.status,
            totalCount = result.totalCount,
            successCount = result.successCount,
            failureCount = result.failureCount,
            startedAt = result.startedAt,
            completedAt = result.completedAt,
            errorMessage = result.errorMessage,
            createdAt = result.createdAt
        )
    }
}
