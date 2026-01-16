package com.lms.infrastructure.persistence.repository

import com.lms.domain.model.employee.EmployeeId
import com.lms.domain.model.payroll.Payroll
import com.lms.domain.model.payroll.PayrollId
import com.lms.domain.model.payroll.PayrollPeriod
import com.lms.domain.model.payroll.PayrollRepository
import com.lms.infrastructure.persistence.entity.PayrollEntity
import com.lms.infrastructure.persistence.mapper.PayrollMapper
import java.time.LocalDate
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface PayrollJpaRepository : JpaRepository<PayrollEntity, String> {
    fun findByEmployeeId(employeeId: String): List<PayrollEntity>
    fun findByIsPaid(isPaid: Boolean): List<PayrollEntity>
    fun findByEmployeeIdAndIsPaid(employeeId: String, isPaid: Boolean): List<PayrollEntity>

    @Query(
        "SELECT p FROM PayrollEntity p WHERE p.employeeId = :employeeId AND p.payPeriodStart >= :startDate AND p.payPeriodEnd <= :endDate"
    )
    fun findByEmployeeIdAndPayPeriod(
        @Param("employeeId") employeeId: String,
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate
    ): List<PayrollEntity>

    @Query("SELECT p FROM PayrollEntity p WHERE p.payPeriodStart >= :startDate AND p.payPeriodEnd <= :endDate")
    fun findByPayPeriod(
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate
    ): List<PayrollEntity>

    @Query(
        "SELECT SUM(p.totalPay) FROM PayrollEntity p WHERE p.employeeId = :employeeId AND p.payPeriodStart >= :startDate AND p.payPeriodEnd <= :endDate"
    )
    fun calculateTotalPayByEmployeeAndPeriod(
        @Param("employeeId") employeeId: String,
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate
    ): Double?
}

@Repository
@Transactional
class PayrollRepositoryImpl(private val jpaRepository: PayrollJpaRepository) : PayrollRepository {

    override fun save(payroll: Payroll): Payroll {
        val entity = PayrollMapper.toEntity(payroll)
        val saved = jpaRepository.save(entity)
        return PayrollMapper.toDomain(saved)
    }

    override fun findById(id: PayrollId): Payroll? = jpaRepository.findById(id.value)
        .map { PayrollMapper.toDomain(it) }
        .orElse(null)

    override fun findByEmployeeId(employeeId: EmployeeId): List<Payroll> =
        jpaRepository.findByEmployeeId(employeeId.value)
            .map { PayrollMapper.toDomain(it) }

    override fun findByEmployeeIdAndPeriod(employeeId: EmployeeId, period: PayrollPeriod): Payroll? {
        val yearMonth = period.toYearMonth()
        val startDate = yearMonth.atDay(1)
        val endDate = yearMonth.atEndOfMonth()

        return jpaRepository.findByEmployeeIdAndPayPeriod(
            employeeId.value,
            startDate,
            endDate
        ).firstOrNull()
            ?.let { PayrollMapper.toDomain(it) }
    }

    override fun findByPeriod(period: PayrollPeriod): List<Payroll> {
        val yearMonth = period.toYearMonth()
        val startDate = yearMonth.atDay(1)
        val endDate = yearMonth.atEndOfMonth()

        return jpaRepository.findByPayPeriod(
            startDate,
            endDate
        ).map { PayrollMapper.toDomain(it) }
    }

    override fun findUnpaidByEmployeeId(employeeId: EmployeeId): List<Payroll> =
        jpaRepository.findByEmployeeIdAndIsPaid(employeeId.value, false)
            .map { PayrollMapper.toDomain(it) }

    override fun delete(id: PayrollId) {
        jpaRepository.deleteById(id.value)
    }
}
