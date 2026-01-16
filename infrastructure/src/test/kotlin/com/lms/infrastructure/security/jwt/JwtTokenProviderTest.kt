package com.lms.infrastructure.security.jwt

import com.lms.infrastructure.config.JwtProperties
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldNotBeBlank
import io.kotest.matchers.types.shouldBeInstanceOf
import java.util.*

/**
 * JwtTokenProvider 테스트
 */
class JwtTokenProviderTest :
    FunSpec({

        lateinit var jwtTokenProvider: JwtTokenProvider

        beforeTest {
            val jwtProperties = JwtProperties(
                secretKey = "test-secret-key-for-jwt-token-generation-must-be-at-least-256-bits",
                accessTokenExpiration = 3600000, // 1 hour
                refreshTokenExpiration = 604800000 // 7 days
            )
            jwtTokenProvider = JwtTokenProvider(jwtProperties)
        }

        test("generateAccessToken - Access Token 생성 성공") {
            // Given
            val employeeId = "emp-001"
            val role = "MANAGER"
            val storeId = "store-001"

            // When
            val token = jwtTokenProvider.generateAccessToken(employeeId, role, storeId)

            // Then
            token.shouldNotBeBlank()
            token.split(".").size shouldBe 3 // JWT는 header.payload.signature 형식
        }

        test("generateAccessToken - storeId가 null인 경우 토큰 생성 성공") {
            // Given
            val employeeId = "emp-002"
            val role = "SUPER_ADMIN"

            // When
            val token = jwtTokenProvider.generateAccessToken(employeeId, role, null)

            // Then
            token.shouldNotBeBlank()
            jwtTokenProvider.extractStoreId(token) shouldBe null
        }

        test("generateRefreshToken - Refresh Token 생성 성공") {
            // Given
            val employeeId = "emp-001"

            // When
            val token = jwtTokenProvider.generateRefreshToken(employeeId)

            // Then
            token.shouldNotBeBlank()
            token.split(".").size shouldBe 3
        }

        test("validateToken - 유효한 토큰 검증 성공") {
            // Given
            val token = jwtTokenProvider.generateAccessToken("emp-001", "EMPLOYEE", "store-001")

            // When
            val isValid = jwtTokenProvider.validateToken(token)

            // Then
            isValid shouldBe true
        }

        test("validateToken - 잘못된 토큰 검증 실패") {
            // Given
            val invalidToken = "invalid.jwt.token"

            // When
            val isValid = jwtTokenProvider.validateToken(invalidToken)

            // Then
            isValid shouldBe false
        }

        test("extractEmployeeId - 토큰에서 employeeId 추출") {
            // Given
            val employeeId = "emp-001"
            val token = jwtTokenProvider.generateAccessToken(employeeId, "MANAGER", "store-001")

            // When
            val extractedId = jwtTokenProvider.extractEmployeeId(token)

            // Then
            extractedId shouldBe employeeId
        }

        test("extractRole - 토큰에서 role 추출") {
            // Given
            val role = "MANAGER"
            val token = jwtTokenProvider.generateAccessToken("emp-001", role, "store-001")

            // When
            val extractedRole = jwtTokenProvider.extractRole(token)

            // Then
            extractedRole shouldBe role
        }

        test("extractStoreId - 토큰에서 storeId 추출") {
            // Given
            val storeId = "store-001"
            val token = jwtTokenProvider.generateAccessToken("emp-001", "MANAGER", storeId)

            // When
            val extractedStoreId = jwtTokenProvider.extractStoreId(token)

            // Then
            extractedStoreId shouldBe storeId
        }

        test("extractClaims - 토큰에서 Claims 추출") {
            // Given
            val employeeId = "emp-001"
            val role = "EMPLOYEE"
            val storeId = "store-001"
            val token = jwtTokenProvider.generateAccessToken(employeeId, role, storeId)

            // When
            val claims = jwtTokenProvider.extractClaims(token)

            // Then
            claims.subject shouldBe employeeId
            claims["role"] shouldBe role
            claims["storeId"] shouldBe storeId
            claims.issuedAt.shouldBeInstanceOf<Date>()
            claims.expiration.shouldBeInstanceOf<Date>()
        }

        test("extractExpiration - 토큰 만료 시간 추출") {
            // Given
            val token = jwtTokenProvider.generateAccessToken("emp-001", "EMPLOYEE", "store-001")

            // When
            val expiration = jwtTokenProvider.extractExpiration(token)

            // Then
            expiration.shouldBeInstanceOf<Date>()
            expiration.after(Date()) shouldBe true
        }

        test("isTokenExpired - 유효한 토큰은 만료되지 않음") {
            // Given
            val token = jwtTokenProvider.generateAccessToken("emp-001", "EMPLOYEE", "store-001")

            // When
            val isExpired = jwtTokenProvider.isTokenExpired(token)

            // Then
            isExpired shouldBe false
        }

        test("isTokenExpired - 잘못된 토큰은 만료된 것으로 처리") {
            // Given
            val invalidToken = "invalid.jwt.token"

            // When
            val isExpired = jwtTokenProvider.isTokenExpired(invalidToken)

            // Then
            isExpired shouldBe true
        }

        test("Access Token과 Refresh Token의 만료 시간이 다름") {
            // Given
            val employeeId = "emp-001"
            val accessToken = jwtTokenProvider.generateAccessToken(employeeId, "EMPLOYEE", "store-001")
            val refreshToken = jwtTokenProvider.generateRefreshToken(employeeId)

            // When
            val accessExpiration = jwtTokenProvider.extractExpiration(accessToken)
            val refreshExpiration = jwtTokenProvider.extractExpiration(refreshToken)

            // Then
            accessExpiration.shouldNotBe(refreshExpiration)
            refreshExpiration.after(accessExpiration) shouldBe true
        }

        test("동일한 파라미터로 생성한 토큰도 발급 시간이 다르면 다른 토큰") {
            // Given
            val employeeId = "emp-001"
            val role = "EMPLOYEE"
            val storeId = "store-001"

            // When
            val token1 = jwtTokenProvider.generateAccessToken(employeeId, role, storeId)
            Thread.sleep(1000) // 발급 시간을 다르게 하기 위해 1초 대기 (JWT는 초 단위로 iat를 저장)
            val token2 = jwtTokenProvider.generateAccessToken(employeeId, role, storeId)

            // Then
            token1.shouldNotBe(token2)
        }
    })
