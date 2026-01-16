plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.spring) apply false
    alias(libs.plugins.kotlin.jpa) apply false
    alias(libs.plugins.spring.boot) apply false
    alias(libs.plugins.spring.dependency.management) apply false
    alias(libs.plugins.spotless) apply false
}

allprojects {
    group = "com.example.lms"
    version = "0.0.1-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "com.diffplug.spotless")

    tasks.withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }

    dependencies {
        val implementation by configurations
        val testImplementation by configurations

        // Kotlin
        implementation(rootProject.libs.kotlin.reflect)
        implementation(rootProject.libs.kotlin.stdlib)

        // Testing
        testImplementation(rootProject.libs.kotest.runner.junit5)
        testImplementation(rootProject.libs.kotest.assertions.core)
        testImplementation(rootProject.libs.kotest.property)
        testImplementation(rootProject.libs.mockk)
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "17"
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    configure<com.diffplug.gradle.spotless.SpotlessExtension> {
        kotlin {
            target("**/*.kt")
            targetExclude("**/build/**/*.kt")
            ktlint(rootProject.libs.versions.ktlint.get())
                .editorConfigOverride(
                    mapOf(
                        "ktlint_standard_no-wildcard-imports" to "disabled",
                        "ktlint_standard_trailing-comma-on-call-site" to "disabled",
                        "ktlint_standard_trailing-comma-on-declaration-site" to "disabled",
                        "ktlint_standard_filename" to "disabled",
                        "max_line_length" to "120"
                    )
                )
        }
        kotlinGradle {
            target("*.gradle.kts")
            ktlint(rootProject.libs.versions.ktlint.get())
        }
    }
}
