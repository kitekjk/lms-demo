package com.lms.infrastructure.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

/**
 * JWT 설정 속성
 * application.yml의 jwt.* 설정을 바인딩
 */
@Configuration
@ConfigurationProperties(prefix = "jwt")
data class JwtProperties(
    var secretKey: String = "",
    var accessTokenExpiration: Long = 3600000, // 1 hour
    var refreshTokenExpiration: Long = 604800000 // 7 days
)
