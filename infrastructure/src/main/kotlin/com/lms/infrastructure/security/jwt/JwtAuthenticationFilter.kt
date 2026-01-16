package com.lms.infrastructure.security.jwt

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

/**
 * JWT 인증 필터
 * 요청마다 JWT 토큰을 검증하고 SecurityContext에 인증 정보 설정
 */
@Component
class JwtAuthenticationFilter(private val jwtTokenProvider: JwtTokenProvider) : OncePerRequestFilter() {

    companion object {
        private const val AUTHORIZATION_HEADER = "Authorization"
        private const val BEARER_PREFIX = "Bearer "
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            val token = extractTokenFromRequest(request)

            if (token != null && jwtTokenProvider.validateToken(token)) {
                val employeeId = jwtTokenProvider.extractEmployeeId(token)
                val role = jwtTokenProvider.extractRole(token)
                val storeId = jwtTokenProvider.extractStoreId(token)

                // GrantedAuthority 생성 (ROLE_ prefix 추가)
                val authorities = listOf(SimpleGrantedAuthority("ROLE_$role"))

                // UsernamePasswordAuthenticationToken 생성
                val authentication = UsernamePasswordAuthenticationToken(
                    employeeId,
                    null,
                    authorities
                )

                // JWT에서 추출한 추가 정보를 details에 저장
                val details = JwtAuthenticationDetails(
                    employeeId = employeeId,
                    role = role ?: "",
                    storeId = storeId
                )
                authentication.details = details

                // SecurityContext에 인증 정보 설정
                SecurityContextHolder.getContext().authentication = authentication
            }
        } catch (e: Exception) {
            logger.error("JWT 인증 처리 중 오류 발생", e)
        }

        filterChain.doFilter(request, response)
    }

    /**
     * HTTP 요청 헤더에서 Bearer 토큰 추출
     */
    private fun extractTokenFromRequest(request: HttpServletRequest): String? {
        val bearerToken = request.getHeader(AUTHORIZATION_HEADER)

        return if (bearerToken != null && bearerToken.startsWith(BEARER_PREFIX)) {
            bearerToken.substring(BEARER_PREFIX.length)
        } else {
            null
        }
    }
}

/**
 * JWT 인증 상세 정보
 * SecurityContext의 authentication.details에 저장되는 정보
 */
data class JwtAuthenticationDetails(val employeeId: String, val role: String, val storeId: String?)
