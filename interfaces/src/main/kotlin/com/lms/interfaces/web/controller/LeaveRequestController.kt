package com.lms.interfaces.web.controller

import com.lms.application.leave.ApproveLeaveRequestAppService
import com.lms.application.leave.CancelLeaveRequestAppService
import com.lms.application.leave.CreateLeaveRequestAppService
import com.lms.application.leave.GetLeaveRequestsByStoreAppService
import com.lms.application.leave.GetMyLeaveRequestsAppService
import com.lms.application.leave.GetPendingLeaveRequestsAppService
import com.lms.application.leave.RejectLeaveRequestAppService
import com.lms.application.leave.dto.CreateLeaveRequestCommand
import com.lms.application.leave.dto.RejectLeaveRequestCommand
import com.lms.domain.common.DomainContext
import com.lms.infrastructure.security.SecurityUtils
import com.lms.interfaces.web.dto.LeaveRejectionRequest
import com.lms.interfaces.web.dto.LeaveRequestCreateRequest
import com.lms.interfaces.web.dto.LeaveRequestListResponse
import com.lms.interfaces.web.dto.LeaveRequestResponse
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
 * 휴가 관리 REST API 컨트롤러
 * 근로자의 휴가 신청/취소 및 매니저의 승인/반려 기능 제공
 */
