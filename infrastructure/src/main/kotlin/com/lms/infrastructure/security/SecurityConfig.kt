package com.lms.infrastructure.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

/**
 * Spring Security 설정
 * - JWT 기반 인증
 * - RBAC (Role Based Access Control)
 * - CORS 설정
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter
) {

    /**
     * Security Filter Chain 설정
     */
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            // CSRF 비활성화 (JWT 사용)
            .csrf { it.disable() }

            // CORS 설정 활성화
            .cors { it.configurationSource(corsConfigurationSource()) }

            // Session 비활성화 (Stateless)
            .sessionManagement {
                it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }

            // 요청 인가 설정
            .authorizeHttpRequests { auth ->
                auth
                    // 인증 API는 모두 허용
                    .requestMatchers("/api/auth/**").permitAll()

                    // Actuator health check 허용
                    .requestMatchers("/actuator/health").permitAll()

                    // 그 외 모든 요청은 인증 필요
                    .anyRequest().authenticated()
            }

            // JWT 인증 필터 추가 (UsernamePasswordAuthenticationFilter 앞에)
            .addFilterBefore(
                jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter::class.java
            )

        return http.build()
    }

    /**
     * CORS 설정
     * Flutter Web 및 Mobile 앱의 요청을 허용
     */
    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration().apply {
            // 허용할 Origin (개발 환경)
            allowedOrigins = listOf(
                "http://localhost:3000",      // Flutter Web (dev)
                "http://localhost:8080",      // 로컬 테스트
                "http://127.0.0.1:3000",
                "http://127.0.0.1:8080"
            )

            // 허용할 HTTP 메서드
            allowedMethods = listOf(
                "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
            )

            // 허용할 헤더
            allowedHeaders = listOf(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "Accept",
                "Origin"
            )

            // 노출할 헤더
            exposedHeaders = listOf(
                "Authorization"
            )

            // 인증 정보 허용 (쿠키 등)
            allowCredentials = true

            // Preflight 요청 캐시 시간 (1시간)
            maxAge = 3600L
        }

        return UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", configuration)
        }
    }

    /**
     * 비밀번호 암호화 인코더
     * BCrypt 알고리즘 사용 (strength 10)
     */
    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }
}
