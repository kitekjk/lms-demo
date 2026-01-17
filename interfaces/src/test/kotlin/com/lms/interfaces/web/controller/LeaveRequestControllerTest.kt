package com.lms.interfaces.web.controller

import com.lms.interfaces.base.IntegrationTestBase
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class LeaveRequestControllerTest : IntegrationTestBase() {

    init {
        test("POST /api/leave-requests - should create leave request with EMPLOYEE role") {
            // Given
            val token = generateToken("employee-id", "EMPLOYEE", "store-123")
            val request = mapOf(
                "leaveType" to "ANNUAL",
                "startDate" to "2024-01-15",
                "endDate" to "2024-01-16",
                "reason" to "Personal reasons"
            )

            // When & Then
            post("/api/leave-requests", request, token)
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.employeeId").exists())
                .andExpect(jsonPath("$.leaveType").value("ANNUAL"))
                .andExpect(jsonPath("$.startDate").value("2024-01-15"))
                .andExpect(jsonPath("$.endDate").value("2024-01-16"))
                .andExpect(jsonPath("$.status").value("PENDING"))
        }

        test("POST /api/leave-requests - should return 401 without authentication") {
            // Given
            val request = mapOf(
                "leaveType" to "ANNUAL",
                "startDate" to "2024-01-15",
                "endDate" to "2024-01-16",
                "reason" to "Personal reasons"
            )

            // When & Then
            post("/api/leave-requests", request)
                .andExpect(status().isUnauthorized)
        }

        test("POST /api/leave-requests - should return 400 with invalid date range") {
            // Given
            val token = generateToken("employee-id", "EMPLOYEE")
            val request = mapOf(
                "leaveType" to "ANNUAL",
                "startDate" to "2024-01-20",
                "endDate" to "2024-01-15", // End date before start date
                "reason" to "Personal reasons"
            )

            // When & Then
            post("/api/leave-requests", request, token)
                .andExpect(status().isBadRequest)
        }

        test("GET /api/leave-requests/my-requests - should get my leave requests") {
            // Given
            val token = generateToken("employee-id", "EMPLOYEE")

            // When & Then
            get("/api/leave-requests/my-requests", token)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.requests").isArray)
                .andExpect(jsonPath("$.totalCount").exists())
        }

        test("GET /api/leave-requests/my-requests?status=PENDING - should filter by status") {
            // Given
            val token = generateToken("employee-id", "EMPLOYEE")

            // When & Then
            get("/api/leave-requests/my-requests?status=PENDING", token)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.requests").isArray)
        }

        test("GET /api/leave-requests/store/{storeId} - should get store leave requests with MANAGER role") {
            // Given
            val token = generateToken("manager-id", "MANAGER", "store-123")
            val storeId = "store-123"

            // When & Then
            get("/api/leave-requests/store/$storeId", token)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.requests").isArray)
                .andExpect(jsonPath("$.totalCount").exists())
        }

        test("GET /api/leave-requests/store/{storeId} - should return 403 with EMPLOYEE role") {
            // Given
            val token = generateToken("employee-id", "EMPLOYEE")
            val storeId = "store-123"

            // When & Then
            get("/api/leave-requests/store/$storeId", token)
                .andExpect(status().isForbidden)
        }

        test("GET /api/leave-requests/{requestId} - should get leave request details") {
            // Given
            val token = generateToken("employee-id", "EMPLOYEE")
            val requestId = "request-123"

            // When & Then
            get("/api/leave-requests/$requestId", token)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.id").value(requestId))
                .andExpect(jsonPath("$.employeeId").exists())
                .andExpect(jsonPath("$.status").exists())
        }

        test("GET /api/leave-requests/{requestId} - should return 404 for non-existent request") {
            // Given
            val token = generateToken("employee-id", "EMPLOYEE")
            val requestId = "non-existent-request"

            // When & Then
            get("/api/leave-requests/$requestId", token)
                .andExpect(status().isNotFound)
        }

        test("POST /api/leave-requests/{requestId}/approve - should approve request with MANAGER role") {
            // Given
            val token = generateToken("manager-id", "MANAGER")
            val requestId = "request-123"
            val request = emptyMap<String, String>()

            // When & Then
            post("/api/leave-requests/$requestId/approve", request, token)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.id").value(requestId))
                .andExpect(jsonPath("$.status").value("APPROVED"))
        }

        test("POST /api/leave-requests/{requestId}/approve - should return 403 with EMPLOYEE role") {
            // Given
            val token = generateToken("employee-id", "EMPLOYEE")
            val requestId = "request-123"
            val request = emptyMap<String, String>()

            // When & Then
            post("/api/leave-requests/$requestId/approve", request, token)
                .andExpect(status().isForbidden)
        }

        test("POST /api/leave-requests/{requestId}/reject - should reject request with MANAGER role") {
            // Given
            val token = generateToken("manager-id", "MANAGER")
            val requestId = "request-123"
            val request = mapOf(
                "reason" to "Insufficient staffing"
            )

            // When & Then
            post("/api/leave-requests/$requestId/reject", request, token)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.id").value(requestId))
                .andExpect(jsonPath("$.status").value("REJECTED"))
        }

        test("POST /api/leave-requests/{requestId}/reject - should return 403 with EMPLOYEE role") {
            // Given
            val token = generateToken("employee-id", "EMPLOYEE")
            val requestId = "request-123"
            val request = mapOf(
                "reason" to "Insufficient staffing"
            )

            // When & Then
            post("/api/leave-requests/$requestId/reject", request, token)
                .andExpect(status().isForbidden)
        }

        test("DELETE /api/leave-requests/{requestId} - should cancel own request") {
            // Given
            val token = generateToken("employee-id", "EMPLOYEE")
            val requestId = "request-123"

            // When & Then
            delete("/api/leave-requests/$requestId", token)
                .andExpect(status().isNoContent)
        }

        test("DELETE /api/leave-requests/{requestId} - should return 400 for already approved request") {
            // Given
            val token = generateToken("employee-id", "EMPLOYEE")
            val requestId = "approved-request-123"

            // When & Then
            delete("/api/leave-requests/$requestId", token)
                .andExpect(status().isBadRequest)
        }
    }
}
