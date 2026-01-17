package com.lms.infrastructure.persistence.entity

import com.lms.domain.model.payroll.BatchStatus
import jakarta.persistence.*
import java.time.Instant

/**
 * 급여 배치 이력 JPA Entity
 */
@Entity
@Table(
    name = "payroll_batch_histories",
    indexes = [
        Index(name = "idx_batch_period", columnList = "period"),
        Index(name = "idx_batch_store_id", columnList = "store_id"),
        Index(name = "idx_batch_started_at", columnList = "started_at")
    ]
)
class PayrollBatchHistoryEntity(
    @Id
    var id: String,

    @Column(nullable = false, length = 7)
    var period: String,

    @Column(name = "store_id", length = 36)
    var storeId: String?,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: BatchStatus,

    @Column(nullable = false)
    var totalCount: Int,

    @Column(nullable = false)
    var successCount: Int,

    @Column(nullable = false)
    var failureCount: Int,

    @Column(nullable = false)
    var startedAt: Instant,

    @Column
    var completedAt: Instant?,

    @Column(columnDefinition = "TEXT")
    var errorMessage: String?
) : BaseEntity()
