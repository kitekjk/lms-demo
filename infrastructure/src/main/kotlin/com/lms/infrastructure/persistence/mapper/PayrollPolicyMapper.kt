package com.lms.infrastructure.persistence.mapper

import com.lms.domain.model.payroll.*
import com.lms.infrastructure.persistence.entity.PayrollPolicyEntity

/**
 * PayrollPolicy Domain ↔ Entity Mapper
 */
object PayrollPolicyMapper {
    /**
     * Entity → Domain 변환
     */
    fun toDomain(entity: PayrollPolicyEntity): PayrollPolicy {
        val effectivePeriod = PolicyEffectivePeriod(
            effectiveFrom = entity.effectiveFrom,
            effectiveTo = entity.effectiveTo
        )

        return PayrollPolicy.reconstruct(
            id = PayrollPolicyId.from(entity.id),
            policyType = PolicyType.valueOf(entity.policyType),
            multiplier = PolicyMultiplier.from(entity.multiplier),
            effectivePeriod = effectivePeriod,
            description = entity.description,
            createdAt = entity.createdAt
        )
    }

    /**
     * Domain → Entity 변환
     */
    fun toEntity(domain: PayrollPolicy): PayrollPolicyEntity {
        return PayrollPolicyEntity(
            id = domain.id.value,
            policyType = domain.policyType.name,
            multiplier = domain.multiplier.value,
            effectiveFrom = domain.effectivePeriod.effectiveFrom,
            effectiveTo = domain.effectivePeriod.effectiveTo,
            description = domain.description
        )
    }

    /**
     * Domain 변경사항을 Entity에 반영
     */
    fun updateEntity(entity: PayrollPolicyEntity, domain: PayrollPolicy) {
        entity.policyType = domain.policyType.name
        entity.multiplier = domain.multiplier.value
        entity.effectiveFrom = domain.effectivePeriod.effectiveFrom
        entity.effectiveTo = domain.effectivePeriod.effectiveTo
        entity.description = domain.description
    }
}
