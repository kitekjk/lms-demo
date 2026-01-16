package com.lms.domain.exception

/**
 * 매장을 찾을 수 없을 때 발생하는 예외
 */
class StoreNotFoundException(storeId: String, cause: Throwable? = null) :
    DomainException("STORE001", "매장을 찾을 수 없습니다: $storeId", cause)

/**
 * 중복된 매장명으로 등록 시도 시 발생하는 예외
 */
class DuplicateStoreNameException(name: String, cause: Throwable? = null) :
    DomainException("STORE002", "이미 존재하는 매장명입니다: $name", cause)
