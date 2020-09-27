package com.arny.flightlogbook.customfields.models

import com.arny.flightlogbook.data.models.customfields.CustomFieldValueEntity
import com.arny.helpers.utils.DateTimeUtils

data class CustomFieldValue(
        val id: Long? = null,
        var fieldId: Long? = null,
        var field: CustomField? = null,
        var externalId: Long? = null,
        var type: CustomFieldType? = null,
        var value: Any? = null
) {

    private fun valueToString(value: Any?): String {
        return when (type) {
            is CustomFieldType.Time -> DateTimeUtils.convertStringToTime(value.toString()).toString()
            else -> value.toString()
        }
    }

    fun toDbValue() = CustomFieldValueEntity(
            id,
            fieldId,
            externalId,
            type.toString(),
            valueToString(value)
    )
}
