package com.lms.interfaces.web.testonly

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.context.annotation.Profile
import org.springframework.core.io.ClassPathResource
import org.springframework.http.ResponseEntity
import org.springframework.jdbc.datasource.init.ScriptUtils
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.sql.DataSource

/**
 * Test-only reset endpoint.
 *
 * ACTIVE ONLY under the `e2e` Spring profile. NEVER loaded in local, dev, or prod.
 *
 * POST /test-only/reset
 *   1. Truncates all business tables (FK-safe via SET FOREIGN_KEY_CHECKS=0).
 *   2. Re-runs data.sql to reseed reference data (users/stores/employees/policies).
 */
@RestController
@RequestMapping("/test-only")
@Profile("e2e")
class E2eResetController(private val dataSource: DataSource) {

    @PersistenceContext
    private lateinit var em: EntityManager

    private val truncateOrder = listOf(
        // Transactional tables first
        "payroll_details",
        "payroll_batch_histories",
        "payrolls",
        "payroll_policies",
        "leave_requests",
        "attendance_records",
        "work_schedules",
        // Reference tables (will be reseeded from data.sql)
        "employees",
        "users",
        "stores",
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

        dataSource.connection.use { conn ->
            ScriptUtils.executeSqlScript(conn, ClassPathResource("data.sql"))
        }

        return ResponseEntity.ok(
            mapOf(
                "status" to "ok",
                "truncated" to truncateOrder,
                "reseedFrom" to "classpath:data.sql",
            ),
        )
    }
}
