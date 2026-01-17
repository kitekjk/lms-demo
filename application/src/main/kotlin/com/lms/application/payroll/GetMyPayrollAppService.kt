package com.lms.application.payroll

import com.lms.application.payroll.dto.PayrollResult
import com.lms.domain.model.employee.EmployeeId
import com.lms.domain.model.payroll.PayrollRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 본인 급여 내역 조회 UseCase
 */
@Service
@Transactional(readOnly = true)
class GetMyPayrollAppService(private val payrollRepository: PayrollRepository) {
    fun execute(employeeId: String): List<PayrollResult> {
        val empId = EmployeeId.from(employeeId)

        val payrolls = payrollRepository.findByEmployeeId(empId)

        return payrolls.map { PayrollResult.from(it) }
    }
}
