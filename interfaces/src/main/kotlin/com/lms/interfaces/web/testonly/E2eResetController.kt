package com.lms.interfaces.web.testonly

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.context.annotation.Profile
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Test-only reset endpoint.
 *
 * ACTIVE ONLY under the `e2e` Spring profile. NEVER loaded in local, dev, or prod.
 *
 * POST /test-only/reset
 *   Truncates transactional tables only. Reference data (users/employees/stores/policies)
 *   is populated at boot via `spring.sql.init.mode: always` and stays untouched.
 *   This keeps the reset simple and avoids transaction-boundary issues with
 *   mid-request SQL script execution.
 */
@RestController
@RequestMapping("/test-only")
@Profile("e2e")
class E2eResetController {

    @PersistenceContext
    private lateinit var em: EntityManager

    // Transactional tables only — ordered FK-safely.
    // Reference tables (employees, users, stores, payroll_policies) intentionally
    // excluded: tests don't mutate them, and boot-time seeding handles initial load.
    private val truncateOrder = listOf(
        "payroll_details",
        "payroll_batch_histories",
        "payrolls",
        "leave_requests",
        "attendance_records",
        "work_schedules",
    )

    @PostMapping("/reset")
    @Transactional
    fun reset(): ResponseEntity<Map<String, Any>> {
        em.createNativeQuery("SET FOREIGN_KEY_CHECKS = 0").executeUpdate()
        truncateOrder.forEach { table ->
            em.createNativeQuery("TRUNCATE TABLE $table").executeUpdate()
        }
        em.createNativeQuery("SET FOREIGN_KEY_CHECKS = 1").executeUpdate()
        em.flush()

        return ResponseEntity.ok(mapOf(
            "status" to "ok",
            "truncated" to truncateOrder,
            "note" to "Reference data (users/employees/stores/policies) preserved — seeded at boot.",
        ))
    }
}
