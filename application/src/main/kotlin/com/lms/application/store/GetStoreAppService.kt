package com.lms.application.store

import com.lms.application.store.dto.StoreResult
import com.lms.domain.model.store.StoreId
import com.lms.domain.model.store.StoreRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 매장 단건 조회 UseCase
 */
@Service
@Transactional(readOnly = true)
class GetStoreAppService(private val storeRepository: StoreRepository) {
    fun execute(storeId: String): StoreResult? {
        val store = storeRepository.findById(StoreId(storeId))
        return store?.let { StoreResult.from(it) }
    }
}
