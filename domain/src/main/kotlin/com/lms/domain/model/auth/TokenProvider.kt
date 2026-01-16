package com.lms.domain.model.auth

/**
 * 토큰 생성 및 검증을 위한 도메인 인터페이스
 * 구체적인 구현(JWT 등)은 Infrastructure 계층에서 제공
 */
interface TokenProvider {
    /**
     * Access Token 생성
     * @param employeeId 근로자 ID
     * @param role 역할 (SUPER_ADMIN, MANAGER, EMPLOYEE)
     * @param storeId 매장 ID (nullable)
     * @return Access Token 문자열
     */
    fun generateAccessToken(employeeId: String, role: String, storeId: String?): String

    /**
     * Refresh Token 생성
     * @param employeeId 근로자 ID
     * @return Refresh Token 문자열
     */
    fun generateRefreshToken(employeeId: String): String

    /**
     * 토큰 유효성 검증
     * @param token 검증할 토큰
     * @return 유효하면 true, 만료되거나 잘못된 토큰이면 false
     */
    fun validateToken(token: String): Boolean

    /**
     * 토큰에서 employeeId 추출
     * @param token 토큰
     * @return employeeId
     */
    fun extractEmployeeId(token: String): String

    /**
     * 토큰에서 role 추출
     * @param token 토큰
     * @return role (nullable)
     */
    fun extractRole(token: String): String?

    /**
     * 토큰에서 storeId 추출
     * @param token 토큰
     * @return storeId (nullable)
     */
    fun extractStoreId(token: String): String?
}
