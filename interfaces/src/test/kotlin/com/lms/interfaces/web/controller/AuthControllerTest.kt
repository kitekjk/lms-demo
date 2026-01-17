package com.lms.interfaces.web.controller

import com.lms.interfaces.base.IntegrationTestBase
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class AuthControllerTest : IntegrationTestBase() {

    private lateinit var passwordEncoder: PasswordEncoder

    init {
        test("POST /api/auth/login - successful login with valid credentials") {
            // Given: Create a test user in database
            val email = "test@example.com"
            val password = "password123"
            val hashedPassword = passwordEncoder.encode(password)

            // TODO: Insert test user into database
            // This will be implemented once we understand the repository structure

            // When & Then
            val request = mapOf(
                "email" to email,
                "password" to password
            )

            post("/api/auth/login", request)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.employeeId").exists())
                .andExpect(jsonPath("$.role").exists())
        }

        test("POST /api/auth/login - should return 401 with invalid credentials") {
            // Given
            val request = mapOf(
                "email" to "test@example.com",
                "password" to "wrongpassword"
            )

            // When & Then
            post("/api/auth/login", request)
                .andExpect(status().isUnauthorized)
                .andExpect(jsonPath("$.code").value("AUTH001"))
        }

        test("POST /api/auth/login - should return 400 with invalid email format") {
            // Given
            val request = mapOf(
                "email" to "invalid-email",
                "password" to "password123"
            )

            // When & Then
            post("/api/auth/login", request)
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
        }

        test("POST /api/auth/login - should return 400 with short password") {
            // Given
            val request = mapOf(
                "email" to "test@example.com",
                "password" to "short"
            )

            // When & Then
            post("/api/auth/login", request)
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
        }

        test("POST /api/auth/register - should successfully register with SUPER_ADMIN role") {
            // Given
            val token = generateToken("super-admin-id", "SUPER_ADMIN")
            val request = mapOf(
                "email" to "newuser@example.com",
                "password" to "password123",
                "role" to "EMPLOYEE",
                "storeId" to "store-123"
            )

            // When & Then
            post("/api/auth/register", request, token)
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.employeeId").exists())
                .andExpect(jsonPath("$.email").value("newuser@example.com"))
                .andExpect(jsonPath("$.role").value("EMPLOYEE"))
        }

        test("POST /api/auth/register - should return 403 without SUPER_ADMIN role") {
            // Given
            val token = generateToken("manager-id", "MANAGER")
            val request = mapOf(
                "email" to "newuser@example.com",
                "password" to "password123",
                "role" to "EMPLOYEE"
            )

            // When & Then
            post("/api/auth/register", request, token)
                .andExpect(status().isForbidden)
        }

        test("POST /api/auth/register - should return 401 without authentication") {
            // Given
            val request = mapOf(
                "email" to "newuser@example.com",
                "password" to "password123",
                "role" to "EMPLOYEE"
            )

            // When & Then
            post("/api/auth/register", request)
                .andExpect(status().isUnauthorized)
        }

        test("POST /api/auth/register - should return 409 with duplicate email") {
            // Given
            val token = generateToken("super-admin-id", "SUPER_ADMIN")
            val request = mapOf(
                "email" to "existing@example.com",
                "password" to "password123",
                "role" to "EMPLOYEE"
            )

            // TODO: Insert existing user with same email

            // When & Then
            post("/api/auth/register", request, token)
                .andExpect(status().isConflict)
                .andExpect(jsonPath("$.code").value("REG001"))
        }

        test("POST /api/auth/refresh - should successfully refresh tokens with valid refresh token") {
            // Given
            val refreshToken = jwtTokenProvider.generateRefreshToken("employee-123")
            val request = mapOf(
                "refreshToken" to refreshToken
            )

            // When & Then
            post("/api/auth/refresh", request)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
        }

        test("POST /api/auth/refresh - should return 401 with invalid refresh token") {
            // Given
            val request = mapOf(
                "refreshToken" to "invalid-token"
            )

            // When & Then
            post("/api/auth/refresh", request)
                .andExpect(status().isUnauthorized)
        }

        test("POST /api/auth/refresh - should return 400 with missing refresh token") {
            // Given
            val request = mapOf(
                "refreshToken" to ""
            )

            // When & Then
            post("/api/auth/refresh", request)
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
        }

        test("POST /api/auth/logout - should successfully logout") {
            // Given & When & Then
            post("/api/auth/logout", emptyMap<String, String>())
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.message").value("로그아웃 되었습니다"))
        }
    }
}
