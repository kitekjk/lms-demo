// Infrastructure module - Technical implementations
// JPA repositories, external API clients, messaging, etc.

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.kotlin.jpa)
    alias(libs.plugins.spring.boot) apply false
    alias(libs.plugins.spring.dependency.management)
}

dependencyManagement {
    imports {
        mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
    }
}

dependencies {
    // Domain dependency
    implementation(project(":domain"))

    // Spring Boot Configuration Processor
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    // Spring Data JPA
    implementation(libs.spring.boot.starter.data.jpa)

    // Spring Security (for JWT filters and authentication)
    implementation(libs.spring.boot.starter.security)

    // Servlet API
    compileOnly("jakarta.servlet:jakarta.servlet-api")
    testImplementation("jakarta.servlet:jakarta.servlet-api")

    // JWT
    implementation(libs.jjwt.api)
    runtimeOnly(libs.jjwt.impl)
    runtimeOnly(libs.jjwt.jackson)

    // Database
    runtimeOnly(libs.mysql.connector)

    // Testing with Spring
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.spring.mockk)
    testImplementation(libs.kotest.extensions.spring)
}
