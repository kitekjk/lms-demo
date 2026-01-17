package com.lms.interfaces.web.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.LocalDate
import java.time.LocalTime

/**
 * 근무 일정 생성 요청 DTO
 */
data class WorkScheduleCreateRequest(
    @field:NotBlank(message = "근로자 ID는 필수입니다")
    val employeeId: String,

    @field:NotBlank(message = "매장 ID는 필수입니다")
    val storeId: String,

    @field:NotNull(message = "근무 날짜는 필수입니다")
    val workDate: LocalDate,

    @field:NotNull(message = "근무 시작 시간은 필수입니다")
    val startTime: LocalTime,

    @field:NotNull(message = "근무 종료 시간은 필수입니다")
    val endTime: LocalTime
)

/**
 * 근무 일정 수정 요청 DTO
 */
data class WorkScheduleUpdateRequest(val workDate: LocalDate?, val startTime: LocalTime?, val endTime: LocalTime?)
