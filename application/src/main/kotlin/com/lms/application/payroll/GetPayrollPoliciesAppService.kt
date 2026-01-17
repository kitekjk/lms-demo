package com.lms.application.payroll

import com.lms.application.payroll.dto.PayrollPolicyResult
import com.lms.domain.model.payroll.PayrollPolicyRepository
import com.lms.domain.model.payroll.PolicyType
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 급여 정책 조회 UseCase
 */
@Service
@Transactional(readOnly = true)
class GetPayrollPoliciesAppService(private val payrollPolicyRepository: PayrollPolicyRepository) {
    /**
     * 현재 유효한 정책 조회
     */
    fun getCurrentlyEffectivePolicies(): List<PayrollPolicyResult> {
        val policies = payrollPolicyRepository.findCurrentlyEffectivePolicies()
        return policies.map { PayrollPolicyResult.from(it) }
    }

    /**
     * 특정 유형의 정책 조회
     */
    fun getPoliciesByType(policyType: PolicyType): List<PayrollPolicyResult> {
        val policies = payrollPolicyRepository.findByPolicyType(policyType)
        return policies.map { PayrollPolicyResult.from(it) }
    }
}
