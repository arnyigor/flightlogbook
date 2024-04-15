package com.arny.flightlogbook.domain.customfields

import com.arny.core.utils.OptionalNull
import com.arny.flightlogbook.data.models.CustomField
import com.arny.flightlogbook.data.models.CustomFieldType
import com.arny.flightlogbook.data.models.CustomFieldValue

interface ICustomFieldInteractor {
    fun getCustomFields(): List<CustomField>
    fun getCustomFieldValues(externalId: Long): List<CustomFieldValue>
    fun addCustomField(name: String, type: CustomFieldType): Boolean

    fun getCustomField(id: Long): OptionalNull<CustomField?>
    fun save(
        id: Long?,
        name: String,
        type: CustomFieldType,
        showByDefault: Boolean,
        addTimeChecked: Boolean
    ): Boolean

    fun getCustomFieldsWithValues(externalId: Long?): List<CustomFieldValue>
    fun saveValues(values: List<CustomFieldValue>, flightId: Long?): Boolean
    fun removeField(id: Long): Boolean
}
