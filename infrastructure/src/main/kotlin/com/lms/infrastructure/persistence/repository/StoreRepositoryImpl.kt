package com.lms.infrastructure.persistence.repository

import com.lms.domain.model.store.Store
import com.lms.domain.model.store.StoreId
import com.lms.domain.model.store.StoreName
import com.lms.domain.model.store.StoreRepository
import com.lms.infrastructure.persistence.entity.StoreEntity
import com.lms.infrastructure.persistence.mapper.StoreMapper
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

/**
 * Store JPA Repository
 */
@Repository
interface StoreJpaRepositoryInterface : JpaRepository<StoreEntity, String> {
    fun findByName(name: String): StoreEntity?
    fun findByIsActive(isActive: Boolean): List<StoreEntity>

    @Query("SELECT s FROM StoreEntity s WHERE s.location LIKE %:location%")
    fun searchByLocation(@Param("location") location: String): List<StoreEntity>

    @Query("SELECT s FROM StoreEntity s WHERE s.name LIKE %:name%")
    fun searchByName(@Param("name") name: String): List<StoreEntity>
}

/**
 * Store Repository 구현체
 * Domain의 StoreRepository 인터페이스를 구현
 */
@Repository
@Transactional
class StoreRepositoryImpl(private val jpaRepository: StoreJpaRepositoryInterface) : StoreRepository {

    override fun save(store: Store): Store {
        val entity = StoreMapper.toEntity(store)
        val saved = jpaRepository.save(entity)
        return StoreMapper.toDomain(saved)
    }

    override fun findById(storeId: StoreId): Store? = jpaRepository.findById(storeId.value)
        .map { StoreMapper.toDomain(it) }
        .orElse(null)

    override fun findByName(name: StoreName): Store? = jpaRepository.findByName(name.value)
        ?.let { StoreMapper.toDomain(it) }

    override fun findAll(): List<Store> = jpaRepository.findAll()
        .map { StoreMapper.toDomain(it) }

    override fun delete(storeId: StoreId) {
        jpaRepository.deleteById(storeId.value)
    }
}
