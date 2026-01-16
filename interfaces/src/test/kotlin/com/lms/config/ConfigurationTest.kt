package com.lms.config

import com.lms.infrastructure.config.JwtProperties
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource

/**
 * application.yml 설정값 로딩 테스트
 */
@SpringBootTest
@TestPropertySource(
    properties = [
        "jwt.secret-key=test-secret-key-for-testing-purposes-only",
        "jwt.access-token-expiration=7200000",
        "jwt.refresh-token-expiration=1209600000"
    ]
)
class ConfigurationTest(private val jwtProperties: JwtProperties) :
    StringSpec({

        "JWT 설정값이 올바르게 로드되는지 검증" {
            jwtProperties.secretKey shouldNotBe ""
            jwtProperties.secretKey shouldBe "test-secret-key-for-testing-purposes-only"
            jwtProperties.accessTokenExpiration shouldBe 7200000L
            jwtProperties.refreshTokenExpiration shouldBe 1209600000L
        }

        "JWT 기본값이 설정되어 있는지 검증" {
            jwtProperties.accessTokenExpiration shouldNotBe 0L
            jwtProperties.refreshTokenExpiration shouldNotBe 0L
        }
    })
