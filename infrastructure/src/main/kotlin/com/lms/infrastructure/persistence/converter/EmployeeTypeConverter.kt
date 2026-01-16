package com.lms.infrastructure.persistence.converter

import com.lms.domain.model.employee.EmployeeType
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

/**
 * EmployeeType enum ↔ String Converter
 */
@Converter(autoApply = true)
class EmployeeTypeConverter : AttributeConverter<EmployeeType, String> {
    override fun convertToDatabaseColumn(attribute: EmployeeType?): String? = attribute?.name

    override fun convertToEntityAttribute(dbData: String?): EmployeeType? = dbData?.let { EmployeeType.valueOf(it) }
}
