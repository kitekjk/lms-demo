package com.lms.infrastructure.web

import com.lms.infrastructure.context.AuditContext
import com.lms.infrastructure.context.AuditContextHolder
import com.lms.infrastructure.context.HttpDomainContext
import com.lms.infrastructure.security.SecurityUtils
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.ModelAndView

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

        // AuditContext 설정 (reason은 요청 본문에서 추출하거나 기본값 사용)
        val auditContext = AuditContext(
            domainContext = context,
            reason = null // 기본값, 필요시 요청에서 추출
        )
        AuditContextHolder.setContext(auditContext)

        return true
    }

    override fun postHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        modelAndView: ModelAndView?
    ) {
        // 요청 처리 후 ThreadLocal 정리
        AuditContextHolder.clear()
    }

    override fun afterCompletion(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        ex: Exception?
    ) {
        // 예외 발생 시에도 ThreadLocal 정리
        AuditContextHolder.clear()
    }
}
