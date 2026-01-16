package com.lms.infrastructure.persistence.entity

import com.lms.domain.model.attendance.AttendanceStatus
import jakarta.persistence.*
import java.time.Instant
import java.time.LocalDate
import java.util.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener

/**
 * 출퇴근 기록 JPA Entity
 */
@Entity
@Table(
    name = "attendance_records",
    indexes = [
        Index(name = "idx_attendance_employee", columnList = "employee_id"),
        Index(name = "idx_attendance_date", columnList = "attendance_date"),
        Index(name = "idx_attendance_status", columnList = "status")
    ]
)
@EntityListeners(AuditingEntityListener::class)
class AttendanceRecordEntity(
    @Id
    @Column(name = "attendance_record_id", nullable = false, length = 36)
    val id: String = UUID.randomUUID().toString(),

    @Column(name = "employee_id", nullable = false, length = 36)
    var employeeId: String,

    @Column(name = "work_schedule_id", length = 36)
    var workScheduleId: String? = null,

    @Column(name = "attendance_date", nullable = false)
    var attendanceDate: LocalDate,

    @Column(name = "check_in_time", nullable = false)
    var checkInTime: Instant,

    @Column(name = "check_out_time")
    var checkOutTime: Instant? = null,

    @Column(name = "status", nullable = false, length = 20)
    var status: AttendanceStatus,

    @Column(name = "note", length = 500)
    var note: String? = null
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
        if (other !is AttendanceRecordEntity) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String = "AttendanceRecordEntity(id='$id', employeeId='$employeeId', date=$attendanceDate)"
}
