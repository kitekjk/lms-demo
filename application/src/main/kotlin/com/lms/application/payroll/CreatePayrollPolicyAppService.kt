package com.lms.application.payroll

import com.lms.application.payroll.dto.CreatePayrollPolicyCommand
import com.lms.application.payroll.dto.PayrollPolicyResult
import com.lms.domain.common.DomainContext
import com.lms.domain.exception.PayrollPolicyPeriodOverlapException
import com.lms.domain.model.payroll.PayrollPolicy
import com.lms.domain.model.payroll.PayrollPolicyRepository
import com.lms.domain.model.payroll.PolicyEffectivePeriod
import com.lms.domain.model.payroll.PolicyMultiplier
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 급여 정책 생성 UseCase
 */
@Service
@Transactional
class CreatePayrollPolicyAppService(private val payrollPolicyRepository: PayrollPolicyRepository) {
    fun execute(context: DomainContext, command: CreatePayrollPolicyCommand): PayrollPolicyResult {
        // 1. 정책 기간 중복 검증
        val effectivePolicies = if (command.effectiveTo != null) {
            payrollPolicyRepository.findEffectivePolicies(command.effectiveFrom)
                .filter { it.policyType == command.policyType }
        } else {
            payrollPolicyRepository.findByPolicyType(command.policyType)
                .filter { it.isCurrentlyEffective() }
        }

        effectivePolicies.forEach { existingPolicy ->
            val existingPeriod = existingPolicy.effectivePeriod
            val existingEndDate = existingPeriod.effectiveTo
            val newEndDate = command.effectiveTo

            // 기간 겹침 확인
            val isOverlapping = if (newEndDate != null) {
                // 신규 정책에 종료일이 있는 경우
                existingPeriod.isEffectiveOn(command.effectiveFrom) ||
                    existingPeriod.isEffectiveOn(newEndDate) ||
                    (
                        existingPeriod.effectiveFrom.isAfter(command.effectiveFrom) &&
                            (existingEndDate?.isBefore(newEndDate) ?: false)
                        )
            } else {
                // 신규 정책이 무기한인 경우
                existingEndDate == null || !existingEndDate.isBefore(command.effectiveFrom)
            }

            if (isOverlapping) {
                throw PayrollPolicyPeriodOverlapException(
                    command.policyType.name,
                    "${existingPeriod.effectiveFrom} ~ ${existingPeriod.effectiveTo ?: "무기한"}",
                    "${command.effectiveFrom} ~ ${command.effectiveTo ?: "무기한"}"
                )
            }
        }

        // 2. 정책 생성
        val policy = PayrollPolicy.create(
            context = context,
            policyType = command.policyType,
            multiplier = PolicyMultiplier.from(command.multiplier),
            effectivePeriod = PolicyEffectivePeriod(command.effectiveFrom, command.effectiveTo),
            description = command.description
        )

        // 3. 저장
        val savedPolicy = payrollPolicyRepository.save(policy)

        return PayrollPolicyResult.from(savedPolicy)
    }
}
