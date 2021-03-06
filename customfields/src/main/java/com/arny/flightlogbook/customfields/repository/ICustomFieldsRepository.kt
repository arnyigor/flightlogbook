package com.arny.flightlogbook.customfields.repository

import com.arny.core.utils.OptionalNull
import com.arny.flightlogbook.customfields.models.CustomField
import com.arny.flightlogbook.customfields.models.CustomFieldValue

interface ICustomFieldsRepository {
    fun getAllCustomFields(): List<CustomField>
    fun getAllAdditionalTime(): List<CustomFieldValue>
    fun getCustomFieldValues(externalId: Long): List<CustomFieldValue>
    fun addCustomField(customField: CustomField): Boolean
    fun addCustomFieldValue(customFieldValue: CustomFieldValue): Boolean
    fun getAllCustomField(id: Long): OptionalNull<CustomField?>
    fun getCustomFieldWithValues(externalId: Long?): List<CustomFieldValue>
    fun saveCustomFieldValues(values: List<CustomFieldValue>): Array<Long>
    fun removeCustomField(id: Long): Boolean
    fun removeCustomFields(idsToRemove: List<Long>): Boolean
}
