package com.lms.infrastructure.persistence.converter

import com.lms.domain.model.attendance.AttendanceStatus
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

/**
 * AttendanceStatus enum ↔ String Converter
 */
@Converter(autoApply = true)
class AttendanceStatusConverter : AttributeConverter<AttendanceStatus, String> {
    override fun convertToDatabaseColumn(attribute: AttendanceStatus?): String? = attribute?.name

    override fun convertToEntityAttribute(dbData: String?): AttendanceStatus? = dbData?.let {
        AttendanceStatus.valueOf(it)
    }
}
