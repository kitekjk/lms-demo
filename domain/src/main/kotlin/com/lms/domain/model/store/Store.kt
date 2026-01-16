package com.lms.domain.model.store

import com.lms.domain.common.DomainContext
import java.time.Instant

/**
 * Store Aggregate Root
 * 매장 정보 관리 (순수 Kotlin)
 */
data class Store private constructor(
    val id: StoreId,
    val name: StoreName,
    val location: StoreLocation,
    val createdAt: Instant
) {
    companion object {
        /**
         * 새로운 매장 생성
         */
        fun create(context: DomainContext, name: StoreName, location: StoreLocation): Store = Store(
            id = StoreId.generate(),
            name = name,
            location = location,
            createdAt = context.requestedAt
        )

        /**
         * 기존 매장 재구성 (Repository에서 조회 시)
         */
        fun reconstruct(id: StoreId, name: StoreName, location: StoreLocation, createdAt: Instant): Store =
            Store(id, name, location, createdAt)
    }

    /**
     * 매장 정보 수정
     */
    fun update(context: DomainContext, name: StoreName, location: StoreLocation): Store =
        this.copy(name = name, location = location)
}
