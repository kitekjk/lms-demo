package com.lms.infrastructure.config

import com.lms.infrastructure.web.DomainContextArgumentResolver
import com.lms.infrastructure.web.DomainContextInterceptor
import org.springframework.context.annotation.Configuration
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

/**
 * Spring MVC 설정
 * DomainContext Interceptor 및 ArgumentResolver 등록
 */
@Configuration
class WebMvcConfig(
    private val domainContextInterceptor: DomainContextInterceptor,
    private val domainContextArgumentResolver: DomainContextArgumentResolver
) : WebMvcConfigurer {

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(domainContextInterceptor)
            .addPathPatterns("/api/**")
    }

    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(domainContextArgumentResolver)
    }
}
