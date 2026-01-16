package com.lms.application.store

import com.lms.application.store.dto.StoreResult
import com.lms.domain.model.store.StoreRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 전체 매장 목록 조회 UseCase
 */
@Service
@Transactional(readOnly = true)
class GetAllStoresAppService(private val storeRepository: StoreRepository) {
    fun execute(): List<StoreResult> {
        val stores = storeRepository.findAll()
        return stores.map { StoreResult.from(it) }
    }
}
