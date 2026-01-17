package com.lms.domain.model.payroll

/**
 * PayrollDetail Repository Interface
 * 구현체는 infrastructure 모듈에 위치
 */
interface PayrollDetailRepository {
    fun save(payrollDetail: PayrollDetail): PayrollDetail

    fun saveAll(payrollDetails: List<PayrollDetail>): List<PayrollDetail>

    fun findById(id: PayrollDetailId): PayrollDetail?

    fun findByPayrollId(payrollId: PayrollId): List<PayrollDetail>

    fun deleteByPayrollId(payrollId: PayrollId)
}
