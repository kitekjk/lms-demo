package com.lms.application.leave

import com.lms.application.leave.dto.CreateLeaveRequestCommand
import com.lms.application.leave.dto.LeaveRequestResult
import com.lms.domain.common.DomainContext
import com.lms.domain.exception.EmployeeNotFoundException
import com.lms.domain.exception.InsufficientLeaveBalanceException
import com.lms.domain.exception.InvalidLeaveDateRangeException
import com.lms.domain.exception.LeaveRequestDateOverlapException
import com.lms.domain.exception.PastDateLeaveRequestException
import com.lms.domain.model.employee.EmployeeId
import com.lms.domain.model.employee.EmployeeRepository
import com.lms.domain.model.leave.LeavePeriod
import com.lms.domain.model.leave.LeaveRequest
import com.lms.domain.model.leave.LeaveRequestRepository
import com.lms.domain.model.leave.LeaveStatus
import com.lms.domain.service.LeavePolicyService
import java.math.BigDecimal
import java.time.LocalDate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 휴가 신청 UseCase
 */
@Service
@Transactional
class CreateLeaveRequestAppService(
    private val leaveRequestRepository: LeaveRequestRepository,
    private val employeeRepository: EmployeeRepository,
    private val leavePolicyService: LeavePolicyService
) {
    fun execute(context: DomainContext, command: CreateLeaveRequestCommand): LeaveRequestResult {
        val employeeId = EmployeeId.from(command.employeeId)

        // 1. 근로자 조회
        val employee = employeeRepository.findById(employeeId)
            ?: throw EmployeeNotFoundException(command.employeeId)

        // 2. 날짜 검증
        validateDateRange(command.startDate, command.endDate)

        // 3. LeavePeriod 생성
        val leavePeriod = LeavePeriod(command.startDate, command.endDate)
        val requestedDays = BigDecimal(leavePeriod.calculateDays())

        // 4. 잔여 연차 검증 (정규직/비정규직만)
        val validationError = leavePolicyService.validateLeaveRequest(
            employee.employeeType,
            employee.remainingLeave,
            requestedDays
        )
        if (validationError != null) {
            throw InsufficientLeaveBalanceException(
                requestedDays.toDouble(),
                employee.remainingLeave.value.toDouble()
            )
        }

        // 5. 날짜 중복 검증 (승인된 휴가와 겹치는지 확인)
        val existingLeaves = leaveRequestRepository.findByEmployeeIdAndDateRange(
            employeeId,
            command.startDate,
            command.endDate
        )

        existingLeaves
            .filter { it.status == LeaveStatus.APPROVED }
            .forEach { existingLeave ->
                if (existingLeave.leavePeriod.overlapsWith(leavePeriod)) {
                    throw LeaveRequestDateOverlapException(
                        command.employeeId,
                        command.startDate.toString(),
                        command.endDate.toString()
                    )
                }
            }

        // 6. 휴가 신청 생성
        val leaveRequest = LeaveRequest.create(
            context = context,
            employeeId = employeeId,
            leaveType = command.leaveType,
            leavePeriod = leavePeriod,
            reason = command.reason
        )

        // 7. 저장
        val savedLeaveRequest = leaveRequestRepository.save(leaveRequest)

        return LeaveRequestResult.from(savedLeaveRequest)
    }

    private fun validateDateRange(startDate: LocalDate, endDate: LocalDate) {
        val today = LocalDate.now()

        // 과거 날짜 체크
        if (startDate.isBefore(today)) {
            throw PastDateLeaveRequestException(startDate.toString())
        }

        // 시작일 > 종료일 체크
        if (startDate.isAfter(endDate)) {
            throw InvalidLeaveDateRangeException(startDate.toString(), endDate.toString())
        }
    }
}
