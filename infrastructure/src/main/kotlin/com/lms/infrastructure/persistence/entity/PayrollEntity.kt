package com.lms.infrastructure.persistence.entity

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.math.BigDecimal
import java.time.Instant
import java.util.*

/**
 * 급여 JPA Entity
 */
@Entity
@Table(
    name = "payrolls",
    indexes = [
        Index(name = "idx_payroll_employee", columnList = "employee_id"),
        Index(name = "idx_payroll_period", columnList = "period"),
        Index(name = "idx_payroll_paid", columnList = "is_paid")
    ]
)
@EntityListeners(AuditingEntityListener::class)
class PayrollEntity(
    @Id
    @Column(name = "payroll_id", nullable = false, length = 36)
    val id: String = UUID.randomUUID().toString(),

    @Column(name = "employee_id", nullable = false, length = 36)
    var employeeId: String,

    @Column(name = "period", nullable = false, length = 7)
    var period: String,  // YYYY-MM format

    @Column(name = "base_amount", nullable = false, precision = 15, scale = 2)
    var baseAmount: BigDecimal,

    @Column(name = "overtime_amount", nullable = false, precision = 15, scale = 2)
    var overtimeAmount: BigDecimal = BigDecimal.ZERO,

    @Column(name = "deductions", nullable = false, precision = 15, scale = 2)
    var deductions: BigDecimal = BigDecimal.ZERO,

    @Column(name = "total_amount", nullable = false, precision = 15, scale = 2)
    var totalAmount: BigDecimal,

    @Column(name = "calculated_at", nullable = false)
    var calculatedAt: Instant,

    @Column(name = "is_paid", nullable = false)
    var isPaid: Boolean = false,

    @Column(name = "paid_at")
    var paidAt: Instant? = null
) {
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant = Instant.now()
        protected set

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()
        protected set

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PayrollEntity) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String {
        return "PayrollEntity(id='$id', employeeId='$employeeId', period='$period')"
    }
}
