package com.arny.flightlogbook.customfields.repository

import com.arny.flightlogbook.customfields.models.CustomField
import com.arny.flightlogbook.customfields.models.CustomFieldType
import com.arny.flightlogbook.customfields.models.CustomFieldValue
import com.arny.flightlogbook.customfields.models.toCustomFieldType
import com.arny.flightlogbook.data.db.daos.CustomFieldDAO
import com.arny.flightlogbook.data.db.daos.CustomFieldValuesDAO
import com.arny.flightlogbook.data.models.CustomFieldEntity
import com.arny.flightlogbook.data.models.CustomFieldValueEntity
import com.arny.helpers.utils.OptionalNull
import com.arny.helpers.utils.fromSingle
import com.arny.helpers.utils.toOptionalNull
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class CustomFieldsRepository @Inject constructor(
        private val customFieldDAO: CustomFieldDAO,
        private val customFieldValuesDAO: CustomFieldValuesDAO
) : ICustomFieldsRepository {

    override fun addCustomField(customField: CustomField): Single<Boolean> {
        return fromSingle { customFieldDAO.insertReplace(customField.toDBValue()) != 0L }
                .subscribeOn(Schedulers.io())
    }

    override fun addCustomFields(vararg customField: CustomField): Single<Boolean> {
        return fromSingle {
            customFieldDAO.insertReplace(
                    customField.map { it.toDBValue() }).any { it != 0L }
        }
                .subscribeOn(Schedulers.io())
    }

    override fun removeCustomField(id: Long): Single<Boolean> {
        return fromSingle { customFieldDAO.delete(id) != 0 }
                .subscribeOn(Schedulers.io())
    }

    override fun addCustomFieldValue(customFieldValue: CustomFieldValue): Single<Boolean> {
        return fromSingle { customFieldValuesDAO.insertReplace(customFieldValue.toDbValue()) != 0L }
                .subscribeOn(Schedulers.io())
    }

    override fun getAllCustomField(id: Long): Single<OptionalNull<CustomField?>> {
        return fromSingle { customFieldDAO.getDbCustomField(id)?.let { toField(it) }.toOptionalNull() }
                .subscribeOn(Schedulers.io())
    }

    override fun getAllCustomFields(): Single<List<CustomField>> {
        return fromSingle { getAllFields() }
                .subscribeOn(Schedulers.io())
    }

    override fun getCustomFieldValues(externalId: Long): Single<List<CustomFieldValue>> {
        return fromSingle { getValues(externalId) }
                .subscribeOn(Schedulers.io())
    }

    private fun getAllFields(): List<CustomField> {
        return customFieldDAO.getDbCustomFields()
                .map(::toField)
    }

    private fun toField(it: CustomFieldEntity): CustomField {
        return CustomField(
                it.id ?: 0,
                it.name ?: "",
                it.type.toCustomFieldType()
        )
    }

    private fun getValues(externalId: Long): List<CustomFieldValue> {
        val allFields = getAllFields()
        return customFieldValuesDAO.getDbCustomFieldValues(externalId)
                .map { entity ->
                    val fieldId = entity.fieldId ?: 0
                    val field = allFields.find { it.id == fieldId }
                    val customFieldValue = CustomFieldValue(
                            entity.id ?: 0,
                            fieldId,
                            field,
                            externalId,
                            field?.type
                    )
                    setValueFromType(customFieldValue, entity)
                    customFieldValue
                }
    }

    private fun setValueFromType(field: CustomFieldValue, entity: CustomFieldValueEntity) {
        val value = entity.value.toString()
        when (field.type) {
            CustomFieldType.TYPE_BOOLEAN -> {
                field.value = value.toBoolean() || value == "1"
            }
            CustomFieldType.TYPE_DATE -> {
                field.value = value
            }
            CustomFieldType.TYPE_NUMBER_DOUBLE -> {
                field.value = value.toDoubleOrNull()
            }
            CustomFieldType.TYPE_NUMBER_INT -> {
                field.value = value.toIntOrNull()
            }
            CustomFieldType.TYPE_NUMBER_LONG -> {
                field.value = value.toLongOrNull()
            }
            else -> field.value = value
        }
    }
}
