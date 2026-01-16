package com.lms.infrastructure.persistence.converter

import com.lms.domain.model.leave.LeaveType
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

/**
 * LeaveType enum ↔ String Converter
 */
@Converter(autoApply = true)
class LeaveTypeConverter : AttributeConverter<LeaveType, String> {
    override fun convertToDatabaseColumn(attribute: LeaveType?): String? = attribute?.name

    override fun convertToEntityAttribute(dbData: String?): LeaveType? = dbData?.let { LeaveType.valueOf(it) }
}
