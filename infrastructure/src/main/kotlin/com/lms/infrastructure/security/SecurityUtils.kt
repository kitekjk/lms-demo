package com.lms.infrastructure.security

import com.lms.infrastructure.security.jwt.JwtAuthenticationDetails
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder

/**
 * SecurityContext 유틸리티
 * 현재 인증된 사용자 정보에 쉽게 접근하기 위한 헬퍼 함수들
 */
object SecurityUtils {

    /**
     * 현재 인증된 사용자의 ID 반환
     * @return 사용자 ID 또는 null (인증되지 않은 경우)
     */
    fun getCurrentUserId(): String? = getAuthentication()?.name

    /**
     * 현재 인증된 사용자의 역할 반환
     * @return 역할 문자열 (SUPER_ADMIN, MANAGER, EMPLOYEE) 또는 null
     */
    fun getCurrentUserRole(): String? = getAuthentication()?.authorities
        ?.firstOrNull()
        ?.authority
        ?.removePrefix("ROLE_")

    /**
     * 현재 사용자가 특정 역할을 가지고 있는지 확인
     * @param role 확인할 역할 (예: "SUPER_ADMIN")
     * @return true if user has the role
     */
    fun hasRole(role: String): Boolean {
        val authorities = getAuthentication()?.authorities ?: return false
        return authorities.any { it.authority == "ROLE_$role" }
    }

    /**
     * 현재 사용자가 여러 역할 중 하나를 가지고 있는지 확인
     * @param roles 확인할 역할 목록
     * @return true if user has any of the roles
     */
    fun hasAnyRole(vararg roles: String): Boolean = roles.any { hasRole(it) }

    /**
     * 현재 사용자가 SUPER_ADMIN인지 확인
     */
    fun isSuperAdmin(): Boolean = hasRole("SUPER_ADMIN")

    /**
     * 현재 사용자가 MANAGER인지 확인
     */
    fun isManager(): Boolean = hasRole("MANAGER")

    /**
     * 현재 사용자가 EMPLOYEE인지 확인
     */
    fun isEmployee(): Boolean = hasRole("EMPLOYEE")

    /**
     * 현재 사용자가 특정 사용자 ID와 일치하는지 확인
     * @param employeeId 비교할 사용자 ID
     * @return true if current user ID matches
     */
    fun isCurrentUser(employeeId: String): Boolean = getCurrentUserId() == employeeId

    /**
     * 현재 사용자가 인증되었는지 확인
     * @return true if authenticated
     */
    fun isAuthenticated(): Boolean {
        val authentication = getAuthentication()
        return authentication != null && authentication.isAuthenticated
    }

    /**
     * 현재 Authentication 객체 반환
     * @return Authentication 또는 null
     */
    private fun getAuthentication(): Authentication? = SecurityContextHolder.getContext()?.authentication

    /**
     * 현재 사용자의 모든 권한 반환
     * @return 권한 목록
     */
    fun getCurrentUserAuthorities(): Collection<GrantedAuthority> = getAuthentication()?.authorities ?: emptyList()

    /**
     * 현재 인증된 사용자의 매장 ID 조회
     * @return 매장 ID 또는 null
     */
    fun getCurrentStoreId(): String? {
        val authentication = getAuthentication() ?: return null
        val details = authentication.details as? JwtAuthenticationDetails
        return details?.storeId
    }

    /**
     * 현재 사용자가 특정 매장에 속해있는지 확인
     * @param storeId 확인할 매장 ID
     * @return true if user belongs to the store
     */
    fun belongsToStore(storeId: String): Boolean = getCurrentStoreId() == storeId

    /**
     * 현재 사용자의 JwtAuthenticationDetails 조회
     * @return JwtAuthenticationDetails 또는 null
     */
    fun getCurrentDetails(): JwtAuthenticationDetails? {
        val authentication = getAuthentication() ?: return null
        return authentication.details as? JwtAuthenticationDetails
    }
}
