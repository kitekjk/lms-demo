package com.lms.interfaces.web.controller

import com.lms.application.store.CreateStoreAppService
import com.lms.application.store.DeleteStoreAppService
import com.lms.application.store.GetAllStoresAppService
import com.lms.application.store.GetStoreAppService
import com.lms.application.store.UpdateStoreAppService
import com.lms.application.store.dto.CreateStoreCommand
import com.lms.application.store.dto.UpdateStoreCommand
import com.lms.domain.common.DomainContext
import com.lms.interfaces.web.dto.StoreCreateRequest
import com.lms.interfaces.web.dto.StoreListResponse
import com.lms.interfaces.web.dto.StoreResponse
import com.lms.interfaces.web.dto.StoreUpdateRequest
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
 * 매장 관리 REST API 컨트롤러
 * SUPER_ADMIN만 매장 CRUD 작업 가능
 */
@Tag(name = "매장 관리", description = "매장 생성, 조회, 수정, 삭제 API")
@RestController
@RequestMapping("/api/stores")
class StoreController(
    private val createStoreAppService: CreateStoreAppService,
    private val getStoreAppService: GetStoreAppService,
    private val getAllStoresAppService: GetAllStoresAppService,
    private val updateStoreAppService: UpdateStoreAppService,
    private val deleteStoreAppService: DeleteStoreAppService
) {

    /**
     * 매장 생성
     * SUPER_ADMIN만 가능
     */
    @Operation(
        summary = "매장 생성",
        description = "새로운 매장을 생성합니다. SUPER_ADMIN 권한이 필요합니다.",
        security = [SecurityRequirement(name = "Bearer Authentication")]
    )
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "매장 생성 성공"),
        ApiResponse(responseCode = "400", description = "잘못된 요청"),
        ApiResponse(responseCode = "401", description = "인증 실패"),
        ApiResponse(responseCode = "403", description = "권한 없음")
    )
    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    fun createStore(
        context: DomainContext,
        @Valid @RequestBody request: StoreCreateRequest
    ): ResponseEntity<StoreResponse> {
        val command = CreateStoreCommand(
            name = request.name,
            location = request.location
        )

        val result = createStoreAppService.execute(context, command)
        val response = StoreResponse(
            id = result.id,
            name = result.name,
            location = result.location,
            createdAt = result.createdAt
        )

        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    /**
     * 전체 매장 목록 조회
     * SUPER_ADMIN만 가능
     */
    @Operation(
        summary = "전체 매장 목록 조회",
        description = "모든 매장 목록을 조회합니다. SUPER_ADMIN 권한이 필요합니다.",
        security = [SecurityRequirement(name = "Bearer Authentication")]
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "조회 성공"),
        ApiResponse(responseCode = "401", description = "인증 실패"),
        ApiResponse(responseCode = "403", description = "권한 없음")
    )
    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    fun getAllStores(): ResponseEntity<StoreListResponse> {
        val results = getAllStoresAppService.execute()
        val stores = results.map { result ->
            StoreResponse(
                id = result.id,
                name = result.name,
                location = result.location,
                createdAt = result.createdAt
            )
        }

        val response = StoreListResponse(
            stores = stores,
            totalCount = stores.size
        )

        return ResponseEntity.ok(response)
    }

    /**
     * 매장 상세 조회
     * 인증된 사용자 모두 가능
     */
    @Operation(
        summary = "매장 상세 조회",
        description = "특정 매장의 상세 정보를 조회합니다.",
        security = [SecurityRequirement(name = "Bearer Authentication")]
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "조회 성공"),
        ApiResponse(responseCode = "401", description = "인증 실패"),
        ApiResponse(responseCode = "404", description = "매장을 찾을 수 없음")
    )
    @GetMapping("/{storeId}")
    fun getStore(@PathVariable storeId: String): ResponseEntity<StoreResponse> {
        val result = getStoreAppService.execute(storeId)
            ?: return ResponseEntity.notFound().build()

        val response = StoreResponse(
            id = result.id,
            name = result.name,
            location = result.location,
            createdAt = result.createdAt
        )

        return ResponseEntity.ok(response)
    }

    /**
     * 매장 정보 수정
     * SUPER_ADMIN만 가능
     */
    @Operation(
        summary = "매장 정보 수정",
        description = "매장 정보를 수정합니다. SUPER_ADMIN 권한이 필요합니다.",
        security = [SecurityRequirement(name = "Bearer Authentication")]
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "수정 성공"),
        ApiResponse(responseCode = "400", description = "잘못된 요청"),
        ApiResponse(responseCode = "401", description = "인증 실패"),
        ApiResponse(responseCode = "403", description = "권한 없음"),
        ApiResponse(responseCode = "404", description = "매장을 찾을 수 없음")
    )
    @PutMapping("/{storeId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    fun updateStore(
        context: DomainContext,
        @PathVariable storeId: String,
        @Valid @RequestBody request: StoreUpdateRequest
    ): ResponseEntity<StoreResponse> {
        val command = UpdateStoreCommand(
            name = request.name,
            location = request.location
        )

        val result = updateStoreAppService.execute(context, storeId, command)
        val response = StoreResponse(
            id = result.id,
            name = result.name,
            location = result.location,
            createdAt = result.createdAt
        )

        return ResponseEntity.ok(response)
    }

    /**
     * 매장 삭제
     * SUPER_ADMIN만 가능
     */
    @Operation(
        summary = "매장 삭제",
        description = "매장을 삭제합니다. SUPER_ADMIN 권한이 필요합니다.",
        security = [SecurityRequirement(name = "Bearer Authentication")]
    )
    @ApiResponses(
        ApiResponse(responseCode = "204", description = "삭제 성공"),
        ApiResponse(responseCode = "401", description = "인증 실패"),
        ApiResponse(responseCode = "403", description = "권한 없음"),
        ApiResponse(responseCode = "404", description = "매장을 찾을 수 없음")
    )
    @DeleteMapping("/{storeId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    fun deleteStore(@PathVariable storeId: String): ResponseEntity<Void> {
        deleteStoreAppService.execute(storeId)
        return ResponseEntity.noContent().build()
    }
}
