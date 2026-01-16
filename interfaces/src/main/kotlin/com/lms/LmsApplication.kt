package com.lms

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(
    scanBasePackages = ["com.lms"]
)
class LmsApplication

fun main(args: Array<String>) {
    runApplication<LmsApplication>(*args)
}
