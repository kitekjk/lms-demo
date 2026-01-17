package com.lms.application.leave

import com.lms.application.leave.dto.LeaveRequestResult
import com.lms.domain.model.employee.EmployeeId
import com.lms.domain.model.leave.LeaveRequestRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 본인 휴가 신청 내역 조회 UseCase
 */
@Service
@Transactional(readOnly = true)
class GetMyLeaveRequestsAppService(private val leaveRequestRepository: LeaveRequestRepository) {
    fun execute(employeeId: String): List<LeaveRequestResult> {
        val empId = EmployeeId.from(employeeId)
        val leaveRequests = leaveRequestRepository.findByEmployeeId(empId)
        return leaveRequests.map { LeaveRequestResult.from(it) }
    }
}
