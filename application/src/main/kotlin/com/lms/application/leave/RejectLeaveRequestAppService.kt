package com.lms.application.leave

import com.lms.application.leave.dto.LeaveRequestResult
import com.lms.application.leave.dto.RejectLeaveRequestCommand
import com.lms.domain.common.DomainContext
import com.lms.domain.exception.LeaveRequestCannotBeProcessedException
import com.lms.domain.exception.LeaveRequestNotFoundException
import com.lms.domain.model.leave.LeaveRequestId
import com.lms.domain.model.leave.LeaveRequestRepository
import com.lms.domain.model.leave.LeaveStatus
import com.lms.domain.model.user.UserId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 휴가 반려 UseCase
 */
@Service
@Transactional
class RejectLeaveRequestAppService(private val leaveRequestRepository: LeaveRequestRepository) {
    fun execute(
        context: DomainContext,
        leaveRequestId: String,
        command: RejectLeaveRequestCommand
    ): LeaveRequestResult {
        val requestId = LeaveRequestId.from(leaveRequestId)
        val rejectorId = UserId.from(context.userId)

        // 1. 휴가 신청 조회
        var leaveRequest = leaveRequestRepository.findById(requestId)
            ?: throw LeaveRequestNotFoundException(leaveRequestId)

        // 2. 상태 검증 (PENDING만 반려 가능)
        if (leaveRequest.status != LeaveStatus.PENDING) {
            throw LeaveRequestCannotBeProcessedException(
                leaveRequestId,
                leaveRequest.status.name
            )
        }

        // 3. 휴가 반려 처리
        leaveRequest = leaveRequest.reject(context, rejectorId, command.rejectionReason)

        // 4. 저장
        val savedLeaveRequest = leaveRequestRepository.save(leaveRequest)

        return LeaveRequestResult.from(savedLeaveRequest)
    }
}
