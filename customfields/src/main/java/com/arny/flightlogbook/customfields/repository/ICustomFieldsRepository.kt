package com.arny.flightlogbook.customfields.repository

import com.arny.flightlogbook.customfields.models.CustomField
import com.arny.flightlogbook.customfields.models.CustomFieldValue
import com.arny.helpers.utils.OptionalNull
import io.reactivex.Single

interface ICustomFieldsRepository {
    fun getAllCustomFields(): Single<List<CustomField>>
    fun getCustomFieldValues(externalId: Long): Single<List<CustomFieldValue>>
    fun addCustomField(customField: CustomField): Single<Boolean>
    fun addCustomFields(vararg customField: CustomField): Single<Boolean>
    fun removeCustomField(id: Long): Single<Boolean>
    fun addCustomFieldValue(customFieldValue: CustomFieldValue): Single<Boolean>
    fun getAllCustomField(id: Long): Single<OptionalNull<CustomField?>>
    fun getCustomFieldWithValues(externalId: Long?): Single<List<CustomFieldValue>>
    fun saveCustomFieldValues(values: List<CustomFieldValue>): Single<Array<Long>>
}
