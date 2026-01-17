package com.lms.interfaces.web.controller

import com.lms.application.payroll.CreatePayrollPolicyAppService
import com.lms.application.payroll.DeletePayrollPolicyAppService
import com.lms.application.payroll.GetPayrollPoliciesAppService
import com.lms.application.payroll.UpdatePayrollPolicyAppService
import com.lms.application.payroll.dto.CreatePayrollPolicyCommand
import com.lms.application.payroll.dto.UpdatePayrollPolicyCommand
import com.lms.domain.common.DomainContext
import com.lms.domain.model.payroll.PolicyType
import com.lms.interfaces.web.dto.PayrollPolicyCreateRequest
import com.lms.interfaces.web.dto.PayrollPolicyListResponse
import com.lms.interfaces.web.dto.PayrollPolicyResponse
import com.lms.interfaces.web.dto.PayrollPolicyUpdateRequest
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

/**
 * 급여 정책 관리 REST API 컨트롤러
 * SUPER_ADMIN만 정책 생성/수정/삭제 가능
 */
@RestController
@RequestMapping("/api/payroll-policies")
class PayrollPolicyController(
    private val createPayrollPolicyAppService: CreatePayrollPolicyAppService,
    private val getPayrollPoliciesAppService: GetPayrollPoliciesAppService,
    private val updatePayrollPolicyAppService: UpdatePayrollPolicyAppService,
    private val deletePayrollPolicyAppService: DeletePayrollPolicyAppService
) {

    /**
     * 급여 정책 생성
     * SUPER_ADMIN 권한 필요
     */
    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    fun createPolicy(
        context: DomainContext,
        @Valid @RequestBody request: PayrollPolicyCreateRequest
    ): ResponseEntity<PayrollPolicyResponse> {
        val command = CreatePayrollPolicyCommand(
            policyType = request.policyType,
            multiplier = request.multiplier,
            effectiveFrom = request.effectiveFrom,
            effectiveTo = request.effectiveTo,
            description = request.description
        )

        val result = createPayrollPolicyAppService.execute(context, command)
        val response = PayrollPolicyResponse.from(result)

        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    /**
     * 현재 유효한 정책 조회
     * 모든 인증된 사용자 접근 가능
     */
    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER', 'SUPER_ADMIN')")
    fun getActivePolicies(): ResponseEntity<PayrollPolicyListResponse> {
        val results = getPayrollPoliciesAppService.getCurrentlyEffectivePolicies()
        val policies = results.map { PayrollPolicyResponse.from(it) }
        val response = PayrollPolicyListResponse(
            policies = policies,
            totalCount = policies.size
        )

        return ResponseEntity.ok(response)
    }

    /**
     * 정책 유형별 조회
     * MANAGER와 SUPER_ADMIN만 가능
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'SUPER_ADMIN')")
    fun getPoliciesByType(
        @RequestParam(required = false) policyType: PolicyType?
    ): ResponseEntity<PayrollPolicyListResponse> {
        val results = if (policyType != null) {
            getPayrollPoliciesAppService.getPoliciesByType(policyType)
        } else {
            getPayrollPoliciesAppService.getCurrentlyEffectivePolicies()
        }

        val policies = results.map { PayrollPolicyResponse.from(it) }
        val response = PayrollPolicyListResponse(
            policies = policies,
            totalCount = policies.size
        )

        return ResponseEntity.ok(response)
    }

    /**
     * 급여 정책 수정
     * SUPER_ADMIN 권한 필요
     */
    @PutMapping("/{policyId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    fun updatePolicy(
        context: DomainContext,
        @PathVariable policyId: String,
        @Valid @RequestBody request: PayrollPolicyUpdateRequest
    ): ResponseEntity<PayrollPolicyResponse> {
        val command = UpdatePayrollPolicyCommand(
            multiplier = request.multiplier,
            effectiveTo = request.effectiveTo,
            description = request.description
        )

        val result = updatePayrollPolicyAppService.execute(context, policyId, command)
        val response = PayrollPolicyResponse.from(result)

        return ResponseEntity.ok(response)
    }

    /**
     * 급여 정책 삭제
     * SUPER_ADMIN 권한 필요
     */
    @DeleteMapping("/{policyId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    fun deletePolicy(@PathVariable policyId: String): ResponseEntity<Void> {
        deletePayrollPolicyAppService.execute(policyId)
        return ResponseEntity.noContent().build()
    }
}
