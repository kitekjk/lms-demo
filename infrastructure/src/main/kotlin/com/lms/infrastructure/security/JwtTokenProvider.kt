package com.lms.infrastructure.security

import com.lms.infrastructure.config.JwtProperties
import io.jsonwebtoken.*
import io.jsonwebtoken.security.Keys
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.util.*

/**
 * JWT 토큰 생성 및 검증 프로바이더
 */
@Component
class JwtTokenProvider(
    private val jwtProperties: JwtProperties
) {
    private val secretKey = Keys.hmacShaKeyFor(
        jwtProperties.secretKey.toByteArray(StandardCharsets.UTF_8)
    )

    /**
     * Access Token 생성
     * @param employeeId 사용자 ID
     * @param role 역할 (SUPER_ADMIN, MANAGER, EMPLOYEE)
     * @param storeId 매장 ID (nullable)
     */
    fun generateAccessToken(employeeId: String, role: String, storeId: String?): String {
        val now = Date()
        val expiryDate = Date(now.time + jwtProperties.accessTokenExpiration)

        val builder = Jwts.builder()
            .subject(employeeId)
            .claim("role", role)
            .claim("type", "access")
            .issuedAt(now)
            .expiration(expiryDate)

        storeId?.let { builder.claim("storeId", it) }

        return builder.signWith(secretKey, SignatureAlgorithm.HS256).compact()
    }

    /**
     * Refresh Token 생성
     * @param employeeId 사용자 ID
     */
    fun generateRefreshToken(employeeId: String): String {
        val now = Date()
        val expiryDate = Date(now.time + jwtProperties.refreshTokenExpiration)

        return Jwts.builder()
            .subject(employeeId)
            .claim("type", "refresh")
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(secretKey, SignatureAlgorithm.HS256)
            .compact()
    }

    /**
     * 토큰에서 사용자 ID 추출
     */
    fun getUserIdFromToken(token: String): String {
        return getClaims(token).subject
    }

    /**
     * 토큰에서 역할 추출
     */
    fun getRoleFromToken(token: String): String {
        return getClaims(token)["role"] as String
    }

    /**
     * 토큰에서 매장 ID 추출
     */
    fun getStoreIdFromToken(token: String): String? {
        return getClaims(token)["storeId"] as String?
    }

    /**
     * 토큰 유효성 검증
     * @return true if valid, false otherwise
     */
    fun validateToken(token: String): Boolean {
        return try {
            getClaims(token)
            true
        } catch (e: JwtException) {
            false
        } catch (e: IllegalArgumentException) {
            false
        }
    }

    /**
     * 토큰에서 Authentication 객체 생성
     */
    fun getAuthentication(token: String): Authentication {
        val employeeId = getUserIdFromToken(token)
        val role = getRoleFromToken(token)

        val authorities: Collection<GrantedAuthority> = listOf(
            SimpleGrantedAuthority("ROLE_$role")
        )

        return UsernamePasswordAuthenticationToken(employeeId, null, authorities)
    }

    /**
     * 토큰에서 Claims 추출
     */
    private fun getClaims(token: String): Claims {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .payload
    }

    /**
     * 토큰 타입 확인 (access/refresh)
     */
    fun getTokenType(token: String): String {
        return getClaims(token)["type"] as String
    }

    /**
     * Access Token인지 확인
     */
    fun isAccessToken(token: String): Boolean {
        return try {
            getTokenType(token) == "access"
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Refresh Token인지 확인
     */
    fun isRefreshToken(token: String): Boolean {
        return try {
            getTokenType(token) == "refresh"
        } catch (e: Exception) {
            false
        }
    }
}
