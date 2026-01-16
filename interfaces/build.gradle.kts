// Interfaces module - REST controllers, message handlers, etc.
// Entry point for external requests

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
}

dependencies {
    // Module dependencies
    implementation(project(":domain"))
    implementation(project(":application"))
    implementation(project(":infrastructure"))

    // Spring Web
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.security)
    implementation(libs.spring.boot.starter.validation)

    // JWT
    implementation(libs.jjwt.api)
    runtimeOnly(libs.jjwt.impl)
    runtimeOnly(libs.jjwt.jackson)

    // Jackson for Kotlin
    implementation(libs.jackson.module.kotlin)

    // Testing
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.spring.mockk)
    testImplementation(libs.kotest.extensions.spring)
    testRuntimeOnly(libs.h2.database)
}
