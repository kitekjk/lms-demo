package com.lms.infrastructure.security

import com.lms.infrastructure.config.JwtProperties
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.springframework.security.core.authority.SimpleGrantedAuthority

/**
 * JWT 토큰 생성 및 검증 테스트
 */
class JwtTokenProviderTest :
    StringSpec({

        val jwtProperties = JwtProperties().apply {
            secretKey = "test-secret-key-for-testing-purposes-must-be-at-least-256-bits-long"
            accessTokenExpiration = 3600000
            refreshTokenExpiration = 604800000
        }
        val jwtTokenProvider = JwtTokenProvider(jwtProperties)

        "Access Token 생성 및 파싱 테스트" {
            // Given
            val employeeId = "emp-001"
            val role = "MANAGER"
            val storeId = "store-001"

            // When
            val token = jwtTokenProvider.generateAccessToken(employeeId, role, storeId)

            // Then
            token shouldNotBe ""
            jwtTokenProvider.getUserIdFromToken(token) shouldBe employeeId
            jwtTokenProvider.getRoleFromToken(token) shouldBe role
            jwtTokenProvider.getStoreIdFromToken(token) shouldBe storeId
            jwtTokenProvider.getTokenType(token) shouldBe "access"
        }

        "Refresh Token 생성 및 파싱 테스트" {
            // Given
            val employeeId = "emp-002"

            // When
            val token = jwtTokenProvider.generateRefreshToken(employeeId)

            // Then
            token shouldNotBe ""
            jwtTokenProvider.getUserIdFromToken(token) shouldBe employeeId
            jwtTokenProvider.getTokenType(token) shouldBe "refresh"
        }

        "유효한 토큰 검증 테스트" {
            // Given
            val token = jwtTokenProvider.generateAccessToken("emp-001", "EMPLOYEE", null)

            // When
            val isValid = jwtTokenProvider.validateToken(token)

            // Then
            isValid shouldBe true
        }

        "잘못된 토큰 검증 실패 테스트" {
            // Given
            val invalidToken = "invalid.jwt.token"

            // When
            val isValid = jwtTokenProvider.validateToken(invalidToken)

            // Then
            isValid shouldBe false
        }

        "토큰에서 Authentication 객체 생성 테스트" {
            // Given
            val employeeId = "emp-003"
            val role = "SUPER_ADMIN"
            val token = jwtTokenProvider.generateAccessToken(employeeId, role, null)

            // When
            val authentication = jwtTokenProvider.getAuthentication(token)

            // Then
            authentication.name shouldBe employeeId
            authentication.authorities shouldContain SimpleGrantedAuthority("ROLE_$role")
        }

        "Access Token 타입 확인 테스트" {
            // Given
            val accessToken = jwtTokenProvider.generateAccessToken("emp-001", "EMPLOYEE", null)
            val refreshToken = jwtTokenProvider.generateRefreshToken("emp-001")

            // When & Then
            jwtTokenProvider.isAccessToken(accessToken) shouldBe true
            jwtTokenProvider.isRefreshToken(accessToken) shouldBe false

            jwtTokenProvider.isAccessToken(refreshToken) shouldBe false
            jwtTokenProvider.isRefreshToken(refreshToken) shouldBe true
        }

        "매장 ID가 없는 경우 null 반환 테스트" {
            // Given
            val token = jwtTokenProvider.generateAccessToken("emp-001", "EMPLOYEE", null)

            // When
            val storeId = jwtTokenProvider.getStoreIdFromToken(token)

            // Then
            storeId shouldBe null
        }
    })
