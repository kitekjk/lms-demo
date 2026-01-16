package com.lms.interfaces.web.controller

import com.lms.application.employee.CreateEmployeeAppService
import com.lms.application.employee.DeactivateEmployeeAppService
import com.lms.application.employee.GetAllEmployeesAppService
import com.lms.application.employee.GetEmployeeAppService
import com.lms.application.employee.GetEmployeesByStoreAppService
import com.lms.application.employee.UpdateEmployeeAppService
import com.lms.application.employee.dto.CreateEmployeeCommand
import com.lms.application.employee.dto.UpdateEmployeeCommand
import com.lms.domain.common.DomainContextBase
import com.lms.infrastructure.security.SecurityUtils
import com.lms.interfaces.web.dto.EmployeeCreateRequest
import com.lms.interfaces.web.dto.EmployeeListResponse
import com.lms.interfaces.web.dto.EmployeeResponse
import com.lms.interfaces.web.dto.EmployeeUpdateRequest
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
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MANAGER')")
    fun createEmployee(@Valid @RequestBody request: EmployeeCreateRequest): ResponseEntity<EmployeeResponse> {
        val context = DomainContextBase(
            serviceName = "EmployeeController",
            userId = SecurityUtils.getCurrentUserId() ?: "",
            userName = SecurityUtils.getCurrentUserId() ?: "",
            roleId = SecurityUtils.getCurrentUserRole() ?: "",
            requestId = java.util.UUID.randomUUID(),
            requestedAt = java.time.Instant.now(),
            clientIp = ""
        )

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
    @PutMapping("/{employeeId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MANAGER')")
    fun updateEmployee(
        @PathVariable employeeId: String,
        @Valid @RequestBody request: EmployeeUpdateRequest
    ): ResponseEntity<EmployeeResponse> {
        val context = DomainContextBase(
            serviceName = "EmployeeController",
            userId = SecurityUtils.getCurrentUserId() ?: "",
            userName = SecurityUtils.getCurrentUserId() ?: "",
            roleId = SecurityUtils.getCurrentUserRole() ?: "",
            requestId = java.util.UUID.randomUUID(),
            requestedAt = java.time.Instant.now(),
            clientIp = ""
        )

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
    @PatchMapping("/{employeeId}/deactivate")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MANAGER')")
    fun deactivateEmployee(@PathVariable employeeId: String): ResponseEntity<EmployeeResponse> {
        val context = DomainContextBase(
            serviceName = "EmployeeController",
            userId = SecurityUtils.getCurrentUserId() ?: "",
            userName = SecurityUtils.getCurrentUserId() ?: "",
            roleId = SecurityUtils.getCurrentUserRole() ?: "",
            requestId = java.util.UUID.randomUUID(),
            requestedAt = java.time.Instant.now(),
            clientIp = ""
        )

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
