package com.lms.infrastructure.persistence.repository

import com.lms.infrastructure.persistence.entity.EmployeeEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

/**
 * Employee JPA Repository
 */
@Repository
interface EmployeeJpaRepository : JpaRepository<EmployeeEntity, String> {
    /**
     * userId로 Employee 조회
     */
    fun findByUserId(userId: String): EmployeeEntity?

    /**
     * 매장 ID로 Employee 목록 조회
     */
    fun findByStoreId(storeId: String): List<EmployeeEntity>

    /**
     * 활성 상태인 Employee 목록 조회
     */
    fun findByIsActive(isActive: Boolean): List<EmployeeEntity>

    /**
     * 매장 ID와 활성 상태로 Employee 목록 조회
     */
    fun findByStoreIdAndIsActive(storeId: String, isActive: Boolean): List<EmployeeEntity>

    /**
     * Employee 타입으로 조회
     */
    @Query("SELECT e FROM EmployeeEntity e WHERE e.employeeType = :employeeType")
    fun findByEmployeeType(@Param("employeeType") employeeType: String): List<EmployeeEntity>

    /**
     * 매장별 활성 직원 수 조회
     */
    @Query("SELECT COUNT(e) FROM EmployeeEntity e WHERE e.storeId = :storeId AND e.isActive = true")
    fun countActiveEmployeesByStore(@Param("storeId") storeId: String): Long

    /**
     * 이름으로 Employee 검색 (부분 일치)
     */
    @Query("SELECT e FROM EmployeeEntity e WHERE e.name LIKE %:name%")
    fun searchByName(@Param("name") name: String): List<EmployeeEntity>
}
