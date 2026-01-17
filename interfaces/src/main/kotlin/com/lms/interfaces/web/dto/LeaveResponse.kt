package com.lms.interfaces.web.dto

import com.lms.application.leave.dto.LeaveRequestResult
import com.lms.domain.model.leave.LeaveStatus
import com.lms.domain.model.leave.LeaveType
import java.time.Instant
import java.time.LocalDate

/**
 * 휴가 신청 응답
 */
data class LeaveRequestResponse(
    val id: String,
    val employeeId: String,
    val leaveType: LeaveType,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val requestedDays: Long,
    val reason: String?,
    val status: LeaveStatus,
    val rejectionReason: String?,
    val approvedBy: String?,
    val approvedAt: Instant?,
    val createdAt: Instant
) {
    companion object {
        fun from(result: LeaveRequestResult): LeaveRequestResponse = LeaveRequestResponse(
            id = result.id,
            employeeId = result.employeeId,
            leaveType = result.leaveType,
            startDate = result.startDate,
            endDate = result.endDate,
            requestedDays = result.requestedDays,
            reason = result.reason,
            status = result.status,
            rejectionReason = result.rejectionReason,
            approvedBy = result.approvedBy,
            approvedAt = result.approvedAt,
            createdAt = result.createdAt
        )
    }
}

/**
 * 휴가 신청 목록 응답
 */
data class LeaveRequestListResponse(val requests: List<LeaveRequestResponse>, val totalCount: Int)
