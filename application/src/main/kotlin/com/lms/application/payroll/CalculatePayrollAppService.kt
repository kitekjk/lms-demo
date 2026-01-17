package com.lms.application.payroll

import com.lms.application.payroll.dto.CalculatePayrollCommand
import com.lms.application.payroll.dto.PayrollResult
import com.lms.domain.common.DomainContext
import com.lms.domain.exception.EmployeeNotFoundException
import com.lms.domain.exception.NoAttendanceRecordsFoundException
import com.lms.domain.exception.PayrollAlreadyCalculatedException
import com.lms.domain.model.attendance.AttendanceRecordRepository
import com.lms.domain.model.employee.EmployeeId
import com.lms.domain.model.employee.EmployeeRepository
import com.lms.domain.model.leave.LeaveRequestRepository
import com.lms.domain.model.leave.LeaveStatus
import com.lms.domain.model.payroll.*
import com.lms.domain.service.PayrollCalculationEngine
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 급여 계산 UseCase
 */
@Service
@Transactional
class CalculatePayrollAppService(
    private val payrollRepository: PayrollRepository,
    private val payrollDetailRepository: PayrollDetailRepository,
    private val employeeRepository: EmployeeRepository,
    private val attendanceRecordRepository: AttendanceRecordRepository,
    private val leaveRequestRepository: LeaveRequestRepository,
    private val payrollPolicyRepository: PayrollPolicyRepository,
    private val calculationEngine: PayrollCalculationEngine
) {
    fun execute(context: DomainContext, command: CalculatePayrollCommand): PayrollResult {
        val employeeId = EmployeeId.from(command.employeeId)
        val period = PayrollPeriod.from(command.period)

        // 1. 근로자 존재 확인
        val employee = employeeRepository.findById(employeeId)
            ?: throw EmployeeNotFoundException(command.employeeId)

        // 2. 중복 계산 방지
        payrollRepository.findByEmployeeIdAndPeriod(employeeId, period)?.let {
            throw PayrollAlreadyCalculatedException(
                command.employeeId,
                "${command.period.year}-${command.period.monthValue}"
            )
        }

        // 3. 출퇴근 기록 조회
        val startDate = command.period.atDay(1)
        val endDate = command.period.atEndOfMonth()
        val attendanceRecords = attendanceRecordRepository.findByEmployeeIdAndDateRange(
            employeeId,
            startDate,
            endDate
        )

        if (attendanceRecords.isEmpty()) {
            throw NoAttendanceRecordsFoundException(
                command.employeeId,
                "${command.period.year}-${command.period.monthValue}"
            )
        }

        // 4. 승인된 휴가 조회
        val approvedLeaves = leaveRequestRepository.findByEmployeeIdAndStatus(employeeId, LeaveStatus.APPROVED)
            .filter { leave ->
                // 해당 기간에 속하는 휴가만 필터링
                val leaveStart = leave.leavePeriod.startDate
                val leaveEnd = leave.leavePeriod.endDate
                !(leaveEnd.isBefore(startDate) || leaveStart.isAfter(endDate))
            }

        // 5. 유효한 급여 정책 조회
        val policies = payrollPolicyRepository.findEffectivePolicies(startDate)

        // 6. 급여 계산
        val calculationResult = calculationEngine.calculate(
            context = context,
            attendanceRecords = attendanceRecords,
            approvedLeaves = approvedLeaves,
            hourlyRate = command.hourlyRate,
            policies = policies
        )

        // 7. Payroll 생성 및 저장
        val payroll = Payroll.create(
            context = context,
            employeeId = employeeId,
            period = period,
            amount = PayrollAmount(
                baseAmount = calculationResult.baseAmount,
                overtimeAmount = calculationResult.overtimeAmount
            )
        )
        val savedPayroll = payrollRepository.save(payroll)

        // 8. PayrollDetail 생성 및 저장
        val payrollDetails = calculationResult.details.map { detailData ->
            PayrollDetail.create(
                payrollId = savedPayroll.id,
                workDate = detailData.workDate,
                workType = detailData.workType,
                hours = detailData.hours,
                hourlyRate = detailData.hourlyRate,
                multiplier = detailData.multiplier
            )
        }
        payrollDetailRepository.saveAll(payrollDetails)

        return PayrollResult.from(savedPayroll)
    }
}
