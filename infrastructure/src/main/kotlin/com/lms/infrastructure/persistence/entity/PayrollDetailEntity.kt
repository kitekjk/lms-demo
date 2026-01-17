package com.lms.infrastructure.persistence.entity

import com.lms.domain.model.payroll.WorkType
import com.lms.infrastructure.persistence.converter.WorkTypeConverter
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*
import org.springframework.data.jpa.domain.support.AuditingEntityListener

/**
 * 급여 상세 내역 JPA Entity
 * 일별 근무 유형별 급여 계산 내역
 */
@Entity
@Table(
    name = "payroll_details",
    indexes = [
        Index(name = "idx_payroll_detail_payroll", columnList = "payroll_id"),
        Index(name = "idx_payroll_detail_work_date", columnList = "work_date")
    ]
)
@EntityListeners(AuditingEntityListener::class)
class PayrollDetailEntity(
    @Id
    @Column(name = "detail_id", nullable = false, length = 36)
    val id: String = UUID.randomUUID().toString(),

    @Column(name = "payroll_id", nullable = false, length = 36)
    var payrollId: String,

    @Column(name = "work_date", nullable = false)
    var workDate: LocalDate,

    @Column(name = "work_type", nullable = false, length = 20)
    @Convert(converter = WorkTypeConverter::class)
    var workType: WorkType,

    @Column(name = "hours", nullable = false, precision = 5, scale = 2)
    var hours: BigDecimal,

    @Column(name = "hourly_rate", nullable = false, precision = 15, scale = 2)
    var hourlyRate: BigDecimal,

    @Column(name = "multiplier", nullable = false, precision = 3, scale = 2)
    var multiplier: BigDecimal,

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    var amount: BigDecimal
) : BaseEntity() {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PayrollDetailEntity) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String =
        "PayrollDetailEntity(id='$id', payrollId='$payrollId', workDate=$workDate, workType=$workType)"
}
