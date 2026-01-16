package com.lms.infrastructure.persistence.mapper

import com.lms.domain.model.store.Store
import com.lms.domain.model.store.StoreId
import com.lms.domain.model.store.StoreLocation
import com.lms.domain.model.store.StoreName
import com.lms.infrastructure.persistence.entity.StoreEntity

/**
 * Store Domain ↔ Entity Mapper
 */
object StoreMapper {
    /**
     * Entity → Domain 변환
     */
    fun toDomain(entity: StoreEntity): Store {
        return Store.reconstruct(
            id = StoreId(entity.id),
            name = StoreName(entity.name),
            location = StoreLocation(entity.location),
            createdAt = entity.createdAt
        )
    }

    /**
     * Domain → Entity 변환
     * Note: createdAt은 JPA Auditing에 의해 자동 설정됨
     */
    fun toEntity(domain: Store): StoreEntity {
        return StoreEntity(
            id = domain.id.value,
            name = domain.name.value,
            location = domain.location.value
        )
    }

    /**
     * Domain 변경사항을 Entity에 반영
     */
    fun updateEntity(entity: StoreEntity, domain: Store) {
        entity.name = domain.name.value
        entity.location = domain.location.value
    }
}
