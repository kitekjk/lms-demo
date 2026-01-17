package com.lms.application.payroll

import com.lms.application.payroll.dto.PayrollResult
import com.lms.domain.model.payroll.PayrollPeriod
import com.lms.domain.model.payroll.PayrollRepository
import java.time.YearMonth
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 기간별 급여 내역 조회 UseCase
 */
@Service
@Transactional(readOnly = true)
class GetPayrollsByPeriodAppService(private val payrollRepository: PayrollRepository) {
    fun execute(period: YearMonth): List<PayrollResult> {
        val payrollPeriod = PayrollPeriod.from(period)

        val payrolls = payrollRepository.findByPeriod(payrollPeriod)

        return payrolls.map { PayrollResult.from(it) }
    }
}
