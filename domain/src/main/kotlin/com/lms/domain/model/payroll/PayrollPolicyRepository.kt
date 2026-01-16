package com.lms.domain.model.payroll

import java.time.LocalDate

/**
 * PayrollPolicy Repository Interface
 * 구현체는 infrastructure 모듈에 위치
 */
interface PayrollPolicyRepository {
    fun save(payrollPolicy: PayrollPolicy): PayrollPolicy
    fun findById(id: PayrollPolicyId): PayrollPolicy?
    fun findByPolicyType(policyType: PolicyType): List<PayrollPolicy>
    fun findEffectivePolicies(date: LocalDate): List<PayrollPolicy>
    fun findCurrentlyEffectivePolicies(): List<PayrollPolicy>
    fun delete(id: PayrollPolicyId)
}
