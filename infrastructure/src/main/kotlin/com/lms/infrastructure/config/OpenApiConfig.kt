package com.lms.infrastructure.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.servers.Server
import io.swagger.v3.oas.models.tags.Tag
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Springdoc OpenAPI 3.0 설정
 * Swagger UI 및 API 문서 자동 생성
 */
@Configuration
class OpenApiConfig {

    @Bean
    fun openAPI(): OpenAPI {
        val jwtSecurityScheme = SecurityScheme()
            .type(SecurityScheme.Type.HTTP)
            .scheme("bearer")
            .bearerFormat("JWT")
            .`in`(SecurityScheme.In.HEADER)
            .name("Authorization")
            .description("JWT 토큰을 입력하세요 (Bearer 접두어 제외)")

        val securityRequirement = SecurityRequirement().addList("Bearer Authentication")

        return OpenAPI()
            .info(
                Info()
                    .title("LMS 근태 관리 시스템 API")
                    .version("1.0.0")
                    .description(
                        """
                        ## LMS (Labor Management System) API 문서

                        근로자의 출퇴근, 근무 일정, 휴가 신청 및 급여 조회를 지원하는 근태 관리 시스템입니다.

                        ### 인증 방식
                        - JWT Bearer Token 인증 사용
                        - `/api/auth/login`으로 로그인 후 Access Token 발급
                        - 모든 API 요청 시 `Authorization: Bearer {accessToken}` 헤더 필요

                        ### 권한 체계
                        - **SUPER_ADMIN**: 전체 시스템 관리 (매장 생성, 모든 매장 접근)
                        - **STORE_ADMIN**: 자기 매장의 전체 관리 (근로자 등록, 일정 관리 등)
                        - **MANAGER**: 자기 매장의 근무 일정, 휴가 승인 관리
                        - **EMPLOYEE**: 본인의 출퇴근, 일정 조회, 휴가 신청

                        ### 주요 기능
                        1. **인증**: 로그인, 회원가입, 토큰 갱신
                        2. **매장 관리**: 매장 CRUD
                        3. **근로자 관리**: 근로자 등록, 조회, 수정
                        4. **근무 일정**: 일정 생성, 조회, 수정
                        5. **출퇴근 관리**: 출퇴근 체크, 기록 조회 및 수정
                        6. **휴가 관리**: 휴가 신청, 승인/거부
                        7. **급여 정책**: 급여 정책 등록 및 관리
                        8. **급여 조회**: 급여 계산 및 조회
                        """.trimIndent()
                    )
                    .contact(
                        Contact()
                            .name("LMS Development Team")
                            .email("support@lms.com")
                    )
                    .license(
                        License()
                            .name("Apache 2.0")
                            .url("https://www.apache.org/licenses/LICENSE-2.0.html")
                    )
            )
            .servers(
                listOf(
                    Server().url("http://localhost:8080").description("로컬 개발 서버"),
                    Server().url("https://api.lms.com").description("프로덕션 서버")
                )
            )
            .components(
                Components()
                    .addSecuritySchemes("Bearer Authentication", jwtSecurityScheme)
            )
            .addSecurityItem(securityRequirement)
            .tags(
                listOf(
                    Tag().name("인증").description("로그인, 회원가입, 토큰 갱신 API"),
                    Tag().name("매장 관리").description("매장 생성, 조회, 수정, 삭제 API"),
                    Tag().name("근로자 관리").description("근로자 등록, 조회, 수정 API"),
                    Tag().name("근무 일정").description("일정 등록, 조회, 수정, 삭제 API"),
                    Tag().name("출퇴근 관리").description("출근, 퇴근, 기록 조회 및 수정 API"),
                    Tag().name("휴가 관리").description("휴가 신청, 승인, 거부, 조회 API"),
                    Tag().name("급여 정책").description("급여 정책 등록, 조회, 수정 API"),
                    Tag().name("급여 조회").description("급여 계산, 조회, 배치 실행 API")
                )
            )
    }
}
