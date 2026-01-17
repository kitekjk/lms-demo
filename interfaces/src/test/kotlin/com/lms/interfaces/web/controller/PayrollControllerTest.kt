package com.lms.interfaces.web.controller

import com.lms.interfaces.base.IntegrationTestBase
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class PayrollControllerTest : IntegrationTestBase() {

    init {
        test("POST /api/payroll/calculate - should calculate payroll with MANAGER role") {
            // Given
            val token = generateToken("manager-id", "MANAGER", "store-123")
            val request = mapOf(
                "employeeId" to "employee-123",
                "yearMonth" to "2024-01"
            )

            // When & Then
            post("/api/payroll/calculate", request, token)
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.employeeId").value("employee-123"))
                .andExpect(jsonPath("$.yearMonth").value("2024-01"))
                .andExpect(jsonPath("$.totalWorkHours").exists())
                .andExpect(jsonPath("$.baseSalary").exists())
                .andExpect(jsonPath("$.totalSalary").exists())
        }

        test("POST /api/payroll/calculate - should return 403 with EMPLOYEE role") {
            // Given
            val token = generateToken("employee-id", "EMPLOYEE")
            val request = mapOf(
                "employeeId" to "employee-123",
                "yearMonth" to "2024-01"
            )

            // When & Then
            post("/api/payroll/calculate", request, token)
                .andExpect(status().isForbidden)
        }

        test("POST /api/payroll/calculate - should return 400 with invalid yearMonth format") {
            // Given
            val token = generateToken("manager-id", "MANAGER")
            val request = mapOf(
                "employeeId" to "employee-123",
                "yearMonth" to "invalid-format"
            )

            // When & Then
            post("/api/payroll/calculate", request, token)
                .andExpect(status().isBadRequest)
        }

        test("POST /api/payroll/batch-calculate - should calculate batch payroll with MANAGER role") {
            // Given
            val token = generateToken("manager-id", "MANAGER", "store-123")
            val request = mapOf(
                "storeId" to "store-123",
                "yearMonth" to "2024-01"
            )

            // When & Then
            post("/api/payroll/batch-calculate", request, token)
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.payrolls").isArray)
                .andExpect(jsonPath("$.totalCount").exists())
                .andExpect(jsonPath("$.successCount").exists())
        }

        test("POST /api/payroll/batch-calculate - should return 403 with EMPLOYEE role") {
            // Given
            val token = generateToken("employee-id", "EMPLOYEE")
            val request = mapOf(
                "storeId" to "store-123",
                "yearMonth" to "2024-01"
            )

            // When & Then
            post("/api/payroll/batch-calculate", request, token)
                .andExpect(status().isForbidden)
        }

        test("GET /api/payroll/my-payrolls - should get my payroll records") {
            // Given
            val token = generateToken("employee-id", "EMPLOYEE")

            // When & Then
            get("/api/payroll/my-payrolls", token)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.payrolls").isArray)
                .andExpect(jsonPath("$.totalCount").exists())
        }

        test("GET /api/payroll/my-payrolls?yearMonth=2024-01 - should filter by yearMonth") {
            // Given
            val token = generateToken("employee-id", "EMPLOYEE")

            // When & Then
            get("/api/payroll/my-payrolls?yearMonth=2024-01", token)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.payrolls").isArray)
        }

        test("GET /api/payroll/store/{storeId} - should get store payrolls with MANAGER role") {
            // Given
            val token = generateToken("manager-id", "MANAGER", "store-123")
            val storeId = "store-123"

            // When & Then
            get("/api/payroll/store/$storeId", token)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.payrolls").isArray)
                .andExpect(jsonPath("$.totalCount").exists())
        }

        test("GET /api/payroll/store/{storeId} - should return 403 with EMPLOYEE role") {
            // Given
            val token = generateToken("employee-id", "EMPLOYEE")
            val storeId = "store-123"

            // When & Then
            get("/api/payroll/store/$storeId", token)
                .andExpect(status().isForbidden)
        }

        test("GET /api/payroll/store/{storeId}?yearMonth=2024-01 - should filter store payrolls by month") {
            // Given
            val token = generateToken("manager-id", "MANAGER")
            val storeId = "store-123"

            // When & Then
            get("/api/payroll/store/$storeId?yearMonth=2024-01", token)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.payrolls").isArray)
        }

        test("GET /api/payroll/{payrollId} - should get payroll details") {
            // Given
            val token = generateToken("employee-id", "EMPLOYEE")
            val payrollId = "payroll-123"

            // When & Then
            get("/api/payroll/$payrollId", token)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.id").value(payrollId))
                .andExpect(jsonPath("$.employeeId").exists())
                .andExpect(jsonPath("$.totalSalary").exists())
        }

        test("GET /api/payroll/{payrollId} - should return 404 for non-existent payroll") {
            // Given
            val token = generateToken("employee-id", "EMPLOYEE")
            val payrollId = "non-existent-payroll"

            // When & Then
            get("/api/payroll/$payrollId", token)
                .andExpect(status().isNotFound)
        }

        test("PUT /api/payroll/{payrollId}/approve - should approve payroll with MANAGER role") {
            // Given
            val token = generateToken("manager-id", "MANAGER")
            val payrollId = "payroll-123"
            val request = emptyMap<String, String>()

            // When & Then
            put("/api/payroll/$payrollId/approve", request, token)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.id").value(payrollId))
                .andExpect(jsonPath("$.status").value("APPROVED"))
        }

        test("PUT /api/payroll/{payrollId}/approve - should return 403 with EMPLOYEE role") {
            // Given
            val token = generateToken("employee-id", "EMPLOYEE")
            val payrollId = "payroll-123"
            val request = emptyMap<String, String>()

            // When & Then
            put("/api/payroll/$payrollId/approve", request, token)
                .andExpect(status().isForbidden)
        }

        test("DELETE /api/payroll/{payrollId} - should delete payroll with MANAGER role") {
            // Given
            val token = generateToken("manager-id", "MANAGER")
            val payrollId = "payroll-123"

            // When & Then
            delete("/api/payroll/$payrollId", token)
                .andExpect(status().isNoContent)
        }

        test("DELETE /api/payroll/{payrollId} - should return 403 with EMPLOYEE role") {
            // Given
            val token = generateToken("employee-id", "EMPLOYEE")
            val payrollId = "payroll-123"

            // When & Then
            delete("/api/payroll/$payrollId", token)
                .andExpect(status().isForbidden)
        }

        test("GET /api/payroll/employee/{employeeId}/summary - should get employee payroll summary") {
            // Given
            val token = generateToken("manager-id", "MANAGER")
            val employeeId = "employee-123"

            // When & Then
            get("/api/payroll/employee/$employeeId/summary", token)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.employeeId").value(employeeId))
                .andExpect(jsonPath("$.totalPayrolls").exists())
                .andExpect(jsonPath("$.totalSalaryPaid").exists())
        }
    }
}
