package com.lms.infrastructure.persistence.mapper

import com.lms.domain.model.payroll.PayrollDetail
import com.lms.domain.model.payroll.PayrollDetailId
import com.lms.domain.model.payroll.PayrollId
import com.lms.infrastructure.persistence.entity.PayrollDetailEntity

/**
 * PayrollDetail과 PayrollDetailEntity 간의 매핑을 담당하는 Mapper
 */
object PayrollDetailMapper {

    /**
     * Domain PayrollDetail을 Entity로 변환
     */
    fun toEntity(payrollDetail: PayrollDetail): PayrollDetailEntity = PayrollDetailEntity(
        id = payrollDetail.id.value,
        payrollId = payrollDetail.payrollId.value,
        workDate = payrollDetail.workDate,
        workType = payrollDetail.workType,
        hours = payrollDetail.hours,
        hourlyRate = payrollDetail.hourlyRate,
        multiplier = payrollDetail.multiplier,
        amount = payrollDetail.amount
    )

    /**
     * Entity를 Domain PayrollDetail로 변환
     */
    fun toDomain(entity: PayrollDetailEntity): PayrollDetail = PayrollDetail.reconstruct(
        id = PayrollDetailId(entity.id),
        payrollId = PayrollId(entity.payrollId),
        workDate = entity.workDate,
        workType = entity.workType,
        hours = entity.hours,
        hourlyRate = entity.hourlyRate,
        multiplier = entity.multiplier,
        amount = entity.amount
    )
}
