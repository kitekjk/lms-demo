package com.lms.application.leave

import com.lms.application.leave.dto.LeaveRequestResult
import com.lms.domain.model.leave.LeaveRequestRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 대기 중인 휴가 신청 조회 UseCase (관리자용)
 */
@Service
@Transactional(readOnly = true)
class GetPendingLeaveRequestsAppService(private val leaveRequestRepository: LeaveRequestRepository) {
    fun execute(): List<LeaveRequestResult> {
        val pendingRequests = leaveRequestRepository.findPendingRequests()
        return pendingRequests.map { LeaveRequestResult.from(it) }
    }
}
