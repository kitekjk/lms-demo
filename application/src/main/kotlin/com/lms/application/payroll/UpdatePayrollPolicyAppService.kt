package com.lms.application.payroll

import com.lms.application.payroll.dto.PayrollPolicyResult
import com.lms.application.payroll.dto.UpdatePayrollPolicyCommand
import com.lms.domain.common.DomainContext
import com.lms.domain.exception.InactivePolicyCannotBeModifiedException
import com.lms.domain.exception.PayrollPolicyNotFoundException
import com.lms.domain.model.payroll.PayrollPolicyId
import com.lms.domain.model.payroll.PayrollPolicyRepository
import com.lms.domain.model.payroll.PolicyMultiplier
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 급여 정책 수정 UseCase
 */
@Service
@Transactional
class UpdatePayrollPolicyAppService(private val payrollPolicyRepository: PayrollPolicyRepository) {
    fun execute(context: DomainContext, policyId: String, command: UpdatePayrollPolicyCommand): PayrollPolicyResult {
        val id = PayrollPolicyId.from(policyId)

        // 1. 정책 조회
        var policy = payrollPolicyRepository.findById(id)
            ?: throw PayrollPolicyNotFoundException(policyId)

        // 2. 유효한 정책인지 확인
        if (!policy.isCurrentlyEffective()) {
            throw InactivePolicyCannotBeModifiedException(policyId)
        }

        // 3. 배율 변경 (제공된 경우)
        if (command.multiplier != null) {
            policy = policy.updateMultiplier(context, PolicyMultiplier.from(command.multiplier))
        }

        // 4. 종료일 설정 (제공된 경우)
        if (command.effectiveTo != null) {
            policy = policy.terminate(context, command.effectiveTo)
        }

        // 5. 설명 변경 (제공된 경우)
        if (command.description != null) {
            policy = policy.copy(description = command.description)
        }

        // 6. 저장
        val savedPolicy = payrollPolicyRepository.save(policy)

        return PayrollPolicyResult.from(savedPolicy)
    }
}
