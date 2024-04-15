package com.arny.flightlogbook.domain.customfields

import com.arny.core.utils.OptionalNull
import com.arny.flightlogbook.data.models.CustomField
import com.arny.flightlogbook.data.models.CustomFieldType
import com.arny.flightlogbook.data.models.CustomFieldValue
import javax.inject.Inject

class CustomFieldInteractor @Inject constructor(
    private val repository: ICustomFieldsRepository
) : ICustomFieldInteractor {
    override fun getCustomFields(): List<CustomField> = repository.getAllCustomFields()

    override fun getCustomFieldValues(externalId: Long): List<CustomFieldValue> =
        repository.getCustomFieldValues(externalId)

    override fun addCustomField(name: String, type: CustomFieldType): Boolean =
        repository.addCustomField(
            CustomField(
                name = name,
                type = type
            )
        )

    override fun removeField(id: Long): Boolean = repository.removeCustomField(id)

    override fun getCustomField(id: Long): OptionalNull<CustomField?> =
        repository.getAllCustomField(id)

    override fun save(
        id: Long?,
        name: String,
        type: CustomFieldType,
        showByDefault: Boolean,
        addTimeChecked: Boolean
    ): Boolean {
        val additionalTime = addTimeChecked && type is CustomFieldType.Time
        return if (id != null) {
            repository.updateCustomField(CustomField(id, name, type, showByDefault, additionalTime))
        } else {
            repository.addCustomField(CustomField(null, name, type, showByDefault, additionalTime))
        }
    }

    override fun getCustomFieldsWithValues(externalId: Long?): List<CustomFieldValue> =
        repository.getCustomFieldWithValues(externalId)

    override fun saveValues(values: List<CustomFieldValue>, flightId: Long?): Boolean {
        val list = flightId?.let { repository.getCustomFieldValues(flightId) }.orEmpty()
        val idsToRemove = getIdsToRemove(list, values)
        if (idsToRemove.isNotEmpty()) {
            repository.removeCustomFieldValues(idsToRemove.mapNotNull { it.id })
        }
        return repository.saveCustomFieldValues(values.filter { it.value != null })
            .all { it != 0L }
    }

    private fun getIdsToRemove(
        origin: List<CustomFieldValue>,
        newList: List<CustomFieldValue>
    ): List<CustomFieldValue> = origin.filter { listValue ->
        newList.find { it.id == listValue.id } == null
    }
}
