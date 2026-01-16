package com.lms.infrastructure.web

import com.lms.infrastructure.context.HttpDomainContext
import com.lms.infrastructure.security.SecurityUtils
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor

/**
 * DomainContext를 생성하고 요청 속성에 저장하는 Interceptor
 * 모든 HTTP 요청에 대해 DomainContext를 자동으로 생성
 */
@Component
class DomainContextInterceptor : HandlerInterceptor {

    companion object {
        const val DOMAIN_CONTEXT_ATTRIBUTE = "domainContext"
    }

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        val context = HttpDomainContext.from(
            request = request,
            userId = SecurityUtils.getCurrentUserId() ?: "anonymous",
            userName = SecurityUtils.getCurrentUserId() ?: "Anonymous User",
            roleId = SecurityUtils.getCurrentUserRole() ?: "ANONYMOUS"
        )

        request.setAttribute(DOMAIN_CONTEXT_ATTRIBUTE, context)
        return true
    }
}
