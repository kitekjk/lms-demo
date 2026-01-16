package com.lms.domain.model.store

/**
 * Store Repository 인터페이스 (도메인 계층에 정의)
 * 실제 구현은 infrastructure 계층에서 수행
 */
interface StoreRepository {
    fun save(store: Store): Store
    fun findById(storeId: StoreId): Store?
    fun findByName(name: StoreName): Store?
    fun findAll(): List<Store>
    fun delete(storeId: StoreId)
}
