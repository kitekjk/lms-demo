package com.lms.infrastructure.persistence.converter

import com.lms.domain.model.payroll.PolicyType
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

/**
 * PolicyType enum ↔ String Converter
 */
@Converter(autoApply = true)
class PolicyTypeConverter : AttributeConverter<PolicyType, String> {
    override fun convertToDatabaseColumn(attribute: PolicyType?): String? {
        return attribute?.name
    }

    override fun convertToEntityAttribute(dbData: String?): PolicyType? {
        return dbData?.let { PolicyType.valueOf(it) }
    }
}
