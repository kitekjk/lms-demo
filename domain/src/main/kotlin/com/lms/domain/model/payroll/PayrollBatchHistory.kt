package com.lms.domain.model.payroll

import com.lms.domain.common.DomainContext
import com.lms.domain.model.store.StoreId
import java.time.Instant

/**
 * 급여 배치 실행 이력
 */
data class PayrollBatchHistory private constructor(
    val id: PayrollBatchHistoryId,
    val period: PayrollPeriod,
    val storeId: StoreId?,
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
        /**
         * 배치 시작 시 생성
         */
        fun start(
            context: DomainContext,
            period: PayrollPeriod,
            storeId: StoreId?,
            totalCount: Int
        ): PayrollBatchHistory = PayrollBatchHistory(
            id = PayrollBatchHistoryId.generate(),
            period = period,
            storeId = storeId,
            status = BatchStatus.RUNNING,
            totalCount = totalCount,
            successCount = 0,
            failureCount = 0,
            startedAt = context.requestedAt,
            completedAt = null,
            errorMessage = null,
            createdAt = context.requestedAt
        )

        /**
         * 기존 이력 재구성
         */
        fun reconstruct(
            id: PayrollBatchHistoryId,
            period: PayrollPeriod,
            storeId: StoreId?,
            status: BatchStatus,
            totalCount: Int,
            successCount: Int,
            failureCount: Int,
            startedAt: Instant,
            completedAt: Instant?,
            errorMessage: String?,
            createdAt: Instant
        ): PayrollBatchHistory = PayrollBatchHistory(
            id,
            period,
            storeId,
            status,
            totalCount,
            successCount,
            failureCount,
            startedAt,
            completedAt,
            errorMessage,
            createdAt
        )
    }

    /**
     * 배치 완료 처리
     */
    fun complete(context: DomainContext, successCount: Int, failureCount: Int): PayrollBatchHistory {
        require(status == BatchStatus.RUNNING) { "실행 중인 배치만 완료할 수 있습니다." }

        return this.copy(
            status = if (failureCount == 0) BatchStatus.COMPLETED else BatchStatus.PARTIAL_SUCCESS,
            successCount = successCount,
            failureCount = failureCount,
            completedAt = context.requestedAt
        )
    }

    /**
     * 배치 실패 처리
     */
    fun fail(context: DomainContext, errorMessage: String): PayrollBatchHistory {
        require(status == BatchStatus.RUNNING) { "실행 중인 배치만 실패 처리할 수 있습니다." }

        return this.copy(
            status = BatchStatus.FAILED,
            completedAt = context.requestedAt,
            errorMessage = errorMessage
        )
    }
}

/**
 * 배치 상태
 */
enum class BatchStatus {
    RUNNING, // 실행 중
    COMPLETED, // 완료 (모두 성공)
    PARTIAL_SUCCESS, // 부분 성공 (일부 실패 있음)
    FAILED // 실패
}

/**
 * 배치 이력 ID
 */
@JvmInline
value class PayrollBatchHistoryId private constructor(val value: String) {
    companion object {
        fun generate(): PayrollBatchHistoryId = PayrollBatchHistoryId(java.util.UUID.randomUUID().toString())

        fun from(value: String): PayrollBatchHistoryId {
            require(value.isNotBlank()) { "배치 이력 ID는 비어있을 수 없습니다." }
            return PayrollBatchHistoryId(value)
        }
    }
}
