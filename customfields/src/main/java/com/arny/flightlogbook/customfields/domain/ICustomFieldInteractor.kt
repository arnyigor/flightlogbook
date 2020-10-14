package com.arny.flightlogbook.customfields.domain

import com.arny.flightlogbook.customfields.models.CustomField
import com.arny.flightlogbook.customfields.models.CustomFieldType
import com.arny.flightlogbook.customfields.models.CustomFieldValue
import com.arny.helpers.utils.OptionalNull
import io.reactivex.Single

interface ICustomFieldInteractor {
    fun getCustomFields(): List<CustomField>
    fun getCustomFieldValues(externalId: Long): List<CustomFieldValue>
    fun addCustomField(name: String, type: CustomFieldType): Boolean
    fun removeCustomField(id: Long? = null): Single<Boolean>
    fun addCustomFieldValue(
            id: Long?,
            fieldId: Long?,
            externalId: Long,
            type: CustomFieldType,
            value: Any? = null
    ): Single<Boolean>

    fun getCustomField(id: Long): Single<OptionalNull<CustomField?>>
    fun save(id: Long?, name: String, type: CustomFieldType, showByDefault: Boolean, addTime: Boolean): Boolean
    fun getCustomFieldsWithValues(externalId: Long?): List<CustomFieldValue>
    fun saveValues(values: List<CustomFieldValue>): Single<Boolean>
}
