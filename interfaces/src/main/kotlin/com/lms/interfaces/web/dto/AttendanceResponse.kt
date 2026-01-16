package com.lms.interfaces.web.dto

import com.lms.domain.model.attendance.AttendanceStatus
import java.time.Instant
import java.time.LocalDate

/**
 * 출퇴근 기록 응답 DTO
 */
data class AttendanceRecordResponse(
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
)

/**
 * 출퇴근 기록 목록 응답 DTO
 */
data class AttendanceRecordListResponse(val records: List<AttendanceRecordResponse>, val totalCount: Int)
