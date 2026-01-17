package com.lms.infrastructure.persistence.repository

import com.lms.domain.model.payroll.PayrollDetail
import com.lms.domain.model.payroll.PayrollDetailId
import com.lms.domain.model.payroll.PayrollDetailRepository
import com.lms.domain.model.payroll.PayrollId
import com.lms.infrastructure.persistence.entity.PayrollDetailEntity
import com.lms.infrastructure.persistence.mapper.PayrollDetailMapper
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface PayrollDetailJpaRepository : JpaRepository<PayrollDetailEntity, String> {
    fun findByPayrollId(payrollId: String): List<PayrollDetailEntity>
    fun deleteByPayrollId(payrollId: String)
}

@Repository
@Transactional
class PayrollDetailRepositoryImpl(private val jpaRepository: PayrollDetailJpaRepository) : PayrollDetailRepository {

    override fun save(payrollDetail: PayrollDetail): PayrollDetail {
        val entity = PayrollDetailMapper.toEntity(payrollDetail)
        val saved = jpaRepository.save(entity)
        return PayrollDetailMapper.toDomain(saved)
    }

    override fun saveAll(payrollDetails: List<PayrollDetail>): List<PayrollDetail> {
        val entities = payrollDetails.map { PayrollDetailMapper.toEntity(it) }
        val saved = jpaRepository.saveAll(entities)
        return saved.map { PayrollDetailMapper.toDomain(it) }
    }

    override fun findById(id: PayrollDetailId): PayrollDetail? = jpaRepository.findById(id.value)
        .map { PayrollDetailMapper.toDomain(it) }
        .orElse(null)

    override fun findByPayrollId(payrollId: PayrollId): List<PayrollDetail> =
        jpaRepository.findByPayrollId(payrollId.value)
            .map { PayrollDetailMapper.toDomain(it) }

    override fun deleteByPayrollId(payrollId: PayrollId) {
        jpaRepository.deleteByPayrollId(payrollId.value)
    }
}
