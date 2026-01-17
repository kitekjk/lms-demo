package com.lms.application.payroll

import com.lms.application.payroll.dto.PayrollDetailResult
import com.lms.application.payroll.dto.PayrollResult
import com.lms.application.payroll.dto.PayrollWithDetailsResult
import com.lms.domain.exception.PayrollNotFoundException
import com.lms.domain.model.payroll.PayrollDetailRepository
import com.lms.domain.model.payroll.PayrollId
import com.lms.domain.model.payroll.PayrollRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 급여 상세 조회 UseCase
 */
@Service
@Transactional(readOnly = true)
class GetPayrollAppService(
    private val payrollRepository: PayrollRepository,
    private val payrollDetailRepository: PayrollDetailRepository
) {
    fun execute(payrollId: String): PayrollWithDetailsResult {
        val id = PayrollId.from(payrollId)

        // 1. 급여 조회
        val payroll = payrollRepository.findById(id)
            ?: throw PayrollNotFoundException(payrollId)

        // 2. 급여 상세 조회
        val details = payrollDetailRepository.findByPayrollId(id)

        return PayrollWithDetailsResult(
            payroll = PayrollResult.from(payroll),
            details = details.map { PayrollDetailResult.from(it) }
        )
    }
}
