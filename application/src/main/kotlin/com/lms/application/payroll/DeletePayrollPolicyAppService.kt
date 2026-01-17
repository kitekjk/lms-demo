package com.lms.application.payroll

import com.lms.domain.exception.PayrollPolicyNotFoundException
import com.lms.domain.model.payroll.PayrollPolicyId
import com.lms.domain.model.payroll.PayrollPolicyRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 급여 정책 삭제 UseCase
 */
@Service
@Transactional
class DeletePayrollPolicyAppService(private val payrollPolicyRepository: PayrollPolicyRepository) {
    fun execute(policyId: String) {
        val id = PayrollPolicyId.from(policyId)

        // 1. 정책 존재 확인
        payrollPolicyRepository.findById(id)
            ?: throw PayrollPolicyNotFoundException(policyId)

        // 2. 삭제 (실제로는 infrastructure에서 soft delete 처리)
        payrollPolicyRepository.delete(id)
    }
}
