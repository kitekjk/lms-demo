package com.lms.application.leave

import com.lms.application.leave.dto.LeaveRequestResult
import com.lms.domain.common.DomainContext
import com.lms.domain.exception.EmployeeNotFoundException
import com.lms.domain.exception.LeaveRequestCannotBeProcessedException
import com.lms.domain.exception.LeaveRequestNotFoundException
import com.lms.domain.model.employee.EmployeeRepository
import com.lms.domain.model.leave.LeaveRequestId
import com.lms.domain.model.leave.LeaveRequestRepository
import com.lms.domain.model.leave.LeaveStatus
import com.lms.domain.model.user.UserId
import java.math.BigDecimal
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 휴가 승인 UseCase
 */
@Service
@Transactional
class ApproveLeaveRequestAppService(
    private val leaveRequestRepository: LeaveRequestRepository,
    private val employeeRepository: EmployeeRepository
) {
    fun execute(context: DomainContext, leaveRequestId: String): LeaveRequestResult {
        val requestId = LeaveRequestId.from(leaveRequestId)
        val approverId = UserId.from(context.userId)

        // 1. 휴가 신청 조회
        var leaveRequest = leaveRequestRepository.findById(requestId)
            ?: throw LeaveRequestNotFoundException(leaveRequestId)

        // 2. 상태 검증 (PENDING만 승인 가능)
        if (leaveRequest.status != LeaveStatus.PENDING) {
            throw LeaveRequestCannotBeProcessedException(
                leaveRequestId,
                leaveRequest.status.name
            )
        }

        // 3. 근로자 조회
        var employee = employeeRepository.findById(leaveRequest.employeeId)
            ?: throw EmployeeNotFoundException(leaveRequest.employeeId.value)

        // 4. 휴가 승인 처리
        leaveRequest = leaveRequest.approve(context, approverId)

        // 5. 근로자의 잔여 연차 차감
        val requestedDays = BigDecimal(leaveRequest.calculateLeaveDays())
        employee = employee.deductLeave(context, requestedDays)

        // 6. 저장
        val savedLeaveRequest = leaveRequestRepository.save(leaveRequest)
        employeeRepository.save(employee)

        return LeaveRequestResult.from(savedLeaveRequest)
    }
}
