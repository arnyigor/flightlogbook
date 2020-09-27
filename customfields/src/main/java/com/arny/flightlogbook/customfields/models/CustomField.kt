package com.arny.flightlogbook.customfields.models

import com.arny.flightlogbook.data.models.customfields.CustomFieldEntity
import java.io.Serializable

data class CustomField(
        val id: Long? = null,
        val name: String,
        val type: CustomFieldType,
        val showByDefault: Boolean = false,
        val addTime: Boolean = false,
        var values: List<CustomFieldValue>? = null
) : Serializable {
    fun toDBValue() = CustomFieldEntity(id, name, type.toString(), showByDefault, addTime)
}
