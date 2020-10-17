package com.arny.flightlogbook.customfields.domain

import com.arny.flightlogbook.customfields.models.CustomField
import com.arny.flightlogbook.customfields.models.CustomFieldType
import com.arny.flightlogbook.customfields.models.CustomFieldValue
import com.arny.flightlogbook.customfields.repository.ICustomFieldsRepository
import com.arny.helpers.utils.OptionalNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CustomFieldInteractor @Inject constructor(
        private val repository: ICustomFieldsRepository
) : ICustomFieldInteractor {
    override fun getCustomFields(): List<CustomField> = repository.getAllCustomFields()

    override fun getCustomFieldValues(externalId: Long): List<CustomFieldValue> =
        repository.getCustomFieldValues(externalId)

    override fun addCustomField(name: String, type: CustomFieldType): Boolean {
        return repository.addCustomField(
                CustomField(
                        name = name,
                        type = type
                )
        )
    }

    override fun getCustomField(id: Long): OptionalNull<CustomField?> =
            repository.getAllCustomField(id)

    override fun save(
            id: Long?,
            name: String,
            type: CustomFieldType,
            showByDefault: Boolean,
            addTime: Boolean
    ): Boolean = repository.addCustomField(CustomField(id, name, type, showByDefault, addTime))

    override fun getCustomFieldsWithValues(externalId: Long?): List<CustomFieldValue> {
        return repository.getCustomFieldWithValues(externalId)
    }

    override fun saveValues(values: List<CustomFieldValue>): Boolean {
        return repository.saveCustomFieldValues(values.filter { it.value != null })
                .all { it != 0L }
    }

    override fun addCustomFieldValue(
            id: Long?,
            fieldId: Long?,
            externalId: Long,
            type: CustomFieldType,
            value: Any?
    ): Boolean {
        return repository.addCustomFieldValue(
                CustomFieldValue(
                        id = id,
                        fieldId = fieldId,
                        externalId = externalId,
                        type = type,
                        value = value
                )
        )
    }
}
