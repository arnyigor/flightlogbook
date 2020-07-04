package com.arny.flightlogbook.customfields.domain

import com.arny.flightlogbook.customfields.models.CustomField
import com.arny.flightlogbook.customfields.models.CustomFieldType
import com.arny.flightlogbook.customfields.models.CustomFieldValue
import io.reactivex.Single

interface ICustomFieldInteractor {
    fun getCustomFields(): Single<List<CustomField>>
    fun getCustomFieldValues(externalId: Long): Single<List<CustomFieldValue>>
    fun addCustomField(name: String, type: CustomFieldType): Single<Boolean>
    fun removeCustomField(id: Long? = null): Single<Boolean>
    fun addCustomFieldValue(id: Long?, fieldId: Long?, externalId: Long, type: CustomFieldType, value: Any? = null): Single<Boolean>
}
