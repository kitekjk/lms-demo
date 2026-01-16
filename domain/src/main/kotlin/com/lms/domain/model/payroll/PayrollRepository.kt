package com.lms.domain.model.payroll

import com.lms.domain.model.employee.EmployeeId

/**
 * Payroll Repository Interface
 * 구현체는 infrastructure 모듈에 위치
 */
interface PayrollRepository {
    fun save(payroll: Payroll): Payroll
    fun findById(id: PayrollId): Payroll?
    fun findByEmployeeId(employeeId: EmployeeId): List<Payroll>
    fun findByEmployeeIdAndPeriod(employeeId: EmployeeId, period: PayrollPeriod): Payroll?
    fun findByPeriod(period: PayrollPeriod): List<Payroll>
    fun findUnpaidByEmployeeId(employeeId: EmployeeId): List<Payroll>
    fun delete(id: PayrollId)
}
