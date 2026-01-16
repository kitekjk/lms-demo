package com.lms.interfaces.web.exception

/**
 * 엔티티를 찾을 수 없음 예외
 */
class EntityNotFoundException(entityName: String, identifier: Any) :
    BusinessException(
        message = "$entityName(을)를 찾을 수 없습니다: $identifier",
        errorCode = "ENTITY_NOT_FOUND"
    )
