package com.lms.interfaces.web.controller

import com.lms.interfaces.base.IntegrationTestBase
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class AttendanceControllerTest : IntegrationTestBase() {

    init {
        test("POST /api/attendance/check-in - should check in with EMPLOYEE role") {
            // Given
            val token = generateToken("employee-id", "EMPLOYEE", "store-123")
            val request = mapOf(
                "workScheduleId" to "schedule-123"
            )

            // When & Then
            post("/api/attendance/check-in", request, token)
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.employeeId").exists())
                .andExpect(jsonPath("$.workScheduleId").value("schedule-123"))
                .andExpect(jsonPath("$.checkInTime").exists())
                .andExpect(jsonPath("$.status").exists())
        }

        test("POST /api/attendance/check-in - should return 401 without authentication") {
            // Given
            val request = mapOf(
                "workScheduleId" to "schedule-123"
            )

            // When & Then
            post("/api/attendance/check-in", request)
                .andExpect(status().isUnauthorized)
        }

        test("POST /api/attendance/check-in - should return 400 with missing work schedule") {
            // Given
            val token = generateToken("employee-id", "EMPLOYEE")
            val request = emptyMap<String, String>()

            // When & Then
            post("/api/attendance/check-in", request, token)
                .andExpect(status().isBadRequest)
        }

        test("POST /api/attendance/check-out - should check out with EMPLOYEE role") {
            // Given
            val token = generateToken("employee-id", "EMPLOYEE", "store-123")
            val request = emptyMap<String, String>()

            // When & Then
            post("/api/attendance/check-out", request, token)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.checkOutTime").exists())
                .andExpect(jsonPath("$.actualWorkHours").exists())
        }

        test("POST /api/attendance/check-out - should return 401 without authentication") {
            // Given
            val request = emptyMap<String, String>()

            // When & Then
            post("/api/attendance/check-out", request)
                .andExpect(status().isUnauthorized)
        }

        test("GET /api/attendance/my-records - should get my attendance records") {
            // Given
            val token = generateToken("employee-id", "EMPLOYEE")

            // When & Then
            get("/api/attendance/my-records", token)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.records").isArray)
                .andExpect(jsonPath("$.totalCount").exists())
        }

        test("GET /api/attendance/my-records?startDate=2024-01-01&endDate=2024-01-31 - should filter by date range") {
            // Given
            val token = generateToken("employee-id", "EMPLOYEE")

            // When & Then
            get("/api/attendance/my-records?startDate=2024-01-01&endDate=2024-01-31", token)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.records").isArray)
        }

        test("GET /api/attendance/store/{storeId} - should get store attendance records with MANAGER role") {
            // Given
            val token = generateToken("manager-id", "MANAGER", "store-123")
            val storeId = "store-123"

            // When & Then
            get("/api/attendance/store/$storeId", token)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.records").isArray)
                .andExpect(jsonPath("$.totalCount").exists())
        }

        test("GET /api/attendance/store/{storeId} - should return 403 with EMPLOYEE role") {
            // Given
            val token = generateToken("employee-id", "EMPLOYEE")
            val storeId = "store-123"

            // When & Then
            get("/api/attendance/store/$storeId", token)
                .andExpect(status().isForbidden)
        }

        test("PATCH /api/attendance/{recordId}/adjust - should adjust attendance with MANAGER role") {
            // Given
            val token = generateToken("manager-id", "MANAGER")
            val recordId = "record-123"
            val request = mapOf(
                "checkInTime" to "2024-01-01T09:00:00",
                "checkOutTime" to "2024-01-01T18:00:00",
                "note" to "Adjusted by manager"
            )

            // When & Then
            put("/api/attendance/$recordId/adjust", request, token)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.id").value(recordId))
                .andExpect(jsonPath("$.note").value("Adjusted by manager"))
        }

        test("PATCH /api/attendance/{recordId}/adjust - should return 403 with EMPLOYEE role") {
            // Given
            val token = generateToken("employee-id", "EMPLOYEE")
            val recordId = "record-123"
            val request = mapOf(
                "checkInTime" to "2024-01-01T09:00:00",
                "checkOutTime" to "2024-01-01T18:00:00"
            )

            // When & Then
            put("/api/attendance/$recordId/adjust", request, token)
                .andExpect(status().isForbidden)
        }
    }
}
