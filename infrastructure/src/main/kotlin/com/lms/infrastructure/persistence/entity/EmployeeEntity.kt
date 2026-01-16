package com.lms.infrastructure.persistence.entity

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.math.BigDecimal
import java.time.Instant
import java.util.*

/**
 * Employee JPA Entity
 * 근로자 정보 (Employee Aggregate에 대응)
 */
@Entity
@Table(
    name = "employees",
    indexes = [
        Index(name = "idx_employee_user", columnList = "user_id", unique = true),
        Index(name = "idx_employee_store", columnList = "store_id")
    ]
)
@EntityListeners(AuditingEntityListener::class)
class EmployeeEntity(

    @Id
    @Column(name = "employee_id", nullable = false, length = 36)
    val id: String = UUID.randomUUID().toString(),

    @Column(name = "user_id", nullable = false, length = 36)
    var userId: String,  // User 1:1 관계

    @Column(nullable = false, length = 100)
    var name: String,

    @Column(name = "employee_type", nullable = false, length = 20)
    var employeeType: String,  // EmployeeType enum의 name

    @Column(name = "store_id", length = 36)
    var storeId: String? = null,

    @Column(name = "remaining_leave", nullable = false, precision = 5, scale = 1)
    var remainingLeave: BigDecimal = BigDecimal.ZERO,

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true

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
        if (other !is EmployeeEntity) return false
        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "EmployeeEntity(id='$id', name='$name', userId='$userId')"
    }
}
