package com.lms.infrastructure.persistence.converter

import com.lms.domain.model.user.Role
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

/**
 * Role enum ↔ String Converter
 */
@Converter(autoApply = true)
class RoleConverter : AttributeConverter<Role, String> {
    override fun convertToDatabaseColumn(attribute: Role?): String? {
        return attribute?.name
    }

    override fun convertToEntityAttribute(dbData: String?): Role? {
        return dbData?.let { Role.valueOf(it) }
    }
}
