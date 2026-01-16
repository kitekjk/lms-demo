package com.lms.infrastructure.persistence.auditlog.converter

import com.lms.domain.model.auditlog.EntityType
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter(autoApply = true)
class EntityTypeConverter : AttributeConverter<EntityType, String> {
    override fun convertToDatabaseColumn(attribute: EntityType?): String? = attribute?.value

    override fun convertToEntityAttribute(dbData: String?): EntityType? = dbData?.let { EntityType.from(it) }
}
