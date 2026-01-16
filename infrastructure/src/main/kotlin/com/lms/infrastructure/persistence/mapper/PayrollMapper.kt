package com.lms.infrastructure.persistence.mapper

import com.lms.domain.model.employee.EmployeeId
import com.lms.domain.model.payroll.*
import com.lms.infrastructure.persistence.entity.PayrollEntity

/**
 * Payroll Domain ↔ Entity Mapper
 */
object PayrollMapper {
    /**
     * Entity → Domain 변환
     */
    fun toDomain(entity: PayrollEntity): Payroll {
        val amount = PayrollAmount(
            baseAmount = entity.baseAmount,
            overtimeAmount = entity.overtimeAmount,
            deductions = entity.deductions
        )

        return Payroll.reconstruct(
            id = PayrollId.from(entity.id),
            employeeId = EmployeeId.from(entity.employeeId),
            period = PayrollPeriod(entity.period),
            amount = amount,
            calculatedAt = entity.calculatedAt,
            isPaid = entity.isPaid,
            paidAt = entity.paidAt,
            createdAt = entity.createdAt
        )
    }

    /**
     * Domain → Entity 변환
     */
    fun toEntity(domain: Payroll): PayrollEntity {
        return PayrollEntity(
            id = domain.id.value,
            employeeId = domain.employeeId.value,
            period = domain.period.value,
            baseAmount = domain.amount.baseAmount,
            overtimeAmount = domain.amount.overtimeAmount,
            deductions = domain.amount.deductions,
            totalAmount = domain.amount.calculateTotal(),
            calculatedAt = domain.calculatedAt,
            isPaid = domain.isPaid,
            paidAt = domain.paidAt
        )
    }

    /**
     * Domain 변경사항을 Entity에 반영
     */
    fun updateEntity(entity: PayrollEntity, domain: Payroll) {
        entity.employeeId = domain.employeeId.value
        entity.period = domain.period.value
        entity.baseAmount = domain.amount.baseAmount
        entity.overtimeAmount = domain.amount.overtimeAmount
        entity.deductions = domain.amount.deductions
        entity.totalAmount = domain.amount.calculateTotal()
        entity.calculatedAt = domain.calculatedAt
        entity.isPaid = domain.isPaid
        entity.paidAt = domain.paidAt
    }
}
