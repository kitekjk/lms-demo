package com.lms.application.store

import com.lms.application.store.dto.CreateStoreCommand
import com.lms.application.store.dto.StoreResult
import com.lms.domain.common.DomainContext
import com.lms.domain.exception.DuplicateStoreNameException
import com.lms.domain.model.store.Store
import com.lms.domain.model.store.StoreLocation
import com.lms.domain.model.store.StoreName
import com.lms.domain.model.store.StoreRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 매장 생성 UseCase
 */
@Service
@Transactional
class CreateStoreAppService(private val storeRepository: StoreRepository) {
    fun execute(context: DomainContext, command: CreateStoreCommand): StoreResult {
        val storeName = StoreName(command.name)

        // 중복 매장명 검증
        storeRepository.findByName(storeName)?.let {
            throw DuplicateStoreNameException(command.name)
        }

        // 매장 생성
        val store = Store.create(
            context = context,
            name = storeName,
            location = StoreLocation(command.location)
        )

        // 저장
        val savedStore = storeRepository.save(store)

        return StoreResult.from(savedStore)
    }
}
