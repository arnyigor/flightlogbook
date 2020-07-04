package com.arny.flightlogbook.customfields.repository

import com.arny.flightlogbook.customfields.models.CustomField
import com.arny.flightlogbook.customfields.models.CustomFieldValue
import io.reactivex.Single

interface ICustomFieldsRepository {
    fun getAllCustomFields(): Single<List<CustomField>>
    fun getCustomFieldValues(externalId: Long): Single<List<CustomFieldValue>>
    fun addCustomField(customField: CustomField): Single<Boolean>
    fun addCustomFields(vararg customField: CustomField): Single<Boolean>
    fun removeCustomField(id: Long): Single<Boolean>
    fun addCustomFieldValue(customFieldValue: CustomFieldValue): Single<Boolean>
}
