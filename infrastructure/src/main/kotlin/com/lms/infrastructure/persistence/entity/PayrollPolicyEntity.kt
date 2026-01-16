package com.lms.infrastructure.persistence.entity

import com.lms.domain.model.payroll.PolicyType
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.util.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener

/**
 * 급여 정책 JPA Entity
 */
@Entity
@Table(
    name = "payroll_policies",
    indexes = [
        Index(name = "idx_policy_type", columnList = "policy_type"),
        Index(name = "idx_policy_effective", columnList = "effective_from,effective_to"),
        Index(name = "idx_policy_type_effective", columnList = "policy_type,effective_from,effective_to")
    ],
    uniqueConstraints = [
        UniqueConstraint(name = "uk_policy_type_effective", columnNames = ["policy_type", "effective_from"])
    ]
)
@EntityListeners(AuditingEntityListener::class)
class PayrollPolicyEntity(
    @Id
    @Column(name = "payroll_policy_id", nullable = false, length = 36)
    val id: String = UUID.randomUUID().toString(),

    @Column(name = "policy_type", nullable = false, length = 30)
    var policyType: PolicyType,

    @Column(name = "multiplier", nullable = false, precision = 5, scale = 2)
    var multiplier: BigDecimal,

    @Column(name = "effective_from", nullable = false)
    var effectiveFrom: LocalDate,

    @Column(name = "effective_to")
    var effectiveTo: LocalDate? = null,

    @Column(name = "description", length = 500)
    var description: String? = null
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
        if (other !is PayrollPolicyEntity) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String = "PayrollPolicyEntity(id='$id', policyType='$policyType')"
}
