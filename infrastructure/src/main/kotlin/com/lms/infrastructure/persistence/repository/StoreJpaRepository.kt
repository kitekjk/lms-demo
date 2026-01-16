package com.lms.infrastructure.persistence.repository

import com.lms.infrastructure.persistence.entity.StoreEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

/**
 * Store JPA Repository
 */
@Repository
interface StoreJpaRepository : JpaRepository<StoreEntity, String> {
    /**
     * 매장명으로 조회
     */
    fun findByStoreName(storeName: String): StoreEntity?

    /**
     * 활성 상태인 매장 목록 조회
     */
    fun findByIsActive(isActive: Boolean): List<StoreEntity>

    /**
     * 위치로 매장 검색 (부분 일치)
     */
    @Query("SELECT s FROM StoreEntity s WHERE s.location LIKE %:location%")
    fun searchByLocation(@Param("location") location: String): List<StoreEntity>

    /**
     * 매장명으로 검색 (부분 일치)
     */
    @Query("SELECT s FROM StoreEntity s WHERE s.storeName LIKE %:storeName%")
    fun searchByStoreName(@Param("storeName") storeName: String): List<StoreEntity>
}
