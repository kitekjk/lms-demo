package com.lms.infrastructure.persistence.converter

import com.lms.domain.model.payroll.WorkType
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

/**
 * WorkType Enum을 데이터베이스 문자열로 변환하는 Converter
 */
@Converter(autoApply = true)
class WorkTypeConverter : AttributeConverter<WorkType, String> {
    override fun convertToDatabaseColumn(attribute: WorkType?): String? = attribute?.name

    override fun convertToEntityAttribute(dbData: String?): WorkType? = dbData?.let { WorkType.valueOf(it) }
}
