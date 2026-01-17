package com.lms.interfaces.web.dto

import com.lms.domain.model.leave.LeaveType
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.LocalDate

/**
 * 휴가 신청 요청
 */
data class LeaveRequestCreateRequest(
    @field:NotNull(message = "휴가 유형은 필수입니다")
    val leaveType: LeaveType,

    @field:NotNull(message = "시작일은 필수입니다")
    val startDate: LocalDate,

    @field:NotNull(message = "종료일은 필수입니다")
    val endDate: LocalDate,

    val reason: String?
)

/**
 * 휴가 반려 요청
 */
data class LeaveRejectionRequest(
    @field:NotBlank(message = "반려 사유는 필수입니다")
    val rejectionReason: String
)
