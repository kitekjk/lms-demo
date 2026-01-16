package com.lms.infrastructure.security.jwt

import com.lms.infrastructure.config.JwtProperties
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.util.*
import javax.crypto.SecretKey
import org.springframework.stereotype.Component

/**
 * JWT 토큰 생성 및 검증 유틸리티
 * Access Token과 Refresh Token을 생성하고 검증
 */
@Component
class JwtTokenProvider(private val jwtProperties: JwtProperties) {
    private val secretKey: SecretKey by lazy {
        Keys.hmacShaKeyFor(jwtProperties.secretKey.toByteArray(StandardCharsets.UTF_8))
    }

    /**
     * Access Token 생성
     * @param employeeId 근로자 ID
     * @param role 역할 (SUPER_ADMIN, MANAGER, EMPLOYEE)
     * @param storeId 매장 ID (nullable)
     * @return JWT Access Token
     */
    fun generateAccessToken(employeeId: String, role: String, storeId: String?): String {
        val now = Instant.now()
        val expiration = Date.from(now.plusMillis(jwtProperties.accessTokenExpiration))

        return Jwts.builder()
            .subject(employeeId)
            .claim("role", role)
            .claim("storeId", storeId)
            .issuedAt(Date.from(now))
            .expiration(expiration)
            .signWith(secretKey)
            .compact()
    }

    /**
     * Refresh Token 생성
     * @param employeeId 근로자 ID
     * @return JWT Refresh Token
     */
    fun generateRefreshToken(employeeId: String): String {
        val now = Instant.now()
        val expiration = Date.from(now.plusMillis(jwtProperties.refreshTokenExpiration))

        return Jwts.builder()
            .subject(employeeId)
            .issuedAt(Date.from(now))
            .expiration(expiration)
            .signWith(secretKey)
            .compact()
    }

    /**
     * 토큰 유효성 검증
     * @param token JWT 토큰
     * @return 유효하면 true, 만료되거나 잘못된 토큰이면 false
     */
    fun validateToken(token: String): Boolean = try {
        Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
        true
    } catch (e: Exception) {
        false
    }

    /**
     * 토큰에서 Claims 추출
     * @param token JWT 토큰
     * @return Claims (subject, role, storeId 등)
     */
    fun extractClaims(token: String): Claims = Jwts.parser()
        .verifyWith(secretKey)
        .build()
        .parseSignedClaims(token)
        .payload

    /**
     * 토큰에서 employeeId (subject) 추출
     */
    fun extractEmployeeId(token: String): String = extractClaims(token).subject

    /**
     * 토큰에서 role 추출
     */
    fun extractRole(token: String): String? = extractClaims(token).get("role", String::class.java)

    /**
     * 토큰에서 storeId 추출
     */
    fun extractStoreId(token: String): String? = extractClaims(token).get("storeId", String::class.java)

    /**
     * 토큰 만료 시간 추출
     */
    fun extractExpiration(token: String): Date = extractClaims(token).expiration

    /**
     * 토큰이 만료되었는지 확인
     */
    fun isTokenExpired(token: String): Boolean = try {
        extractExpiration(token).before(Date())
    } catch (e: Exception) {
        true
    }
}
