package com.lms.interfaces.web.controller

import com.lms.interfaces.base.IntegrationTestBase
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class PayrollPolicyControllerTest : IntegrationTestBase() {

    init {
        test("POST /api/payroll-policies - should create payroll policy with SUPER_ADMIN role") {
            // Given
            val token = generateToken("super-admin-id", "SUPER_ADMIN")
            val request = mapOf(
                "storeId" to "store-123",
                "baseSalary" to 2000000,
                "hourlyRate" to 10000,
                "overtimeRate" to 1.5,
                "nightShiftRate" to 1.3,
                "weekendRate" to 1.5,
                "holidayRate" to 2.0,
                "effectiveFrom" to "2024-01-01"
            )

            // When & Then
            post("/api/payroll-policies", request, token)
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.storeId").value("store-123"))
                .andExpect(jsonPath("$.baseSalary").value(2000000))
                .andExpect(jsonPath("$.hourlyRate").value(10000))
                .andExpect(jsonPath("$.overtimeRate").value(1.5))
        }

        test("POST /api/payroll-policies - should return 403 without SUPER_ADMIN role") {
            // Given
            val token = generateToken("manager-id", "MANAGER")
            val request = mapOf(
                "storeId" to "store-123",
                "baseSalary" to 2000000,
                "hourlyRate" to 10000
            )

            // When & Then
            post("/api/payroll-policies", request, token)
                .andExpect(status().isForbidden)
        }

        test("POST /api/payroll-policies - should return 400 with invalid rates") {
            // Given
            val token = generateToken("super-admin-id", "SUPER_ADMIN")
            val request = mapOf(
                "storeId" to "store-123",
                "baseSalary" to -1000, // Negative salary
                "hourlyRate" to 10000
            )

            // When & Then
            post("/api/payroll-policies", request, token)
                .andExpect(status().isBadRequest)
        }

        test("GET /api/payroll-policies - should get all policies with SUPER_ADMIN role") {
            // Given
            val token = generateToken("super-admin-id", "SUPER_ADMIN")

            // When & Then
            get("/api/payroll-policies", token)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.policies").isArray)
                .andExpect(jsonPath("$.totalCount").exists())
        }

        test("GET /api/payroll-policies?storeId=store-123 - should filter by store") {
            // Given
            val token = generateToken("super-admin-id", "SUPER_ADMIN")

            // When & Then
            get("/api/payroll-policies?storeId=store-123", token)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.policies").isArray)
        }

        test("GET /api/payroll-policies/{policyId} - should get policy details") {
            // Given
            val token = generateToken("manager-id", "MANAGER")
            val policyId = "policy-123"

            // When & Then
            get("/api/payroll-policies/$policyId", token)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.id").value(policyId))
                .andExpect(jsonPath("$.storeId").exists())
                .andExpect(jsonPath("$.baseSalary").exists())
        }

        test("GET /api/payroll-policies/{policyId} - should return 404 for non-existent policy") {
            // Given
            val token = generateToken("manager-id", "MANAGER")
            val policyId = "non-existent-policy"

            // When & Then
            get("/api/payroll-policies/$policyId", token)
                .andExpect(status().isNotFound)
        }

        test("PUT /api/payroll-policies/{policyId} - should update policy with SUPER_ADMIN role") {
            // Given
            val token = generateToken("super-admin-id", "SUPER_ADMIN")
            val policyId = "policy-123"
            val request = mapOf(
                "baseSalary" to 2500000,
                "hourlyRate" to 12000,
                "overtimeRate" to 1.6
            )

            // When & Then
            put("/api/payroll-policies/$policyId", request, token)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.id").value(policyId))
                .andExpect(jsonPath("$.baseSalary").value(2500000))
                .andExpect(jsonPath("$.hourlyRate").value(12000))
        }

        test("PUT /api/payroll-policies/{policyId} - should return 403 with MANAGER role") {
            // Given
            val token = generateToken("manager-id", "MANAGER")
            val policyId = "policy-123"
            val request = mapOf(
                "baseSalary" to 2500000,
                "hourlyRate" to 12000
            )

            // When & Then
            put("/api/payroll-policies/$policyId", request, token)
                .andExpect(status().isForbidden)
        }

        test("DELETE /api/payroll-policies/{policyId} - should delete policy with SUPER_ADMIN role") {
            // Given
            val token = generateToken("super-admin-id", "SUPER_ADMIN")
            val policyId = "policy-123"

            // When & Then
            delete("/api/payroll-policies/$policyId", token)
                .andExpect(status().isNoContent)
        }

        test("DELETE /api/payroll-policies/{policyId} - should return 403 with MANAGER role") {
            // Given
            val token = generateToken("manager-id", "MANAGER")
            val policyId = "policy-123"

            // When & Then
            delete("/api/payroll-policies/$policyId", token)
                .andExpect(status().isForbidden)
        }

        test("GET /api/payroll-policies/store/{storeId}/current - should get current policy for store") {
            // Given
            val token = generateToken("manager-id", "MANAGER")
            val storeId = "store-123"

            // When & Then
            get("/api/payroll-policies/store/$storeId/current", token)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.storeId").value(storeId))
                .andExpect(jsonPath("$.isActive").value(true))
        }

        test("GET /api/payroll-policies/store/{storeId}/current - should return 404 if no active policy") {
            // Given
            val token = generateToken("manager-id", "MANAGER")
            val storeId = "store-without-policy"

            // When & Then
            get("/api/payroll-policies/store/$storeId/current", token)
                .andExpect(status().isNotFound)
        }
    }
}
