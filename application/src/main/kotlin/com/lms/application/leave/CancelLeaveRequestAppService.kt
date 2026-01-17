package com.lms.application.leave

import com.lms.application.leave.dto.LeaveRequestResult
import com.lms.domain.common.DomainContext
import com.lms.domain.exception.EmployeeNotFoundException
import com.lms.domain.exception.LeaveRequestCannotBeCancelledException
import com.lms.domain.exception.LeaveRequestNotFoundException
import com.lms.domain.model.employee.EmployeeRepository
import com.lms.domain.model.leave.LeaveRequestId
import com.lms.domain.model.leave.LeaveRequestRepository
import com.lms.domain.model.leave.LeaveStatus
import java.math.BigDecimal
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 휴가 신청 취소 UseCase
 */
@Service
@Transactional
class CancelLeaveRequestAppService(
    private val leaveRequestRepository: LeaveRequestRepository,
    private val employeeRepository: EmployeeRepository
) {
    fun execute(context: DomainContext, leaveRequestId: String): LeaveRequestResult {
        val requestId = LeaveRequestId.from(leaveRequestId)

        // 1. 휴가 신청 조회
        var leaveRequest = leaveRequestRepository.findById(requestId)
            ?: throw LeaveRequestNotFoundException(leaveRequestId)

        // 2. 상태 검증 (PENDING 또는 APPROVED만 취소 가능)
        if (leaveRequest.status != LeaveStatus.PENDING && leaveRequest.status != LeaveStatus.APPROVED) {
            throw LeaveRequestCannotBeCancelledException(
                leaveRequestId,
                leaveRequest.status.name
            )
        }

        val wasApproved = leaveRequest.status == LeaveStatus.APPROVED

        // 3. 휴가 취소 처리
        leaveRequest = leaveRequest.cancel(context)

        // 4. 승인된 휴가였다면 근로자의 연차 복구
        if (wasApproved) {
            var employee = employeeRepository.findById(leaveRequest.employeeId)
                ?: throw EmployeeNotFoundException(leaveRequest.employeeId.value)

            val requestedDays = BigDecimal(leaveRequest.calculateLeaveDays())
            employee = employee.restoreLeave(context, requestedDays)

            employeeRepository.save(employee)
        }

        // 5. 저장
        val savedLeaveRequest = leaveRequestRepository.save(leaveRequest)

        return LeaveRequestResult.from(savedLeaveRequest)
    }
}
