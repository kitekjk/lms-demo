package com.lms.domain.model.auditlog

import java.util.UUID

/**
 * 감사로그 ID
 */
@JvmInline
value class AuditLogId(val value: String) {
    companion object {
        fun generate(): AuditLogId = AuditLogId(UUID.randomUUID().toString())
    }
}
