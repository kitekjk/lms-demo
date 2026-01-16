package com.lms.infrastructure.persistence.converter

import com.lms.domain.model.leave.LeaveType
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

/**
 * LeaveType enum ↔ String Converter
 */
@Converter(autoApply = true)
class LeaveTypeConverter : AttributeConverter<LeaveType, String> {
    override fun convertToDatabaseColumn(attribute: LeaveType?): String? {
        return attribute?.name
    }

    override fun convertToEntityAttribute(dbData: String?): LeaveType? {
        return dbData?.let { LeaveType.valueOf(it) }
    }
}
