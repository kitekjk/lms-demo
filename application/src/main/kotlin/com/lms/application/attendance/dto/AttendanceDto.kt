package com.lms.application.attendance.dto

import com.lms.domain.model.attendance.AttendanceRecord
import com.lms.domain.model.attendance.AttendanceStatus
import java.time.Instant
import java.time.LocalDate

/**
 * 출근 Command
 */
data class CheckInCommand(val employeeId: String, val workScheduleId: String?)

/**
 * 퇴근 Command
 */
data class CheckOutCommand(val employeeId: String)

/**
 * 출퇴근 기록 수정 Command
 */
data class AdjustAttendanceCommand(
    val adjustedCheckInTime: Instant,
    val adjustedCheckOutTime: Instant?,
    val reason: String
)

/**
 * 출퇴근 기록 조회 Result
 */
data class AttendanceRecordResult(
    val id: String,
    val employeeId: String,
    val workScheduleId: String?,
    val attendanceDate: LocalDate,
    val checkInTime: Instant,
    val checkOutTime: Instant?,
    val actualWorkHours: Double?,
    val status: AttendanceStatus,
    val note: String?,
    val createdAt: Instant
) {
    companion object {
        fun from(record: AttendanceRecord): AttendanceRecordResult = AttendanceRecordResult(
            id = record.id.value,
            employeeId = record.employeeId.value,
            workScheduleId = record.workScheduleId?.value,
            attendanceDate = record.attendanceDate,
            checkInTime = record.attendanceTime.checkInTime,
            checkOutTime = record.attendanceTime.checkOutTime,
            actualWorkHours = record.calculateActualWorkHours(),
            status = record.status,
            note = record.note,
            createdAt = record.createdAt
        )
    }
}