@Tag(name = "휴가 관리", description = "휴가 신청, 승인, 반려, 취소 API")
@RestController
@RequestMapping("/api/leaves")
class LeaveRequestController(
    private val createLeaveRequestAppService: CreateLeaveRequestAppService,
    private val approveLeaveRequestAppService: ApproveLeaveRequestAppService,
    private val rejectLeaveRequestAppService: RejectLeaveRequestAppService,
    private val cancelLeaveRequestAppService: CancelLeaveRequestAppService,
    private val getMyLeaveRequestsAppService: GetMyLeaveRequestsAppService,
    private val getLeaveRequestsByStoreAppService: GetLeaveRequestsByStoreAppService,
    private val getPendingLeaveRequestsAppService: GetPendingLeaveRequestsAppService
) {

    /**
     * 휴가 신청
     * EMPLOYEE 권한 필요
     */
    @Operation(
        summary = "휴가 신청",
        description = "새로운 휴가를 신청합니다.",
        security = [SecurityRequirement(name = "Bearer Authentication")]
    )
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "휴가 신청 성공"),
        ApiResponse(responseCode = "400", description = "잘못된 요청"),
        ApiResponse(responseCode = "401", description = "인증 실패")
    )
    @PostMapping
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER', 'SUPER_ADMIN')")
    fun createLeaveRequest(
        context: DomainContext,
        @Valid @RequestBody request: LeaveRequestCreateRequest
    ): ResponseEntity<LeaveRequestResponse> {
        val userId = SecurityUtils.getCurrentUserId()
            ?: throw IllegalStateException("인증된 사용자 정보를 찾을 수 없습니다")

        // TODO: userId로 employeeId 조회 (현재는 간단히 userId 사용)
        val command = CreateLeaveRequestCommand(
            employeeId = userId,
            leaveType = request.leaveType,
            startDate = request.startDate,
            endDate = request.endDate,
            reason = request.reason
        )

        val result = createLeaveRequestAppService.execute(context, command)
        val response = LeaveRequestResponse.from(result)

        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    /**
     * 본인 휴가 신청 내역 조회
     */
    @Operation(
        summary = "본인 휴가 신청 내역 조회",
        description = "로그인한 사용자의 휴가 신청 내역을 조회합니다.",
        security = [SecurityRequirement(name = "Bearer Authentication")]
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "조회 성공"),
        ApiResponse(responseCode = "401", description = "인증 실패")
    )
    @GetMapping("/my-leaves")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER', 'SUPER_ADMIN')")
    fun getMyLeaveRequests(): ResponseEntity<LeaveRequestListResponse> {
        val userId = SecurityUtils.getCurrentUserId()
            ?: throw IllegalStateException("인증된 사용자 정보를 찾을 수 없습니다")

        // TODO: userId로 employeeId 조회 (현재는 간단히 userId 사용)
        val results = getMyLeaveRequestsAppService.execute(userId)
        val requests = results.map { LeaveRequestResponse.from(it) }
        val response = LeaveRequestListResponse(
            requests = requests,
            totalCount = requests.size
        )

        return ResponseEntity.ok(response)
    }

    /**
     * 매장별 휴가 신청 목록 조회 (관리자용)
     * MANAGER와 SUPER_ADMIN만 가능
     */
    @Operation(
        summary = "매장별 휴가 신청 목록 조회",
        description = "특정 매장의 모든 휴가 신청 내역을 조회합니다. MANAGER와 SUPER_ADMIN 권한이 필요합니다.",
        security = [SecurityRequirement(name = "Bearer Authentication")]
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "조회 성공"),
        ApiResponse(responseCode = "401", description = "인증 실패"),
        ApiResponse(responseCode = "403", description = "권한 없음")
    )
    @GetMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'SUPER_ADMIN')")
    fun getLeaveRequestsByStore(@RequestParam storeId: String): ResponseEntity<LeaveRequestListResponse> {
        val results = getLeaveRequestsByStoreAppService.execute(storeId)
        val requests = results.map { LeaveRequestResponse.from(it) }
        val response = LeaveRequestListResponse(
            requests = requests,
            totalCount = requests.size
        )

        return ResponseEntity.ok(response)
    }

    /**
     * 대기 중인 휴가 신청 목록 조회 (관리자용)
     * MANAGER와 SUPER_ADMIN만 가능
     */
    @Operation(
        summary = "대기 중인 휴가 신청 목록 조회",
        description = "승인 대기 중인 모든 휴가 신청을 조회합니다. MANAGER와 SUPER_ADMIN 권한이 필요합니다.",
        security = [SecurityRequirement(name = "Bearer Authentication")]
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "조회 성공"),
        ApiResponse(responseCode = "401", description = "인증 실패"),
        ApiResponse(responseCode = "403", description = "권한 없음")
    )
    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('MANAGER', 'SUPER_ADMIN')")
    fun getPendingLeaveRequests(): ResponseEntity<LeaveRequestListResponse> {
        val results = getPendingLeaveRequestsAppService.execute()
        val requests = results.map { LeaveRequestResponse.from(it) }
        val response = LeaveRequestListResponse(
            requests = requests,
            totalCount = requests.size
        )

        return ResponseEntity.ok(response)
    }

    /**
     * 휴가 승인 (관리자용)
     * MANAGER와 SUPER_ADMIN만 가능
     */
    @Operation(
        summary = "휴가 승인",
        description = "휴가 신청을 승인합니다. MANAGER와 SUPER_ADMIN 권한이 필요합니다.",
        security = [SecurityRequirement(name = "Bearer Authentication")]
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "승인 성공"),
        ApiResponse(responseCode = "401", description = "인증 실패"),
        ApiResponse(responseCode = "403", description = "권한 없음"),
        ApiResponse(responseCode = "404", description = "휴가 신청을 찾을 수 없음")
    )
    @PatchMapping("/{leaveId}/approve")
    @PreAuthorize("hasAnyRole('MANAGER', 'SUPER_ADMIN')")
    fun approveLeaveRequest(
        context: DomainContext,
        @PathVariable leaveId: String
    ): ResponseEntity<LeaveRequestResponse> {
        val result = approveLeaveRequestAppService.execute(context, leaveId)
        val response = LeaveRequestResponse.from(result)

        return ResponseEntity.ok(response)
    }

    /**
     * 휴가 반려 (관리자용)
     * MANAGER와 SUPER_ADMIN만 가능
     */
    @Operation(
        summary = "휴가 반려",
        description = "휴가 신청을 반려합니다. MANAGER와 SUPER_ADMIN 권한이 필요합니다.",
        security = [SecurityRequirement(name = "Bearer Authentication")]
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "반려 성공"),
        ApiResponse(responseCode = "400", description = "잘못된 요청"),
        ApiResponse(responseCode = "401", description = "인증 실패"),
        ApiResponse(responseCode = "403", description = "권한 없음"),
        ApiResponse(responseCode = "404", description = "휴가 신청을 찾을 수 없음")
    )
    @PatchMapping("/{leaveId}/reject")
    @PreAuthorize("hasAnyRole('MANAGER', 'SUPER_ADMIN')")
    fun rejectLeaveRequest(
        context: DomainContext,
        @PathVariable leaveId: String,
        @Valid @RequestBody request: LeaveRejectionRequest
    ): ResponseEntity<LeaveRequestResponse> {
        val command = RejectLeaveRequestCommand(
            rejectionReason = request.rejectionReason
        )

        val result = rejectLeaveRequestAppService.execute(context, leaveId, command)
        val response = LeaveRequestResponse.from(result)

        return ResponseEntity.ok(response)
    }

    /**
     * 휴가 신청 취소
     * EMPLOYEE 권한 필요 (본인만 취소 가능)
     */
    @Operation(
        summary = "휴가 신청 취소",
        description = "본인이 신청한 휴가를 취소합니다.",
        security = [SecurityRequirement(name = "Bearer Authentication")]
    )
    @ApiResponses(
        ApiResponse(responseCode = "204", description = "취소 성공"),
        ApiResponse(responseCode = "401", description = "인증 실패"),
        ApiResponse(responseCode = "404", description = "휴가 신청을 찾을 수 없음")
    )
    @DeleteMapping("/{leaveId}")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER', 'SUPER_ADMIN')")
    fun cancelLeaveRequest(context: DomainContext, @PathVariable leaveId: String): ResponseEntity<Void> {
        cancelLeaveRequestAppService.execute(context, leaveId)
        return ResponseEntity.noContent().build()
    }
}
