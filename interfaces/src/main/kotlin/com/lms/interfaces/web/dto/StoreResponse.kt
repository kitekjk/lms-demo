package com.lms.interfaces.web.dto

import com.lms.domain.model.store.Store
import java.time.Instant

/**
 * 매장 응답 DTO
 */
data class StoreResponse(val id: String, val name: String, val location: String, val createdAt: Instant) {
    companion object {
        fun from(store: Store): StoreResponse = StoreResponse(
            id = store.id.value,
            name = store.name.value,
            location = store.location.value,
            createdAt = store.createdAt
        )
    }
}

/**
 * 매장 목록 응답 DTO
 */
data class StoreListResponse(val stores: List<StoreResponse>, val totalCount: Int) {
    companion object {
        fun from(stores: List<Store>): StoreListResponse = StoreListResponse(
            stores = stores.map { StoreResponse.from(it) },
            totalCount = stores.size
        )
    }
}
