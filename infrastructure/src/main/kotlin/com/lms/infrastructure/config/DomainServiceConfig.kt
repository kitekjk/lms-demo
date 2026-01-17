package com.lms.infrastructure.config

import com.lms.domain.service.LeavePolicyService
import com.lms.domain.service.PayrollCalculationEngine
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * 도메인 서비스를 Spring Bean으로 등록하는 설정
 *
 * 도메인 서비스는 순수 Kotlin 클래스이므로 직접 Spring 어노테이션을 사용하지 않음
 * 대신 이 설정 클래스에서 Bean으로 등록하여 DI 컨테이너에서 관리되도록 함
 */
@Configuration
class DomainServiceConfig {

    @Bean
    fun leavePolicyService(): LeavePolicyService = LeavePolicyService()

    @Bean
    fun payrollCalculationEngine(): PayrollCalculationEngine = PayrollCalculationEngine()
}
