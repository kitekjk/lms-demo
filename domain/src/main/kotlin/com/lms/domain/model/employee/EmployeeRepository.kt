package com.lms.domain.model.employee

import com.lms.domain.model.store.StoreId
import com.lms.domain.model.user.UserId

/**
 * Employee Repository 인터페이스 (도메인 계층에 정의)
 * 실제 구현은 infrastructure 계층에서 수행
 */
interface EmployeeRepository {
    fun save(employee: Employee): Employee
    fun findById(employeeId: EmployeeId): Employee?
    fun findByUserId(userId: UserId): Employee?
    fun findByStoreId(storeId: StoreId): List<Employee>
    fun findActiveByStoreId(storeId: StoreId): List<Employee>
    fun findByStoreIdAndActive(storeId: StoreId, isActive: Boolean): List<Employee>
    fun findByActive(isActive: Boolean): List<Employee>
    fun findAll(): List<Employee>
    fun delete(employeeId: EmployeeId)
}
