package com.lms.infrastructure.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.util.StringUtils
import org.springframework.web.filter.OncePerRequestFilter

/**
 * JWT 인증 필터
 * 모든 요청을 가로채서 Authorization 헤더의 JWT 토큰을 검증하고
 * SecurityContext에 인증 정보를 설정합니다.
 */
@Component
class JwtAuthenticationFilter(
    private val jwtTokenProvider: JwtTokenProvider
) : OncePerRequestFilter() {

    private val log = LoggerFactory.getLogger(javaClass)

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
            // 1. Authorization 헤더에서 JWT 토큰 추출
            val token = resolveToken(request)

            // 2. 토큰이 있고 유효한 경우
            if (token != null && jwtTokenProvider.validateToken(token)) {
                // 3. Access Token인지 확인 (Refresh Token은 인증에 사용 불가)
                if (jwtTokenProvider.isAccessToken(token)) {
                    // 4. 토큰에서 인증 정보 생성
                    val authentication = jwtTokenProvider.getAuthentication(token)

                    // 5. SecurityContext에 인증 정보 설정
                    SecurityContextHolder.getContext().authentication = authentication

                    log.debug(
                        "Set Authentication to SecurityContext for '{}', uri: {}",
                        authentication.name,
                        request.requestURI
                    )
                } else {
                    log.warn("Refresh token cannot be used for authentication, uri: {}", request.requestURI)
                }
            } else {
                log.debug("No valid JWT token found, uri: {}", request.requestURI)
            }
        } catch (e: Exception) {
            log.error("Could not set user authentication in security context", e)
        }

        // 6. 다음 필터로 진행
        filterChain.doFilter(request, response)
    }

    /**
     * Authorization 헤더에서 Bearer 토큰 추출
     * @return JWT 토큰 문자열 또는 null
     */
    private fun resolveToken(request: HttpServletRequest): String? {
        val bearerToken = request.getHeader(AUTHORIZATION_HEADER)

        return if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            bearerToken.substring(BEARER_PREFIX.length)
        } else {
            null
        }
    }
}
