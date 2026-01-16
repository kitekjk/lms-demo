package com.lms.application.store

import com.lms.application.store.dto.StoreResult
import com.lms.application.store.dto.UpdateStoreCommand
import com.lms.domain.common.DomainContext
import com.lms.domain.exception.StoreNotFoundException
import com.lms.domain.model.store.StoreId
import com.lms.domain.model.store.StoreLocation
import com.lms.domain.model.store.StoreName
import com.lms.domain.model.store.StoreRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 매장 정보 수정 UseCase
 */
@Service
@Transactional
class UpdateStoreAppService(private val storeRepository: StoreRepository) {
    fun execute(context: DomainContext, storeId: String, command: UpdateStoreCommand): StoreResult {
        val store = storeRepository.findById(StoreId(storeId))
            ?: throw StoreNotFoundException(storeId)

        // 매장 정보 수정
        val updatedStore = store.update(
            context = context,
            name = StoreName(command.name),
            location = StoreLocation(command.location)
        )

        // 저장
        val savedStore = storeRepository.save(updatedStore)

        return StoreResult.from(savedStore)
    }
}
