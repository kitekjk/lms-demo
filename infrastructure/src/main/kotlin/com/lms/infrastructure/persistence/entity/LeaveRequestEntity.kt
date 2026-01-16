package com.lms.infrastructure.persistence.entity

import com.lms.domain.model.leave.LeaveStatus
import com.lms.domain.model.leave.LeaveType
import jakarta.persistence.*
import java.time.Instant
import java.time.LocalDate
import java.util.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener

/**
 * 휴가 신청 JPA Entity
 */
@Entity
@Table(
    name = "leave_requests",
    indexes = [
        Index(name = "idx_leave_employee", columnList = "employee_id"),
        Index(name = "idx_leave_status", columnList = "status"),
        Index(name = "idx_leave_dates", columnList = "start_date,end_date"),
        Index(name = "idx_leave_employee_status", columnList = "employee_id,status"),
        Index(name = "idx_leave_type", columnList = "leave_type")
    ]
)
@EntityListeners(AuditingEntityListener::class)
class LeaveRequestEntity(
    @Id
    @Column(name = "leave_request_id", nullable = false, length = 36)
    val id: String = UUID.randomUUID().toString(),

    @Column(name = "employee_id", nullable = false, length = 36)
    var employeeId: String,

    @Column(name = "leave_type", nullable = false, length = 20)
    var leaveType: LeaveType,

    @Column(name = "start_date", nullable = false)
    var startDate: LocalDate,

    @Column(name = "end_date", nullable = false)
    var endDate: LocalDate,

    @Column(name = "status", nullable = false, length = 20)
    var status: LeaveStatus,

    @Column(name = "reason", length = 500)
    var reason: String? = null,

    @Column(name = "approved_by", length = 36)
    var approvedBy: String? = null,

    @Column(name = "approved_at")
    var approvedAt: Instant? = null,

    @Column(name = "rejection_reason", length = 500)
    var rejectionReason: String? = null
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
        if (other !is LeaveRequestEntity) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String = "LeaveRequestEntity(id='$id', employeeId='$employeeId', leaveType='$leaveType')"
}
