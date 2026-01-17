package com.lms.interfaces.web.controller

import com.lms.interfaces.base.IntegrationTestBase
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class EmployeeControllerTest : IntegrationTestBase() {

    init {
        test("POST /api/employees - should create employee with MANAGER role") {
            // Given
            val token = generateToken("manager-id", "MANAGER")
            val request = mapOf(
                "userId" to "user-123",
                "name" to "John Doe",
                "employeeType" to "FULL_TIME",
                "storeId" to "store-123"
            )

            // When & Then
            post("/api/employees", request, token)
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.userId").value("user-123"))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.employeeType").value("FULL_TIME"))
                .andExpect(jsonPath("$.storeId").value("store-123"))
                .andExpect(jsonPath("$.isActive").value(true))
        }

        test("POST /api/employees - should create employee with SUPER_ADMIN role") {
            // Given
            val token = generateToken("super-admin-id", "SUPER_ADMIN")
            val request = mapOf(
                "userId" to "user-456",
                "name" to "Jane Smith",
                "employeeType" to "PART_TIME",
                "storeId" to "store-456"
            )

            // When & Then
            post("/api/employees", request, token)
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Jane Smith"))
        }

        test("POST /api/employees - should return 403 with EMPLOYEE role") {
            // Given
            val token = generateToken("employee-id", "EMPLOYEE")
            val request = mapOf(
                "userId" to "user-789",
                "name" to "Bob Wilson",
                "employeeType" to "FULL_TIME",
                "storeId" to "store-789"
            )

            // When & Then
            post("/api/employees", request, token)
                .andExpect(status().isForbidden)
        }

        test("POST /api/employees - should return 401 without authentication") {
            // Given
            val request = mapOf(
                "userId" to "user-789",
                "name" to "Bob Wilson",
                "employeeType" to "FULL_TIME",
                "storeId" to "store-789"
            )

            // When & Then
            post("/api/employees", request)
                .andExpect(status().isUnauthorized)
        }

        test("POST /api/employees - should return 400 with missing required fields") {
            // Given
            val token = generateToken("manager-id", "MANAGER")
            val request = mapOf(
                "userId" to "user-123"
                // Missing name, employeeType, storeId
            )

            // When & Then
            post("/api/employees", request, token)
                .andExpect(status().isBadRequest)
        }

        test("GET /api/employees - should get all employees with authentication") {
            // Given
            val token = generateToken("employee-id", "EMPLOYEE")

            // When & Then
            get("/api/employees", token)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.employees").isArray)
                .andExpect(jsonPath("$.totalCount").exists())
        }

        test("GET /api/employees?storeId=store-123 - should filter employees by store") {
            // Given
            val token = generateToken("manager-id", "MANAGER")

            // When & Then
            get("/api/employees?storeId=store-123", token)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.employees").isArray)
                .andExpect(jsonPath("$.totalCount").exists())
        }

        test("GET /api/employees?activeOnly=true - should filter active employees only") {
            // Given
            val token = generateToken("manager-id", "MANAGER")

            // When & Then
            get("/api/employees?activeOnly=true", token)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.employees").isArray)
        }

        test("GET /api/employees/{employeeId} - should get employee details") {
            // Given
            val token = generateToken("employee-id", "EMPLOYEE")
            val employeeId = "test-employee-id"

            // When & Then
            get("/api/employees/$employeeId", token)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.id").value(employeeId))
                .andExpect(jsonPath("$.name").exists())
                .andExpect(jsonPath("$.employeeType").exists())
        }

        test("GET /api/employees/{employeeId} - should return 404 for non-existent employee") {
            // Given
            val token = generateToken("employee-id", "EMPLOYEE")
            val employeeId = "non-existent-employee-id"

            // When & Then
            get("/api/employees/$employeeId", token)
                .andExpect(status().isNotFound)
        }

        test("PUT /api/employees/{employeeId} - should update employee with MANAGER role") {
            // Given
            val token = generateToken("manager-id", "MANAGER")
            val employeeId = "test-employee-id"
            val request = mapOf(
                "name" to "Updated Name",
                "employeeType" to "PART_TIME",
                "storeId" to "updated-store-id"
            )

            // When & Then
            put("/api/employees/$employeeId", request, token)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.id").value(employeeId))
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.employeeType").value("PART_TIME"))
        }

        test("PUT /api/employees/{employeeId} - should return 403 with EMPLOYEE role") {
            // Given
            val token = generateToken("employee-id", "EMPLOYEE")
            val employeeId = "test-employee-id"
            val request = mapOf(
                "name" to "Updated Name",
                "employeeType" to "PART_TIME",
                "storeId" to "updated-store-id"
            )

            // When & Then
            put("/api/employees/$employeeId", request, token)
                .andExpect(status().isForbidden)
        }

        test("PUT /api/employees/{employeeId} - should return 404 for non-existent employee") {
            // Given
            val token = generateToken("manager-id", "MANAGER")
            val employeeId = "non-existent-employee-id"
            val request = mapOf(
                "name" to "Updated Name",
                "employeeType" to "PART_TIME",
                "storeId" to "updated-store-id"
            )

            // When & Then
            put("/api/employees/$employeeId", request, token)
                .andExpect(status().isNotFound)
        }

        test("PATCH /api/employees/{employeeId}/deactivate - should deactivate employee with MANAGER role") {
            // Given
            val token = generateToken("manager-id", "MANAGER")
            val employeeId = "test-employee-id"

            // When & Then
            mockMvc.perform(
                patch("/api/employees/$employeeId/deactivate")
                    .header("Authorization", "Bearer $token")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.id").value(employeeId))
                .andExpect(jsonPath("$.isActive").value(false))
        }

        test("PATCH /api/employees/{employeeId}/deactivate - should return 403 with EMPLOYEE role") {
            // Given
            val token = generateToken("employee-id", "EMPLOYEE")
            val employeeId = "test-employee-id"

            // When & Then
            mockMvc.perform(
                patch("/api/employees/$employeeId/deactivate")
                    .header("Authorization", "Bearer $token")
            )
                .andExpect(status().isForbidden)
        }

        test("PATCH /api/employees/{employeeId}/deactivate - should return 404 for non-existent employee") {
            // Given
            val token = generateToken("manager-id", "MANAGER")
            val employeeId = "non-existent-employee-id"

            // When & Then
            mockMvc.perform(
                patch("/api/employees/$employeeId/deactivate")
                    .header("Authorization", "Bearer $token")
            )
                .andExpect(status().isNotFound)
        }
    }
}
