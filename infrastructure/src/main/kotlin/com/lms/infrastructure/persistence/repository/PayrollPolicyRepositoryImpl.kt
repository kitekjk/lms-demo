package com.lms.infrastructure.persistence.repository

import com.lms.domain.model.payroll.PayrollPolicy
import com.lms.domain.model.payroll.PayrollPolicyId
import com.lms.domain.model.payroll.PayrollPolicyRepository
import com.lms.domain.model.payroll.PolicyType
import com.lms.infrastructure.persistence.entity.PayrollPolicyEntity
import com.lms.infrastructure.persistence.mapper.PayrollPolicyMapper
import java.time.LocalDate
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface PayrollPolicyJpaRepository : JpaRepository<PayrollPolicyEntity, String> {
    fun findByPolicyType(policyType: String): List<PayrollPolicyEntity>

    @Query(
        """
        SELECT pp FROM PayrollPolicyEntity pp
        WHERE pp.effectiveFrom <= :date
        AND (pp.effectiveTo IS NULL OR pp.effectiveTo >= :date)
    """
    )
    fun findEffectivePoliciesOn(@Param("date") date: LocalDate): List<PayrollPolicyEntity>

    @Query(
        "SELECT p FROM PayrollPolicyEntity p WHERE p.policyType = :policyType AND p.effectiveFrom <= :targetDate AND (p.effectiveTo IS NULL OR p.effectiveTo >= :targetDate)"
    )
    fun findEffectivePolicyByTypeAt(
        @Param("policyType") policyType: String,
        @Param("targetDate") targetDate: LocalDate
    ): PayrollPolicyEntity?
}

@Repository
@Transactional
class PayrollPolicyRepositoryImpl(private val jpaRepository: PayrollPolicyJpaRepository) : PayrollPolicyRepository {

    override fun save(payrollPolicy: PayrollPolicy): PayrollPolicy {
        val entity = PayrollPolicyMapper.toEntity(payrollPolicy)
        val saved = jpaRepository.save(entity)
        return PayrollPolicyMapper.toDomain(saved)
    }

    override fun findById(id: PayrollPolicyId): PayrollPolicy? = jpaRepository.findById(id.value)
        .map { PayrollPolicyMapper.toDomain(it) }
        .orElse(null)

    override fun findByPolicyType(policyType: PolicyType): List<PayrollPolicy> =
        jpaRepository.findByPolicyType(policyType.name)
            .map { PayrollPolicyMapper.toDomain(it) }

    override fun findEffectivePolicies(date: LocalDate): List<PayrollPolicy> =
        jpaRepository.findEffectivePoliciesOn(date)
            .map { PayrollPolicyMapper.toDomain(it) }

    override fun findCurrentlyEffectivePolicies(): List<PayrollPolicy> = findEffectivePolicies(LocalDate.now())

    override fun delete(id: PayrollPolicyId) {
        jpaRepository.deleteById(id.value)
    }
}
