package com.lms.infrastructure.context

import com.lms.domain.common.DomainContext
import jakarta.servlet.http.HttpServletRequest
import java.time.Instant
import java.util.*

/**
 * HTTP 요청 기반 DomainContext 구현체
 */
data class HttpDomainContext(
    override val serviceName: String,
    override val userId: String,
    override val userName: String,
    override val roleId: String,
    override val requestId: UUID,
    override val requestedAt: Instant,
    override val clientIp: String
) : DomainContext {
    companion object {
        /**
         * HTTP 요청으로부터 DomainContext 생성
         */
        fun from(
            request: HttpServletRequest,
            userId: String = "anonymous",
            userName: String = "Anonymous User",
            roleId: String = "ANONYMOUS"
        ): HttpDomainContext = HttpDomainContext(
            serviceName = "lms-demo",
            userId = userId,
            userName = userName,
            roleId = roleId,
            requestId = UUID.randomUUID(),
            requestedAt = Instant.now(),
            clientIp = extractClientIp(request)
        )

        /**
         * 인증 없이 시스템 DomainContext 생성
         */
        fun system(): HttpDomainContext = HttpDomainContext(
            serviceName = "lms-demo",
            userId = "system",
            userName = "System",
            roleId = "SYSTEM",
            requestId = UUID.randomUUID(),
            requestedAt = Instant.now(),
            clientIp = "127.0.0.1"
        )

        /**
         * 클라이언트 IP 추출
         */
        private fun extractClientIp(request: HttpServletRequest): String {
            val xForwardedFor = request.getHeader("X-Forwarded-For")
            return when {
                xForwardedFor != null -> xForwardedFor.split(",").first().trim()
                else -> request.remoteAddr ?: "unknown"
            }
        }
    }
}
