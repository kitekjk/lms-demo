package com.lms.infrastructure.web

import com.lms.domain.common.DomainContext
import jakarta.servlet.http.HttpServletRequest
import org.springframework.core.MethodParameter
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer

/**
 * Controller 메서드 파라미터에 DomainContext를 자동으로 주입하는 ArgumentResolver
 */
@Component
class DomainContextArgumentResolver : HandlerMethodArgumentResolver {

    override fun supportsParameter(parameter: MethodParameter): Boolean =
        DomainContext::class.java.isAssignableFrom(parameter.parameterType)

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?
    ): Any? {
        val request = webRequest.getNativeRequest(HttpServletRequest::class.java)
            ?: throw IllegalStateException("HttpServletRequest를 가져올 수 없습니다")

        return request.getAttribute(DomainContextInterceptor.DOMAIN_CONTEXT_ATTRIBUTE)
            ?: throw IllegalStateException("DomainContext가 설정되지 않았습니다. DomainContextInterceptor가 등록되어 있는지 확인하세요")
    }
}
