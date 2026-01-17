package com.lms.interfaces.web.controller

import com.lms.application.payroll.*
import com.lms.domain.common.DomainContext
import com.lms.interfaces.web.dto.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
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
@Tag(name = "급여 조회", description = "급여 계산, 조회 및 배치 실행 API")
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
    @Operation(
        summary = "급여 계산 실행",
        description = "특정 근로자의 급여를 계산합니다. SUPER_ADMIN과 MANAGER 권한이 필요합니다.",
        security = [SecurityRequirement(name = "Bearer Authentication")]
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "계산 성공"),
        ApiResponse(responseCode = "400", description = "잘못된 요청"),
        ApiResponse(responseCode = "401", description = "인증 실패"),
        ApiResponse(responseCode = "403", description = "권한 없음")
    )
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
    @Operation(
        summary = "급여 상세 조회",
        description = "특정 급여 내역의 상세 정보를 조회합니다.",
        security = [SecurityRequirement(name = "Bearer Authentication")]
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "조회 성공"),
        ApiResponse(responseCode = "401", description = "인증 실패"),
        ApiResponse(responseCode = "404", description = "급여 내역을 찾을 수 없음")
    )
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
    @Operation(
        summary = "본인 급여 내역 조회",
        description = "로그인한 사용자의 모든 급여 내역을 조회합니다.",
        security = [SecurityRequirement(name = "Bearer Authentication")]
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "조회 성공"),
        ApiResponse(responseCode = "401", description = "인증 실패")
    )
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
    @Operation(
        summary = "기간별 급여 내역 조회",
        description = "특정 기간의 모든 급여 내역을 조회합니다. MANAGER와 SUPER_ADMIN 권한이 필요합니다.",
        security = [SecurityRequirement(name = "Bearer Authentication")]
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "조회 성공"),
        ApiResponse(responseCode = "401", description = "인증 실패"),
        ApiResponse(responseCode = "403", description = "권한 없음")
    )
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
    @Operation(
        summary = "급여 배치 실행",
        description = "급여 계산 배치를 수동으로 실행합니다. SUPER_ADMIN 권한이 필요합니다.",
        security = [SecurityRequirement(name = "Bearer Authentication")]
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "배치 실행 성공"),
        ApiResponse(responseCode = "400", description = "잘못된 요청"),
        ApiResponse(responseCode = "401", description = "인증 실패"),
        ApiResponse(responseCode = "403", description = "권한 없음")
    )
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
    @Operation(
        summary = "급여 배치 실행 이력 조회",
        description = "급여 계산 배치의 실행 이력을 조회합니다. MANAGER와 SUPER_ADMIN 권한이 필요합니다.",
        security = [SecurityRequirement(name = "Bearer Authentication")]
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "조회 성공"),
        ApiResponse(responseCode = "401", description = "인증 실패"),
        ApiResponse(responseCode = "403", description = "권한 없음")
    )
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
