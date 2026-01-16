package com.lms.infrastructure.persistence.auditlog.converter

import com.lms.domain.model.auditlog.ActionType
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter(autoApply = true)
class ActionTypeConverter : AttributeConverter<ActionType, String> {
    override fun convertToDatabaseColumn(attribute: ActionType?): String? = attribute?.value

    override fun convertToEntityAttribute(dbData: String?): ActionType? = dbData?.let { ActionType.from(it) }
}
