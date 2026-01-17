package com.lms.interfaces.web.controller

import com.lms.application.payroll.*
import com.lms.domain.common.DomainContext
import com.lms.interfaces.web.dto.*
import jakarta.validation.Valid
import java.time.Instant
import java.time.YearMonth
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

/**
 * 급여 관리 Controller
 */
@RestController
@RequestMapping("/api/payroll")
class PayrollController(
    private val calculatePayrollAppService: CalculatePayrollAppService,
    private val getPayrollAppService: GetPayrollAppService,
    private val getMyPayrollAppService: GetMyPayrollAppService,
    private val getPayrollsByPeriodAppService: GetPayrollsByPeriodAppService,
    private val executePayrollBatchAppService: ExecutePayrollBatchAppService,
    private val getPayrollBatchHistoriesAppService: GetPayrollBatchHistoriesAppService
) {
    /**
     * 급여 계산 실행
     * SUPER_ADMIN/MANAGER만 실행 가능
     */
    @PostMapping("/calculate")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MANAGER')")
    fun calculatePayroll(
        context: DomainContext,
        @Valid @RequestBody request: PayrollCalculateRequest
    ): ResponseEntity<PayrollResponse> {
        val command = request.toCommand()
        val result = calculatePayrollAppService.execute(context, command)
        return ResponseEntity.ok(PayrollResponse.from(result))
    }

    /**
     * 급여 상세 조회
     */
    @GetMapping("/{payrollId}")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER', 'SUPER_ADMIN')")
    fun getPayroll(@PathVariable payrollId: String): ResponseEntity<PayrollWithDetailsResponse> {
        val result = getPayrollAppService.execute(payrollId)
        return ResponseEntity.ok(PayrollWithDetailsResponse.from(result))
    }

    /**
     * 본인 급여 내역 조회
     * 모든 인증된 사용자 접근 가능
     */
    @GetMapping("/my-payroll")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER', 'SUPER_ADMIN')")
    fun getMyPayroll(context: DomainContext): ResponseEntity<List<PayrollResponse>> {
        // DomainContext에서 employeeId 추출 필요
        // 현재는 userId를 employeeId로 사용한다고 가정
        val results = getMyPayrollAppService.execute(context.userId)
        return ResponseEntity.ok(results.map { PayrollResponse.from(it) })
    }

    /**
     * 기간별 급여 내역 조회
     * MANAGER/SUPER_ADMIN만 접근 가능
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'SUPER_ADMIN')")
    fun getPayrollsByPeriod(@RequestParam(required = true) period: YearMonth): ResponseEntity<List<PayrollResponse>> {
        val results = getPayrollsByPeriodAppService.execute(period)
        return ResponseEntity.ok(results.map { PayrollResponse.from(it) })
    }

    /**
     * 수동 배치 실행
     * SUPER_ADMIN만 실행 가능
     */
    @PostMapping("/batch")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    fun executePayrollBatch(
        context: DomainContext,
        @Valid @RequestBody request: PayrollBatchExecuteRequest
    ): ResponseEntity<PayrollBatchHistoryResponse> {
        val command = request.toCommand()
        val result = executePayrollBatchAppService.execute(context, command)
        return ResponseEntity.ok(PayrollBatchHistoryResponse.from(result))
    }

    /**
     * 배치 실행 이력 조회
     * MANAGER/SUPER_ADMIN만 접근 가능
     */
    @GetMapping("/batch-history")
    @PreAuthorize("hasAnyRole('MANAGER', 'SUPER_ADMIN')")
    fun getPayrollBatchHistories(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) startDate: Instant?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) endDate: Instant?
    ): ResponseEntity<List<PayrollBatchHistoryResponse>> {
        val results = getPayrollBatchHistoriesAppService.execute(startDate, endDate)
        return ResponseEntity.ok(results.map { PayrollBatchHistoryResponse.from(it) })
    }
}
