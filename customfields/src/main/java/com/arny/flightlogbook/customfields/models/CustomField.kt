package com.arny.flightlogbook.customfields.models

import com.arny.flightlogbook.data.models.CustomFieldEntity

data class CustomField(
        val id: Long? = null,
        val name: String,
        val type: CustomFieldType,
        val showByDefault: Boolean = false,
        val addTime: Boolean = false,
        var values: List<CustomFieldValue>? = null
) {
    fun toDBValue() = CustomFieldEntity(id, name, type.toString(), showByDefault, addTime)
}
