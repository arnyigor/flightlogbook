package com.arny.flightlogbook.domain.customfields

import com.arny.core.utils.OptionalNull
import com.arny.flightlogbook.data.models.CustomField
import com.arny.flightlogbook.data.models.CustomFieldValue

interface ICustomFieldsRepository {
    fun getAllCustomFields(): List<CustomField>
    fun getAllAdditionalTime(): List<CustomFieldValue>
    fun getCustomFieldValues(externalId: Long): List<CustomFieldValue>
    fun updateCustomField(customField: CustomField): Boolean
    fun addCustomField(customField: CustomField): Boolean
    fun addCustomFieldValue(customFieldValue: CustomFieldValue): Boolean
    fun getAllCustomField(id: Long): OptionalNull<CustomField?>
    fun getCustomFieldWithValues(externalId: Long?): List<CustomFieldValue>
    fun saveCustomFieldValues(values: List<CustomFieldValue>): Array<Long>
    fun removeCustomField(id: Long): Boolean
    fun removeCustomFieldValues(idsToRemove: List<Long>): Boolean
    fun removeCustomFieldValues(): Boolean
    fun removeCustomFields(): Boolean
    fun resetTableCustomFieldValues(): Boolean
    fun addCustomFieldAndGet(customField: CustomField): Long
    fun resetTableCustomFields(): Boolean
}
