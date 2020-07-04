package com.arny.flightlogbook.customfields.models

import com.arny.flightlogbook.data.models.CustomFieldEntity

data class CustomField(
        val id: Long? = null,
        val name: String,
        val type: CustomFieldType
) {
    fun toDBValue() = CustomFieldEntity(id, name, type.toString())
}
