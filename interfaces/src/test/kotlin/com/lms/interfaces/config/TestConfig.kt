package com.lms.interfaces.config

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

@TestConfiguration
class TestConfig {
    @Bean
    fun testPasswordEncoder(): PasswordEncoder = BCryptPasswordEncoder()
}
