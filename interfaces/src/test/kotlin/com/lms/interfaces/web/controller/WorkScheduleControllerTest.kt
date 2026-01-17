package com.lms.interfaces.web.controller

import com.lms.interfaces.base.IntegrationTestBase
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class WorkScheduleControllerTest : IntegrationTestBase() {

    init {
        test("POST /api/work-schedules - should create work schedule with MANAGER role") {
            // Given
            val token = generateToken("manager-id", "MANAGER", "store-123")
            val request = mapOf(
                "employeeId" to "employee-123",
                "storeId" to "store-123",
                "workDate" to "2024-01-15",
                "startTime" to "09:00:00",
                "endTime" to "18:00:00"
            )

            // When & Then
            post("/api/work-schedules", request, token)
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.employeeId").value("employee-123"))
                .andExpect(jsonPath("$.workDate").value("2024-01-15"))
                .andExpect(jsonPath("$.startTime").exists())
                .andExpect(jsonPath("$.endTime").exists())
        }

        test("POST /api/work-schedules - should return 403 with EMPLOYEE role") {
            // Given
            val token = generateToken("employee-id", "EMPLOYEE")
            val request = mapOf(
                "employeeId" to "employee-123",
                "storeId" to "store-123",
                "workDate" to "2024-01-15",
                "startTime" to "09:00:00",
                "endTime" to "18:00:00"
            )

            // When & Then
            post("/api/work-schedules", request, token)
                .andExpect(status().isForbidden)
        }

        test("POST /api/work-schedules - should return 400 with invalid date") {
            // Given
            val token = generateToken("manager-id", "MANAGER")
            val request = mapOf(
                "employeeId" to "employee-123",
                "storeId" to "store-123",
                "workDate" to "invalid-date",
                "startTime" to "09:00:00",
                "endTime" to "18:00:00"
            )

            // When & Then
            post("/api/work-schedules", request, token)
                .andExpect(status().isBadRequest)
        }

        test("GET /api/work-schedules - should get all work schedules with MANAGER role") {
            // Given
            val token = generateToken("manager-id", "MANAGER")

            // When & Then
            get("/api/work-schedules", token)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.schedules").isArray)
                .andExpect(jsonPath("$.totalCount").exists())
        }

        test("GET /api/work-schedules?storeId=store-123 - should filter schedules by store") {
            // Given
            val token = generateToken("manager-id", "MANAGER")

            // When & Then
            get("/api/work-schedules?storeId=store-123", token)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.schedules").isArray)
        }

        test("GET /api/work-schedules?employeeId=employee-123 - should filter schedules by employee") {
            // Given
            val token = generateToken("manager-id", "MANAGER")

            // When & Then
            get("/api/work-schedules?employeeId=employee-123", token)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.schedules").isArray)
        }

        test("GET /api/work-schedules/{scheduleId} - should get schedule details") {
            // Given
            val token = generateToken("employee-id", "EMPLOYEE")
            val scheduleId = "schedule-123"

            // When & Then
            get("/api/work-schedules/$scheduleId", token)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.id").value(scheduleId))
                .andExpect(jsonPath("$.employeeId").exists())
                .andExpect(jsonPath("$.workDate").exists())
        }

        test("GET /api/work-schedules/{scheduleId} - should return 404 for non-existent schedule") {
            // Given
            val token = generateToken("employee-id", "EMPLOYEE")
            val scheduleId = "non-existent-schedule"

            // When & Then
            get("/api/work-schedules/$scheduleId", token)
                .andExpect(status().isNotFound)
        }

        test("PUT /api/work-schedules/{scheduleId} - should update schedule with MANAGER role") {
            // Given
            val token = generateToken("manager-id", "MANAGER")
            val scheduleId = "schedule-123"
            val request = mapOf(
                "workDate" to "2024-01-16",
                "startTime" to "10:00:00",
                "endTime" to "19:00:00"
            )

            // When & Then
            put("/api/work-schedules/$scheduleId", request, token)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.id").value(scheduleId))
                .andExpect(jsonPath("$.workDate").value("2024-01-16"))
        }

        test("PUT /api/work-schedules/{scheduleId} - should return 403 with EMPLOYEE role") {
            // Given
            val token = generateToken("employee-id", "EMPLOYEE")
            val scheduleId = "schedule-123"
            val request = mapOf(
                "workDate" to "2024-01-16",
                "startTime" to "10:00:00",
                "endTime" to "19:00:00"
            )

            // When & Then
            put("/api/work-schedules/$scheduleId", request, token)
                .andExpect(status().isForbidden)
        }

        test("DELETE /api/work-schedules/{scheduleId} - should delete schedule with MANAGER role") {
            // Given
            val token = generateToken("manager-id", "MANAGER")
            val scheduleId = "schedule-123"

            // When & Then
            delete("/api/work-schedules/$scheduleId", token)
                .andExpect(status().isNoContent)
        }

        test("DELETE /api/work-schedules/{scheduleId} - should return 403 with EMPLOYEE role") {
            // Given
            val token = generateToken("employee-id", "EMPLOYEE")
            val scheduleId = "schedule-123"

            // When & Then
            delete("/api/work-schedules/$scheduleId", token)
                .andExpect(status().isForbidden)
        }
    }
}
