package com.lms.application.leave.dto

import com.lms.domain.model.leave.LeaveRequest
import com.lms.domain.model.leave.LeaveStatus
import com.lms.domain.model.leave.LeaveType
import java.time.Instant
import java.time.LocalDate

/**
 * 휴가 신청 Command
 */
data class CreateLeaveRequestCommand(
    val employeeId: String,
    val leaveType: LeaveType,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val reason: String?
)

/**
 * 휴가 반려 Command
 */
data class RejectLeaveRequestCommand(val rejectionReason: String)

/**
 * 휴가 신청 Result
 */
data class LeaveRequestResult(
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
        fun from(leaveRequest: LeaveRequest): LeaveRequestResult = LeaveRequestResult(
            id = leaveRequest.id.value,
            employeeId = leaveRequest.employeeId.value,
            leaveType = leaveRequest.leaveType,
            startDate = leaveRequest.leavePeriod.startDate,
            endDate = leaveRequest.leavePeriod.endDate,
            requestedDays = leaveRequest.calculateLeaveDays(),
            reason = leaveRequest.reason,
            status = leaveRequest.status,
            rejectionReason = leaveRequest.rejectionReason,
            approvedBy = leaveRequest.approvedBy?.value,
            approvedAt = leaveRequest.approvedAt,
            createdAt = leaveRequest.createdAt
        )
    }
}
