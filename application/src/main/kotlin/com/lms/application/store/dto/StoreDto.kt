package com.lms.application.store.dto

import com.lms.domain.model.store.Store
import java.time.Instant

/**
 * 매장 생성 커맨드
 */
data class CreateStoreCommand(val name: String, val location: String)

/**
 * 매장 수정 커맨드
 */
data class UpdateStoreCommand(val name: String, val location: String)

/**
 * 매장 결과 DTO
 */
data class StoreResult(val id: String, val name: String, val location: String, val createdAt: Instant) {
    companion object {
        fun from(store: Store): StoreResult = StoreResult(
            id = store.id.value,
            name = store.name.value,
            location = store.location.value,
            createdAt = store.createdAt
        )
    }
}
