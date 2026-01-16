package com.lms.interfaces.web.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.Instant

/**
 * 출근 요청 DTO
 */
data class CheckInRequest(val workScheduleId: String? = null)

/**
 * 퇴근 요청 DTO
 */
data class CheckOutRequest(
    // 현재는 필드 없음, 향후 확장 가능
    val note: String? = null
)

/**
 * 출퇴근 기록 수정 요청 DTO (관리자용)
 */
data class AttendanceAdjustRequest(
    @field:NotNull(message = "수정된 출근 시간은 필수입니다")
    val adjustedCheckInTime: Instant,

    val adjustedCheckOutTime: Instant?,

    @field:NotBlank(message = "수정 사유는 필수입니다")
    val reason: String
)
