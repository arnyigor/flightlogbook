package com.arny.flightlogbook.customfields.models

import com.arny.flightlogbook.data.models.CustomFieldValueEntity

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
            CustomFieldType.TYPE_TEXT -> value.toString()
            else -> value.toString()
        }
    }

    fun toDbValue() = CustomFieldValueEntity(id, fieldId, externalId, type.toString(), valueToString(value))
}
