package com.lms.domain.model.leave

import com.lms.domain.common.DomainContext
import com.lms.domain.model.employee.EmployeeId
import com.lms.domain.model.user.UserId
import java.time.Instant

/**
 * 휴가 신청 Aggregate Root
 * 근로자의 휴가 신청 및 승인 프로세스를 관리
 */
data class LeaveRequest private constructor(
    val id: LeaveRequestId,
    val employeeId: EmployeeId,
    val leaveType: LeaveType,
    val leavePeriod: LeavePeriod,
    val status: LeaveStatus,
    val reason: String?,
    val approvedBy: UserId?,
    val approvedAt: Instant?,
    val rejectionReason: String?,
    val createdAt: Instant
) {
    companion object {
        /**
         * 새로운 휴가 신청 생성
         */
        fun create(
            context: DomainContext,
            employeeId: EmployeeId,
            leaveType: LeaveType,
            leavePeriod: LeavePeriod,
            reason: String?
        ): LeaveRequest {
            return LeaveRequest(
                id = LeaveRequestId.generate(),
                employeeId = employeeId,
                leaveType = leaveType,
                leavePeriod = leavePeriod,
                status = LeaveStatus.PENDING,
                reason = reason,
                approvedBy = null,
                approvedAt = null,
                rejectionReason = null,
                createdAt = context.requestedAt
            )
        }

        /**
         * 기존 휴가 신청 재구성 (Repository에서 조회 시)
         */
        fun reconstruct(
            id: LeaveRequestId,
            employeeId: EmployeeId,
            leaveType: LeaveType,
            leavePeriod: LeavePeriod,
            status: LeaveStatus,
            reason: String?,
            approvedBy: UserId?,
            approvedAt: Instant?,
            rejectionReason: String?,
            createdAt: Instant
        ): LeaveRequest = LeaveRequest(
            id, employeeId, leaveType, leavePeriod, status,
            reason, approvedBy, approvedAt, rejectionReason, createdAt
        )
    }

    /**
     * 휴가 신청 승인
     */
    fun approve(context: DomainContext, approverId: UserId): LeaveRequest {
        require(status == LeaveStatus.PENDING) {
            "대기 중인 휴가 신청만 승인할 수 있습니다. 현재 상태: ${status.description}"
        }

        return this.copy(
            status = LeaveStatus.APPROVED,
            approvedBy = approverId,
            approvedAt = context.requestedAt,
            rejectionReason = null
        )
    }

    /**
     * 휴가 신청 거부
     */
    fun reject(context: DomainContext, approverId: UserId, rejectionReason: String): LeaveRequest {
        require(status == LeaveStatus.PENDING) {
            "대기 중인 휴가 신청만 거부할 수 있습니다. 현재 상태: ${status.description}"
        }
        require(rejectionReason.isNotBlank()) {
            "거부 사유는 필수입니다."
        }

        return this.copy(
            status = LeaveStatus.REJECTED,
            approvedBy = approverId,
            approvedAt = context.requestedAt,
            rejectionReason = rejectionReason
        )
    }

    /**
     * 휴가 신청 취소
     */
    fun cancel(context: DomainContext): LeaveRequest {
        require(status == LeaveStatus.PENDING || status == LeaveStatus.APPROVED) {
            "대기 중이거나 승인된 휴가 신청만 취소할 수 있습니다. 현재 상태: ${status.description}"
        }

        return this.copy(status = LeaveStatus.CANCELLED)
    }

    /**
     * 휴가 일수 계산
     */
    fun calculateLeaveDays(): Long = leavePeriod.calculateDays()

    /**
     * 승인 필요 여부
     */
    fun requiresApproval(): Boolean = leaveType.requiresApproval

    /**
     * 승인됨 여부
     */
    fun isApproved(): Boolean = status == LeaveStatus.APPROVED

    /**
     * 다른 휴가 신청과 기간이 겹치는지 확인
     */
    fun overlapsWith(other: LeaveRequest): Boolean {
        return this.leavePeriod.overlapsWith(other.leavePeriod)
    }
}
