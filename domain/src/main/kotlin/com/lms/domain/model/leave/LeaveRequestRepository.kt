package com.lms.domain.model.leave

import com.lms.domain.model.employee.EmployeeId
import java.time.LocalDate

/**
 * LeaveRequest Repository Interface
 * 구현체는 infrastructure 모듈에 위치
 */
interface LeaveRequestRepository {
    fun save(leaveRequest: LeaveRequest): LeaveRequest
    fun findById(id: LeaveRequestId): LeaveRequest?
    fun findByEmployeeId(employeeId: EmployeeId): List<LeaveRequest>
    fun findByEmployeeIdAndStatus(employeeId: EmployeeId, status: LeaveStatus): List<LeaveRequest>
    fun findByEmployeeIdAndDateRange(
        employeeId: EmployeeId,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<LeaveRequest>
    fun findPendingRequests(): List<LeaveRequest>
    fun delete(id: LeaveRequestId)
}
