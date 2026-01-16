package com.lms.infrastructure.persistence.converter

import com.lms.domain.model.leave.LeaveStatus
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

/**
 * LeaveStatus enum ↔ String Converter
 */
@Converter(autoApply = true)
class LeaveStatusConverter : AttributeConverter<LeaveStatus, String> {
    override fun convertToDatabaseColumn(attribute: LeaveStatus?): String? {
        return attribute?.name
    }

    override fun convertToEntityAttribute(dbData: String?): LeaveStatus? {
        return dbData?.let { LeaveStatus.valueOf(it) }
    }
}
