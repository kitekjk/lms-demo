package com.lms.infrastructure.persistence.attendance

import com.lms.domain.model.auditlog.ActionType
import com.lms.domain.model.auditlog.AuditLog
import com.lms.domain.model.auditlog.EntityType
import com.lms.infrastructure.context.AuditContextHolder
import com.lms.infrastructure.persistence.auditlog.AuditLogEntity
import com.lms.infrastructure.persistence.entity.AttendanceRecordEntity
import jakarta.persistence.PostLoad
import jakarta.persistence.PostUpdate
import jakarta.persistence.PreUpdate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * AttendanceRecord 엔티티 변경 이력 추적을 위한 EntityListener
 * @PreUpdate, @PostUpdate를 통해 변경 전후 값을 비교하여 AuditLog 생성
 */
@Component
class AttendanceRecordEntityListener {

    @Autowired
    private lateinit var auditLogJpaRepository: com.lms.infrastructure.persistence.auditlog.JpaAuditLogRepository

    /**
     * 엔티티 로드 후 현재 상태를 스냅샷으로 저장
     */
    @PostLoad
    fun postLoad(entity: AttendanceRecordEntity) {
        entity.originalCheckInTime = entity.checkInTime
        entity.originalCheckOutTime = entity.checkOutTime
        entity.originalNote = entity.note
    }

    /**
     * 엔티티 업데이트 전 변경 감지
     */
    @PreUpdate
    fun preUpdate(entity: AttendanceRecordEntity) {
        // 변경 사항이 있는지 확인
        val hasChanges = entity.checkInTime != entity.originalCheckInTime ||
            entity.checkOutTime != entity.originalCheckOutTime ||
            entity.note != entity.originalNote

        if (!hasChanges) {
            return
        }

        // AuditContext에서 메타데이터 가져오기
        val auditContext = AuditContextHolder.getContext()

        // 변경 전 값 JSON
        val oldValue = buildJson(
            entity.originalCheckInTime?.toString(),
            entity.originalCheckOutTime?.toString(),
            entity.originalNote
        )

        // 변경 후 값 JSON
        val newValue = buildJson(
            entity.checkInTime.toString(),
            entity.checkOutTime?.toString(),
            entity.note
        )

        // AuditLog 생성 (reason은 entity의 note 필드에서 가져옴)
        val auditLog = AuditLog.create(
            context = auditContext.domainContext,
            entityType = EntityType.AttendanceRecord,
            entityId = entity.id,
            actionType = ActionType.Update,
            oldValue = oldValue,
            newValue = newValue,
            reason = entity.note // 수정 사유는 note 필드에 저장됨
        )

        // AuditLog 저장
        val auditLogEntity = AuditLogEntity.from(auditLog)
        auditLogJpaRepository.save(auditLogEntity)
    }

    /**
     * 엔티티 업데이트 후 스냅샷 갱신
     */
    @PostUpdate
    fun postUpdate(entity: AttendanceRecordEntity) {
        entity.originalCheckInTime = entity.checkInTime
        entity.originalCheckOutTime = entity.checkOutTime
        entity.originalNote = entity.note
    }

    private fun buildJson(checkInTime: String?, checkOutTime: String?, note: String?): String {
        val parts = mutableListOf<String>()
        checkInTime?.let { parts.add("\"checkInTime\":\"$it\"") }
        checkOutTime?.let { parts.add("\"checkOutTime\":\"$it\"") }
        note?.let { parts.add("\"note\":\"${it.replace("\"", "\\\"")}\"") }
        return "{${parts.joinToString(",")}}"
    }
}
