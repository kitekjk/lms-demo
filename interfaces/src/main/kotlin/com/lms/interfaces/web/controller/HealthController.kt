package com.lms.interfaces.web.controller

import com.lms.interfaces.web.dto.ApiResponse
import java.time.LocalDateTime
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Health Check 컨트롤러
 * 애플리케이션 상태 확인
 */
@RestController
@RequestMapping("/api/health")
class HealthController {

    /**
     * 기본 Health Check
     */
    @GetMapping
    fun health(): ResponseEntity<ApiResponse<HealthStatus>> {
        val status = HealthStatus(
            status = "UP",
            timestamp = LocalDateTime.now(),
            service = "lms-demo"
        )

        return ResponseEntity.ok(
            ApiResponse.success(status, "Application is running")
        )
    }

    data class HealthStatus(val status: String, val timestamp: LocalDateTime, val service: String)
}
