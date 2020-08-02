package com.arny.flightlogbook.customfields.domain

import com.arny.flightlogbook.customfields.models.CustomField
import com.arny.flightlogbook.customfields.models.CustomFieldType
import com.arny.flightlogbook.customfields.models.CustomFieldValue
import com.arny.flightlogbook.customfields.repository.ICustomFieldsRepository
import com.arny.helpers.utils.OptionalNull
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CustomFieldInteractor @Inject constructor(
        private val repository: ICustomFieldsRepository
) : ICustomFieldInteractor {
    override fun getCustomFields(): Single<List<CustomField>> {
        return repository.getAllCustomFields()
    }

    override fun getCustomFieldValues(externalId: Long): Single<List<CustomFieldValue>> {
        return repository.getCustomFieldValues(externalId)
    }

    override fun addCustomField(name: String, type: CustomFieldType): Single<Boolean> {
        return repository.addCustomField(CustomField(
                name = name,
                type = type
        ))
    }

    override fun removeCustomField(id: Long?): Single<Boolean> {
        return if (id != null) {
            repository.removeCustomField(id)
        } else Single.error(Throwable("id is null"))
    }

    override fun getCustomField(id: Long): Single<OptionalNull<CustomField?>> {
        return repository.getAllCustomField(id)
    }

    override fun save(id: Long?, name: String, type: CustomFieldType, showByDefault: Boolean): Single<Boolean> {
        return repository.addCustomField(CustomField(id, name, type, showByDefault))
    }

    override fun getCustomFieldsWithValues(externalId: Long?): Single<List<CustomFieldValue>> {
        return repository.getCustomFieldWithValues(externalId)
    }

    override fun addCustomFieldValue(
            id: Long?,
            fieldId: Long?,
            externalId: Long,
            type: CustomFieldType,
            value: Any?
    ): Single<Boolean> {
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
