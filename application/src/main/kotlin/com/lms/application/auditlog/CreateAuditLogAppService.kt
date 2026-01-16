package com.lms.application.auditlog

import com.lms.application.auditlog.dto.AuditLogResult
import com.lms.application.auditlog.dto.CreateAuditLogCommand
import com.lms.domain.common.DomainContext
import com.lms.domain.model.auditlog.AuditLog
import com.lms.domain.model.auditlog.AuditLogRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 감사로그 생성 UseCase
 * 도메인 객체 변경 시 이력을 기록
 */
@Service
@Transactional
class CreateAuditLogAppService(private val auditLogRepository: AuditLogRepository) {

    fun execute(context: DomainContext, command: CreateAuditLogCommand): AuditLogResult {
        // 1. 감사로그 생성
        val auditLog = AuditLog.create(
            context = context,
            entityType = command.entityType,
            entityId = command.entityId,
            actionType = command.actionType,
            oldValue = command.oldValue,
            newValue = command.newValue,
            reason = command.reason
        )

        // 2. 저장
        val savedLog = auditLogRepository.save(auditLog)

        // 3. 응답 반환
        return AuditLogResult.from(savedLog)
    }
}
