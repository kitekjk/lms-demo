package com.lms.application.payroll.dto

import com.lms.domain.model.payroll.BatchStatus
import com.lms.domain.model.payroll.PayrollBatchHistory
import java.time.Instant
import java.time.YearMonth

/**
 * 배치 실행 커맨드
 */
data class ExecutePayrollBatchCommand(val period: YearMonth, val storeId: String?)

/**
 * 배치 이력 결과
 */
data class PayrollBatchHistoryResult(
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
        fun from(history: PayrollBatchHistory): PayrollBatchHistoryResult = PayrollBatchHistoryResult(
            id = history.id.value,
            period = history.period.toYearMonth(),
            storeId = history.storeId?.value,
            status = history.status,
            totalCount = history.totalCount,
            successCount = history.successCount,
            failureCount = history.failureCount,
            startedAt = history.startedAt,
            completedAt = history.completedAt,
            errorMessage = history.errorMessage,
            createdAt = history.createdAt
        )
    }
}
