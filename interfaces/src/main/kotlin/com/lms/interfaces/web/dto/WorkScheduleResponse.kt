package com.lms.interfaces.web.dto

import com.lms.application.schedule.dto.WorkScheduleResult
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

/**
 * 근무 일정 응답 DTO
 */
data class WorkScheduleResponse(
    val id: String,
    val employeeId: String,
    val storeId: String,
    val workDate: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val workHours: Double,
    val isConfirmed: Boolean,
    val isWeekendWork: Boolean,
    val createdAt: Instant
) {
    companion object {
        fun from(result: WorkScheduleResult): WorkScheduleResponse = WorkScheduleResponse(
            id = result.id,
            employeeId = result.employeeId,
            storeId = result.storeId,
            workDate = result.workDate,
            startTime = result.startTime,
            endTime = result.endTime,
            workHours = result.workHours,
            isConfirmed = result.isConfirmed,
            isWeekendWork = result.isWeekendWork,
            createdAt = result.createdAt
        )
    }
}

/**
 * 근무 일정 목록 응답 DTO
 */
data class WorkScheduleListResponse(val schedules: List<WorkScheduleResponse>, val totalCount: Int)
