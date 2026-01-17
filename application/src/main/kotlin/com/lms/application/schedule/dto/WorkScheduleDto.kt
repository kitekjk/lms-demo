package com.lms.application.schedule.dto

import com.lms.domain.model.schedule.WorkSchedule
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

/**
 * 근무 일정 생성 커맨드
 */
data class CreateWorkScheduleCommand(
    val employeeId: String,
    val storeId: String,
    val workDate: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime
)

/**
 * 근무 일정 수정 커맨드
 */
data class UpdateWorkScheduleCommand(val workDate: LocalDate?, val startTime: LocalTime?, val endTime: LocalTime?)

/**
 * 근무 일정 결과 DTO
 */
data class WorkScheduleResult(
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
        fun from(schedule: WorkSchedule): WorkScheduleResult = WorkScheduleResult(
            id = schedule.id.value,
            employeeId = schedule.employeeId.value,
            storeId = schedule.storeId.value,
            workDate = schedule.workDate.value,
            startTime = schedule.workTime.startTime,
            endTime = schedule.workTime.endTime,
            workHours = schedule.calculateWorkHours(),
            isConfirmed = schedule.isConfirmed,
            isWeekendWork = schedule.isWeekendWork(),
            createdAt = schedule.createdAt
        )
    }
}
