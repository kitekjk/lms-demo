package com.lms.infrastructure.config

import com.lms.infrastructure.security.jwt.JwtAuthenticationFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
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
 * JWT 기반 인증 및 권한 관리
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
class SecurityConfig(private val jwtAuthenticationFilter: JwtAuthenticationFilter) {

    /**
     * SecurityFilterChain 설정
     */
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            // CSRF 비활성화 (JWT 사용)
            .csrf { it.disable() }
            // CORS 설정
            .cors { it.configurationSource(corsConfigurationSource()) }
            // 세션 사용하지 않음 (Stateless)
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            // 인증/인가 설정
            .authorizeHttpRequests { auth ->
                auth
                    // 인증 없이 접근 가능한 경로
                    .requestMatchers(
                        "/api/auth/**",
                        "/health",
                        "/actuator/health",
                        // Swagger UI
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/api-docs/**",
                        "/v3/api-docs/**"
                    ).permitAll()
                    // OPTIONS 요청은 모두 허용 (CORS preflight)
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    // 나머지는 모두 인증 필요
                    .anyRequest().authenticated()
            }
            // JWT 인증 필터 추가 (UsernamePasswordAuthenticationFilter 전에 실행)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }

    /**
     * CORS 설정
     */
    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration().apply {
            allowedOriginPatterns = listOf("*")
            allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
            allowedHeaders = listOf("*")
            exposedHeaders = listOf("Authorization")
            allowCredentials = true
            maxAge = 3600L
        }

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }

    /**
     * 비밀번호 암호화 인코더
     */
    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()
}
