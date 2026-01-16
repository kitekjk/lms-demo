package com.lms.interfaces.web.controller

import com.lms.application.attendance.AdjustAttendanceAppService
import com.lms.application.attendance.CheckInAppService
import com.lms.application.attendance.CheckOutAppService
import com.lms.application.attendance.GetAttendanceRecordsByStoreAppService
import com.lms.application.attendance.GetMyAttendanceRecordsAppService
import com.lms.application.attendance.dto.AdjustAttendanceCommand
import com.lms.application.attendance.dto.CheckInCommand
import com.lms.application.attendance.dto.CheckOutCommand
import com.lms.domain.common.DomainContext
import com.lms.infrastructure.security.SecurityUtils
import com.lms.interfaces.web.dto.AttendanceAdjustRequest
import com.lms.interfaces.web.dto.AttendanceRecordListResponse
import com.lms.interfaces.web.dto.AttendanceRecordResponse
import com.lms.interfaces.web.dto.CheckInRequest
import com.lms.interfaces.web.dto.CheckOutRequest
import jakarta.validation.Valid
import java.time.LocalDate
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

/**
 * 출퇴근 관리 REST API 컨트롤러
 * 근로자의 출근/퇴근 및 본인 기록 조회 기능 제공
 */
@RestController
@RequestMapping("/api/attendance")
class AttendanceController(
    private val checkInAppService: CheckInAppService,
    private val checkOutAppService: CheckOutAppService,
    private val getMyAttendanceRecordsAppService: GetMyAttendanceRecordsAppService,
    private val getAttendanceRecordsByStoreAppService: GetAttendanceRecordsByStoreAppService,
    private val adjustAttendanceAppService: AdjustAttendanceAppService
) {

    /**
     * 출근 체크
     * EMPLOYEE 권한 필요
     */
    @PostMapping("/check-in")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER', 'SUPER_ADMIN')")
    fun checkIn(
        context: DomainContext,
        @Valid @RequestBody request: CheckInRequest
    ): ResponseEntity<AttendanceRecordResponse> {
        val userId = SecurityUtils.getCurrentUserId()
            ?: throw IllegalStateException("인증된 사용자 정보를 찾을 수 없습니다")

        // TODO: userId로 employeeId 조회 (현재는 간단히 userId 사용)
        val command = CheckInCommand(
            employeeId = userId,
            workScheduleId = request.workScheduleId
        )

        val result = checkInAppService.execute(context, command)
        val response = AttendanceRecordResponse(
            id = result.id,
            employeeId = result.employeeId,
            workScheduleId = result.workScheduleId,
            attendanceDate = result.attendanceDate,
            checkInTime = result.checkInTime,
            checkOutTime = result.checkOutTime,
            actualWorkHours = result.actualWorkHours,
            status = result.status,
            note = result.note,
            createdAt = result.createdAt
        )

        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    /**
     * 퇴근 체크
     * EMPLOYEE 권한 필요
     */
    @PostMapping("/check-out")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER', 'SUPER_ADMIN')")
    fun checkOut(
        context: DomainContext,
        @Valid @RequestBody request: CheckOutRequest
    ): ResponseEntity<AttendanceRecordResponse> {
        val userId = SecurityUtils.getCurrentUserId()
            ?: throw IllegalStateException("인증된 사용자 정보를 찾을 수 없습니다")

        // TODO: userId로 employeeId 조회 (현재는 간단히 userId 사용)
        val command = CheckOutCommand(employeeId = userId)

        val result = checkOutAppService.execute(context, command)
        val response = AttendanceRecordResponse(
            id = result.id,
            employeeId = result.employeeId,
            workScheduleId = result.workScheduleId,
            attendanceDate = result.attendanceDate,
            checkInTime = result.checkInTime,
            checkOutTime = result.checkOutTime,
            actualWorkHours = result.actualWorkHours,
            status = result.status,
            note = result.note,
            createdAt = result.createdAt
        )

        return ResponseEntity.ok(response)
    }

    /**
     * 본인 출퇴근 기록 조회
     * 날짜 범위 필터링 지원 (startDate, endDate 쿼리 파라미터)
     */
    @GetMapping("/my-records")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER', 'SUPER_ADMIN')")
    fun getMyRecords(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate?
    ): ResponseEntity<AttendanceRecordListResponse> {
        val userId = SecurityUtils.getCurrentUserId()
            ?: throw IllegalStateException("인증된 사용자 정보를 찾을 수 없습니다")

        // TODO: userId로 employeeId 조회 (현재는 간단히 userId 사용)
        val results = getMyAttendanceRecordsAppService.execute(userId, startDate, endDate)

        val records = results.map { result ->
            AttendanceRecordResponse(
                id = result.id,
                employeeId = result.employeeId,
                workScheduleId = result.workScheduleId,
                attendanceDate = result.attendanceDate,
                checkInTime = result.checkInTime,
                checkOutTime = result.checkOutTime,
                actualWorkHours = result.actualWorkHours,
                status = result.status,
                note = result.note,
                createdAt = result.createdAt
            )
        }

        val response = AttendanceRecordListResponse(
            records = records,
            totalCount = records.size
        )

        return ResponseEntity.ok(response)
    }

    /**
     * 매장별 출퇴근 기록 조회 (관리자용)
     * MANAGER와 SUPER_ADMIN만 가능
     */
    @GetMapping("/records")
    @PreAuthorize("hasAnyRole('MANAGER', 'SUPER_ADMIN')")
    fun getRecordsByStore(
        @RequestParam storeId: String,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate?
    ): ResponseEntity<AttendanceRecordListResponse> {
        val results = getAttendanceRecordsByStoreAppService.execute(storeId, startDate, endDate)

        val records = results.map { result ->
            AttendanceRecordResponse(
                id = result.id,
                employeeId = result.employeeId,
                workScheduleId = result.workScheduleId,
                attendanceDate = result.attendanceDate,
                checkInTime = result.checkInTime,
                checkOutTime = result.checkOutTime,
                actualWorkHours = result.actualWorkHours,
                status = result.status,
                note = result.note,
                createdAt = result.createdAt
            )
        }

        val response = AttendanceRecordListResponse(
            records = records,
            totalCount = records.size
        )

        return ResponseEntity.ok(response)
    }

    /**
     * 출퇴근 기록 수정 (관리자용)
     * MANAGER와 SUPER_ADMIN만 가능
     */
    @PutMapping("/records/{recordId}")
    @PreAuthorize("hasAnyRole('MANAGER', 'SUPER_ADMIN')")
    fun adjustRecord(
        context: DomainContext,
        @PathVariable recordId: String,
        @Valid @RequestBody request: AttendanceAdjustRequest
    ): ResponseEntity<AttendanceRecordResponse> {
        val command = AdjustAttendanceCommand(
            adjustedCheckInTime = request.adjustedCheckInTime,
            adjustedCheckOutTime = request.adjustedCheckOutTime,
            reason = request.reason
        )

        val result = adjustAttendanceAppService.execute(context, recordId, command)
        val response = AttendanceRecordResponse(
            id = result.id,
            employeeId = result.employeeId,
            workScheduleId = result.workScheduleId,
            attendanceDate = result.attendanceDate,
            checkInTime = result.checkInTime,
            checkOutTime = result.checkOutTime,
            actualWorkHours = result.actualWorkHours,
            status = result.status,
            note = result.note,
            createdAt = result.createdAt
        )

        return ResponseEntity.ok(response)
    }
}
