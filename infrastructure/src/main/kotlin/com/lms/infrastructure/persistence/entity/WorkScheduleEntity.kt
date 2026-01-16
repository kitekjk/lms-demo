package com.lms.infrastructure.persistence.entity

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.util.*

/**
 * 근무 일정 JPA Entity
 */
@Entity
@Table(
    name = "work_schedules",
    indexes = [
        Index(name = "idx_work_schedule_employee", columnList = "employee_id"),
        Index(name = "idx_work_schedule_store", columnList = "store_id"),
        Index(name = "idx_work_schedule_date", columnList = "work_date")
    ]
)
@EntityListeners(AuditingEntityListener::class)
class WorkScheduleEntity(
    @Id
    @Column(name = "work_schedule_id", nullable = false, length = 36)
    val id: String = UUID.randomUUID().toString(),

    @Column(name = "employee_id", nullable = false, length = 36)
    var employeeId: String,

    @Column(name = "store_id", nullable = false, length = 36)
    var storeId: String,

    @Column(name = "work_date", nullable = false)
    var workDate: LocalDate,

    @Column(name = "start_time", nullable = false)
    var startTime: LocalTime,

    @Column(name = "end_time", nullable = false)
    var endTime: LocalTime,

    @Column(name = "is_confirmed", nullable = false)
    var isConfirmed: Boolean = false
) {
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant = Instant.now()
        protected set

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()
        protected set

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is WorkScheduleEntity) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String {
        return "WorkScheduleEntity(id='$id', employeeId='$employeeId', workDate=$workDate)"
    }
}
