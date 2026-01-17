package com.lms.interfaces.web.controller

import com.lms.interfaces.base.IntegrationTestBase
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class StoreControllerTest : IntegrationTestBase() {

    init {
        test("POST /api/stores - should create store with SUPER_ADMIN role") {
            // Given
            val token = generateToken("super-admin-id", "SUPER_ADMIN")
            val request = mapOf(
                "name" to "Test Store",
                "location" to "Seoul, Korea"
            )

            // When & Then
            post("/api/stores", request, token)
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Test Store"))
                .andExpect(jsonPath("$.location").value("Seoul, Korea"))
                .andExpect(jsonPath("$.createdAt").exists())
        }

        test("POST /api/stores - should return 403 without SUPER_ADMIN role") {
            // Given
            val token = generateToken("manager-id", "MANAGER")
            val request = mapOf(
                "name" to "Test Store",
                "location" to "Seoul, Korea"
            )

            // When & Then
            post("/api/stores", request, token)
                .andExpect(status().isForbidden)
        }

        test("POST /api/stores - should return 401 without authentication") {
            // Given
            val request = mapOf(
                "name" to "Test Store",
                "location" to "Seoul, Korea"
            )

            // When & Then
            post("/api/stores", request)
                .andExpect(status().isUnauthorized)
        }

        test("POST /api/stores - should return 400 with missing required fields") {
            // Given
            val token = generateToken("super-admin-id", "SUPER_ADMIN")
            val request = mapOf(
                "name" to ""
            )

            // When & Then
            post("/api/stores", request, token)
                .andExpect(status().isBadRequest)
        }

        test("GET /api/stores - should get all stores with SUPER_ADMIN role") {
            // Given
            val token = generateToken("super-admin-id", "SUPER_ADMIN")

            // When & Then
            get("/api/stores", token)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.stores").isArray)
                .andExpect(jsonPath("$.totalCount").exists())
        }

        test("GET /api/stores - should return 403 without SUPER_ADMIN role") {
            // Given
            val token = generateToken("employee-id", "EMPLOYEE")

            // When & Then
            get("/api/stores", token)
                .andExpect(status().isForbidden)
        }

        test("GET /api/stores/{storeId} - should get store details with authentication") {
            // Given
            val token = generateToken("employee-id", "EMPLOYEE")
            val storeId = "test-store-id"

            // When & Then
            get("/api/stores/$storeId", token)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.id").value(storeId))
                .andExpect(jsonPath("$.name").exists())
                .andExpect(jsonPath("$.location").exists())
        }

        test("GET /api/stores/{storeId} - should return 404 for non-existent store") {
            // Given
            val token = generateToken("employee-id", "EMPLOYEE")
            val storeId = "non-existent-store-id"

            // When & Then
            get("/api/stores/$storeId", token)
                .andExpect(status().isNotFound)
        }

        test("PUT /api/stores/{storeId} - should update store with SUPER_ADMIN role") {
            // Given
            val token = generateToken("super-admin-id", "SUPER_ADMIN")
            val storeId = "test-store-id"
            val request = mapOf(
                "name" to "Updated Store Name",
                "location" to "Busan, Korea"
            )

            // When & Then
            put("/api/stores/$storeId", request, token)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.id").value(storeId))
                .andExpect(jsonPath("$.name").value("Updated Store Name"))
                .andExpect(jsonPath("$.location").value("Busan, Korea"))
        }

        test("PUT /api/stores/{storeId} - should return 403 without SUPER_ADMIN role") {
            // Given
            val token = generateToken("manager-id", "MANAGER")
            val storeId = "test-store-id"
            val request = mapOf(
                "name" to "Updated Store Name",
                "location" to "Busan, Korea"
            )

            // When & Then
            put("/api/stores/$storeId", request, token)
                .andExpect(status().isForbidden)
        }

        test("PUT /api/stores/{storeId} - should return 404 for non-existent store") {
            // Given
            val token = generateToken("super-admin-id", "SUPER_ADMIN")
            val storeId = "non-existent-store-id"
            val request = mapOf(
                "name" to "Updated Store Name",
                "location" to "Busan, Korea"
            )

            // When & Then
            put("/api/stores/$storeId", request, token)
                .andExpect(status().isNotFound)
        }

        test("DELETE /api/stores/{storeId} - should delete store with SUPER_ADMIN role") {
            // Given
            val token = generateToken("super-admin-id", "SUPER_ADMIN")
            val storeId = "test-store-id"

            // When & Then
            delete("/api/stores/$storeId", token)
                .andExpect(status().isNoContent)
        }

        test("DELETE /api/stores/{storeId} - should return 403 without SUPER_ADMIN role") {
            // Given
            val token = generateToken("manager-id", "MANAGER")
            val storeId = "test-store-id"

            // When & Then
            delete("/api/stores/$storeId", token)
                .andExpect(status().isForbidden)
        }

        test("DELETE /api/stores/{storeId} - should return 404 for non-existent store") {
            // Given
            val token = generateToken("super-admin-id", "SUPER_ADMIN")
            val storeId = "non-existent-store-id"

            // When & Then
            delete("/api/stores/$storeId", token)
                .andExpect(status().isNotFound)
        }
    }
}
