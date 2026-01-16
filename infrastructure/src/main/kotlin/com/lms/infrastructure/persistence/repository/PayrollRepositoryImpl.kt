package com.lms.infrastructure.persistence.repository

import com.lms.domain.model.employee.EmployeeId
import com.lms.domain.model.payroll.Payroll
import com.lms.domain.model.payroll.PayrollId
import com.lms.domain.model.payroll.PayrollPeriod
import com.lms.domain.model.payroll.PayrollRepository
import com.lms.infrastructure.persistence.entity.PayrollEntity
import com.lms.infrastructure.persistence.mapper.PayrollMapper
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface PayrollJpaRepository : JpaRepository<PayrollEntity, String> {
    fun findByEmployeeId(employeeId: String): List<PayrollEntity>
    fun findByEmployeeIdAndPeriod(employeeId: String, period: String): PayrollEntity?
    fun findByPeriod(period: String): List<PayrollEntity>
    fun findByEmployeeIdAndIsPaidFalse(employeeId: String): List<PayrollEntity>
}

@Repository
@Transactional
class PayrollRepositoryImpl(
    private val jpaRepository: PayrollJpaRepository
) : PayrollRepository {

    override fun save(payroll: Payroll): Payroll {
        val entity = PayrollMapper.toEntity(payroll)
        val saved = jpaRepository.save(entity)
        return PayrollMapper.toDomain(saved)
    }

    override fun findById(id: PayrollId): Payroll? {
        return jpaRepository.findById(id.value)
            .map { PayrollMapper.toDomain(it) }
            .orElse(null)
    }

    override fun findByEmployeeId(employeeId: EmployeeId): List<Payroll> {
        return jpaRepository.findByEmployeeId(employeeId.value)
            .map { PayrollMapper.toDomain(it) }
    }

    override fun findByEmployeeIdAndPeriod(employeeId: EmployeeId, period: PayrollPeriod): Payroll? {
        return jpaRepository.findByEmployeeIdAndPeriod(employeeId.value, period.value)
            ?.let { PayrollMapper.toDomain(it) }
    }

    override fun findByPeriod(period: PayrollPeriod): List<Payroll> {
        return jpaRepository.findByPeriod(period.value)
            .map { PayrollMapper.toDomain(it) }
    }

    override fun findUnpaidByEmployeeId(employeeId: EmployeeId): List<Payroll> {
        return jpaRepository.findByEmployeeIdAndIsPaidFalse(employeeId.value)
            .map { PayrollMapper.toDomain(it) }
    }

    override fun delete(id: PayrollId) {
        jpaRepository.deleteById(id.value)
    }
}
