package com.lms.infrastructure.context

import com.lms.domain.common.DomainContext

/**
 * Audit 메타데이터를 저장하는 컨텍스트
 */
data class AuditContext(val domainContext: DomainContext, val reason: String?)
