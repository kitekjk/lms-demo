package com.lms.interfaces.web.dto

import jakarta.validation.constraints.NotBlank

/**
 * 매장 생성 요청 DTO
 */
data class StoreCreateRequest(
    @field:NotBlank(message = "매장명은 필수입니다")
    val name: String,

    @field:NotBlank(message = "위치는 필수입니다")
    val location: String
)

/**
 * 매장 수정 요청 DTO
 */
data class StoreUpdateRequest(
    @field:NotBlank(message = "매장명은 필수입니다")
    val name: String,

    @field:NotBlank(message = "위치는 필수입니다")
    val location: String
)
