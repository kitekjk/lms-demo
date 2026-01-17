package com.lms.interfaces.web.controller

import com.lms.application.schedule.CreateWorkScheduleAppService
import com.lms.application.schedule.DeleteWorkScheduleAppService
import com.lms.application.schedule.GetWorkScheduleAppService
import com.lms.application.schedule.GetWorkSchedulesByDateRangeAppService
import com.lms.application.schedule.GetWorkSchedulesByEmployeeAppService
import com.lms.application.schedule.GetWorkSchedulesByStoreAppService
import com.lms.application.schedule.UpdateWorkScheduleAppService
import com.lms.application.schedule.dto.CreateWorkScheduleCommand
import com.lms.application.schedule.dto.UpdateWorkScheduleCommand
import com.lms.domain.common.DomainContext
import com.lms.infrastructure.security.SecurityUtils
import com.lms.interfaces.web.dto.WorkScheduleCreateRequest
import com.lms.interfaces.web.dto.WorkScheduleListResponse
import com.lms.interfaces.web.dto.WorkScheduleResponse
import com.lms.interfaces.web.dto.WorkScheduleUpdateRequest
import jakarta.validation.Valid
import java.time.LocalDate
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

/**
 * 근무 일정 관리 REST API 컨트롤러
 * 관리자의 일정 생성/수정/삭제 및 조회 기능 제공
 */
@RestController
@RequestMapping("/api/schedules")
class WorkScheduleController(
    private val createWorkScheduleAppService: CreateWorkScheduleAppService,
    private val getWorkScheduleAppService: GetWorkScheduleAppService,
    private val getWorkSchedulesByEmployeeAppService: GetWorkSchedulesByEmployeeAppService,
    private val getWorkSchedulesByStoreAppService: GetWorkSchedulesByStoreAppService,
    private val getWorkSchedulesByDateRangeAppService: GetWorkSchedulesByDateRangeAppService,
    private val updateWorkScheduleAppService: UpdateWorkScheduleAppService,
    private val deleteWorkScheduleAppService: DeleteWorkScheduleAppService
) {

    /**
     * 근무 일정 생성
     * SUPER_ADMIN과 MANAGER만 가능
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'SUPER_ADMIN')")
    fun createSchedule(
        context: DomainContext,
        @Valid @RequestBody request: WorkScheduleCreateRequest
    ): ResponseEntity<WorkScheduleResponse> {
        val command = CreateWorkScheduleCommand(
            employeeId = request.employeeId,
            storeId = request.storeId,
            workDate = request.workDate,
            startTime = request.startTime,
            endTime = request.endTime
        )

        val result = createWorkScheduleAppService.execute(context, command)
        val response = WorkScheduleResponse.from(result)

        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    /**
     * 근무 일정 조회 (필터링 지원)
     * employeeId, storeId, startDate, endDate 쿼리 파라미터로 필터링 가능
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER', 'SUPER_ADMIN')")
    fun getSchedules(
        @RequestParam(required = false) employeeId: String?,
        @RequestParam(required = false) storeId: String?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate?
    ): ResponseEntity<WorkScheduleListResponse> {
        val results = when {
            // 매장 + 날짜 범위 조회
            storeId != null && startDate != null && endDate != null -> {
                getWorkSchedulesByDateRangeAppService.execute(storeId, startDate, endDate)
            }
            // 근로자별 조회
            employeeId != null -> {
                getWorkSchedulesByEmployeeAppService.execute(employeeId)
            }
            // 매장별 조회
            storeId != null -> {
                getWorkSchedulesByStoreAppService.execute(storeId)
            }
            else -> {
                // 필터 없이 조회는 지원하지 않음
                throw IllegalArgumentException("employeeId 또는 storeId 파라미터가 필요합니다")
            }
        }

        val schedules = results.map { WorkScheduleResponse.from(it) }
        val response = WorkScheduleListResponse(
            schedules = schedules,
            totalCount = schedules.size
        )

        return ResponseEntity.ok(response)
    }

    /**
     * 본인 근무 일정 조회
     * 현재 로그인한 근로자의 일정만 조회
     */
    @GetMapping("/my-schedule")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER', 'SUPER_ADMIN')")
    fun getMySchedule(): ResponseEntity<WorkScheduleListResponse> {
        val userId = SecurityUtils.getCurrentUserId()
            ?: throw IllegalStateException("인증된 사용자 정보를 찾을 수 없습니다")

        // TODO: userId로 employeeId 조회 (현재는 간단히 userId 사용)
        val results = getWorkSchedulesByEmployeeAppService.execute(userId)
        val schedules = results.map { WorkScheduleResponse.from(it) }
        val response = WorkScheduleListResponse(
            schedules = schedules,
            totalCount = schedules.size
        )

        return ResponseEntity.ok(response)
    }

    /**
     * 근무 일정 단건 조회
     */
    @GetMapping("/{scheduleId}")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER', 'SUPER_ADMIN')")
    fun getSchedule(@PathVariable scheduleId: String): ResponseEntity<WorkScheduleResponse> {
        val result = getWorkScheduleAppService.execute(scheduleId)
            ?: return ResponseEntity.notFound().build()

        val response = WorkScheduleResponse.from(result)
        return ResponseEntity.ok(response)
    }

    /**
     * 근무 일정 수정
     * SUPER_ADMIN과 MANAGER만 가능
     */
    @PutMapping("/{scheduleId}")
    @PreAuthorize("hasAnyRole('MANAGER', 'SUPER_ADMIN')")
    fun updateSchedule(
        context: DomainContext,
        @PathVariable scheduleId: String,
        @Valid @RequestBody request: WorkScheduleUpdateRequest
    ): ResponseEntity<WorkScheduleResponse> {
        val command = UpdateWorkScheduleCommand(
            workDate = request.workDate,
            startTime = request.startTime,
            endTime = request.endTime
        )

        val result = updateWorkScheduleAppService.execute(context, scheduleId, command)
        val response = WorkScheduleResponse.from(result)

        return ResponseEntity.ok(response)
    }

    /**
     * 근무 일정 삭제
     * SUPER_ADMIN과 MANAGER만 가능
     */
    @DeleteMapping("/{scheduleId}")
    @PreAuthorize("hasAnyRole('MANAGER', 'SUPER_ADMIN')")
    fun deleteSchedule(@PathVariable scheduleId: String): ResponseEntity<Void> {
        deleteWorkScheduleAppService.execute(scheduleId)
        return ResponseEntity.noContent().build()
    }
}
