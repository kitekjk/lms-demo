package com.lms.application.store

import com.lms.domain.exception.StoreNotFoundException
import com.lms.domain.model.store.StoreId
import com.lms.domain.model.store.StoreRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 매장 삭제 UseCase
 */
@Service
@Transactional
class DeleteStoreAppService(private val storeRepository: StoreRepository) {
    fun execute(storeId: String) {
        val id = StoreId(storeId)

        // 매장 존재 여부 확인
        storeRepository.findById(id)
            ?: throw StoreNotFoundException(storeId)

        // 삭제
        storeRepository.delete(id)
    }
}
