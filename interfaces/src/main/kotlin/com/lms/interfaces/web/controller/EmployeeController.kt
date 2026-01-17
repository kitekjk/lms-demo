package com.lms.interfaces.web.controller

import com.lms.application.employee.CreateEmployeeAppService
import com.lms.application.employee.DeactivateEmployeeAppService
import com.lms.application.employee.GetAllEmployeesAppService
import com.lms.application.employee.GetEmployeeAppService
import com.lms.application.employee.GetEmployeesByStoreAppService
import com.lms.application.employee.UpdateEmployeeAppService
import com.lms.application.employee.dto.CreateEmployeeCommand
import com.lms.application.employee.dto.UpdateEmployeeCommand
import com.lms.domain.common.DomainContext
import com.lms.interfaces.web.dto.EmployeeCreateRequest
import com.lms.interfaces.web.dto.EmployeeListResponse
import com.lms.interfaces.web.dto.EmployeeResponse
import com.lms.interfaces.web.dto.EmployeeUpdateRequest
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

/**
 * 근로자 관리 REST API 컨트롤러
 * SUPER_ADMIN과 MANAGER는 근로자 CRUD 작업 가능
 * MANAGER는 자신의 매장 근로자만 관리 가능
 */
@Tag(name = "근로자 관리", description = "근로자 생성, 조회, 수정, 비활성화 API")
@RestController
@RequestMapping("/api/employees")
class EmployeeController(
    private val createEmployeeAppService: CreateEmployeeAppService,
    private val getEmployeeAppService: GetEmployeeAppService,
    private val getEmployeesByStoreAppService: GetEmployeesByStoreAppService,
    private val getAllEmployeesAppService: GetAllEmployeesAppService,
    private val updateEmployeeAppService: UpdateEmployeeAppService,
    private val deactivateEmployeeAppService: DeactivateEmployeeAppService
) {

    /**
     * 근로자 생성
     * SUPER_ADMIN과 MANAGER만 가능
     */
    @Operation(
        summary = "근로자 생성",
        description = "새로운 근로자를 등록합니다. SUPER_ADMIN과 MANAGER 권한이 필요합니다.",
        security = [SecurityRequirement(name = "Bearer Authentication")]
    )
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "근로자 생성 성공"),
        ApiResponse(responseCode = "400", description = "잘못된 요청"),
        ApiResponse(responseCode = "401", description = "인증 실패"),
        ApiResponse(responseCode = "403", description = "권한 없음")
    )
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MANAGER')")
    fun createEmployee(
        context: DomainContext,
        @Valid @RequestBody request: EmployeeCreateRequest
    ): ResponseEntity<EmployeeResponse> {
        val command = CreateEmployeeCommand(
            userId = request.userId,
            name = request.name,
            employeeType = request.employeeType,
            storeId = request.storeId
        )

        val result = createEmployeeAppService.execute(context, command)
        val response = EmployeeResponse(
            id = result.id,
            userId = result.userId,
            name = result.name,
            employeeType = result.employeeType,
            storeId = result.storeId,
            remainingLeave = result.remainingLeave,
            isActive = result.isActive,
            createdAt = result.createdAt
        )

        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    /**
     * 근로자 목록 조회
     * storeId 쿼리 파라미터로 매장별 필터링 가능
     * activeOnly 쿼리 파라미터로 활성 근로자만 조회 가능
     */
    @Operation(
        summary = "근로자 목록 조회",
        description = "근로자 목록을 조회합니다. storeId로 매장별 필터링, activeOnly로 활성 근로자만 조회 가능합니다.",
        security = [SecurityRequirement(name = "Bearer Authentication")]
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "조회 성공"),
        ApiResponse(responseCode = "401", description = "인증 실패")
    )
    @GetMapping
    fun getEmployees(
        @RequestParam(required = false) storeId: String?,
        @RequestParam(required = false, defaultValue = "false") activeOnly: Boolean
    ): ResponseEntity<EmployeeListResponse> {
        val results = when {
            storeId != null -> getEmployeesByStoreAppService.execute(storeId, activeOnly)
            else -> getAllEmployeesAppService.execute()
        }

        val employees = results.map { result ->
            EmployeeResponse(
                id = result.id,
                userId = result.userId,
                name = result.name,
                employeeType = result.employeeType,
                storeId = result.storeId,
                remainingLeave = result.remainingLeave,
                isActive = result.isActive,
                createdAt = result.createdAt
            )
        }

        val response = EmployeeListResponse(
            employees = employees,
            totalCount = employees.size
        )

        return ResponseEntity.ok(response)
    }

    /**
     * 근로자 상세 조회
     * 인증된 사용자 모두 가능
     */
    @Operation(
        summary = "근로자 상세 조회",
        description = "특정 근로자의 상세 정보를 조회합니다.",
        security = [SecurityRequirement(name = "Bearer Authentication")]
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "조회 성공"),
        ApiResponse(responseCode = "401", description = "인증 실패"),
        ApiResponse(responseCode = "404", description = "근로자를 찾을 수 없음")
    )
    @GetMapping("/{employeeId}")
    fun getEmployee(@PathVariable employeeId: String): ResponseEntity<EmployeeResponse> {
        val result = getEmployeeAppService.execute(employeeId)
            ?: return ResponseEntity.notFound().build()

        val response = EmployeeResponse(
            id = result.id,
            userId = result.userId,
            name = result.name,
            employeeType = result.employeeType,
            storeId = result.storeId,
            remainingLeave = result.remainingLeave,
            isActive = result.isActive,
            createdAt = result.createdAt
        )

        return ResponseEntity.ok(response)
    }

    /**
     * 근로자 정보 수정
     * SUPER_ADMIN과 MANAGER만 가능
     */
    @Operation(
        summary = "근로자 정보 수정",
        description = "근로자 정보를 수정합니다. SUPER_ADMIN과 MANAGER 권한이 필요합니다.",
        security = [SecurityRequirement(name = "Bearer Authentication")]
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "수정 성공"),
        ApiResponse(responseCode = "400", description = "잘못된 요청"),
        ApiResponse(responseCode = "401", description = "인증 실패"),
        ApiResponse(responseCode = "403", description = "권한 없음"),
        ApiResponse(responseCode = "404", description = "근로자를 찾을 수 없음")
    )
    @PutMapping("/{employeeId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MANAGER')")
    fun updateEmployee(
        context: DomainContext,
        @PathVariable employeeId: String,
        @Valid @RequestBody request: EmployeeUpdateRequest
    ): ResponseEntity<EmployeeResponse> {
        val command = UpdateEmployeeCommand(
            name = request.name,
            employeeType = request.employeeType,
            storeId = request.storeId
        )

        val result = updateEmployeeAppService.execute(context, employeeId, command)
        val response = EmployeeResponse(
            id = result.id,
            userId = result.userId,
            name = result.name,
            employeeType = result.employeeType,
            storeId = result.storeId,
            remainingLeave = result.remainingLeave,
            isActive = result.isActive,
            createdAt = result.createdAt
        )

        return ResponseEntity.ok(response)
    }

    /**
     * 근로자 비활성화
     * SUPER_ADMIN과 MANAGER만 가능
     */
    @Operation(
        summary = "근로자 비활성화",
        description = "근로자를 비활성화합니다. SUPER_ADMIN과 MANAGER 권한이 필요합니다.",
        security = [SecurityRequirement(name = "Bearer Authentication")]
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "비활성화 성공"),
        ApiResponse(responseCode = "401", description = "인증 실패"),
        ApiResponse(responseCode = "403", description = "권한 없음"),
        ApiResponse(responseCode = "404", description = "근로자를 찾을 수 없음")
    )
    @PatchMapping("/{employeeId}/deactivate")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MANAGER')")
    fun deactivateEmployee(context: DomainContext, @PathVariable employeeId: String): ResponseEntity<EmployeeResponse> {
        val result = deactivateEmployeeAppService.execute(context, employeeId)
        val response = EmployeeResponse(
            id = result.id,
            userId = result.userId,
            name = result.name,
            employeeType = result.employeeType,
            storeId = result.storeId,
            remainingLeave = result.remainingLeave,
            isActive = result.isActive,
            createdAt = result.createdAt
        )

        return ResponseEntity.ok(response)
    }
}
