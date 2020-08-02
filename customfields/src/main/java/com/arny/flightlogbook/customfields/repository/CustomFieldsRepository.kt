package com.arny.flightlogbook.customfields.repository

import com.arny.flightlogbook.customfields.models.CustomField
import com.arny.flightlogbook.customfields.models.CustomFieldType
import com.arny.flightlogbook.customfields.models.CustomFieldValue
import com.arny.flightlogbook.customfields.models.toCustomFieldType
import com.arny.flightlogbook.data.db.daos.CustomFieldDAO
import com.arny.flightlogbook.data.db.daos.CustomFieldValuesDAO
import com.arny.flightlogbook.data.models.CustomFieldEntity
import com.arny.flightlogbook.data.models.CustomFieldValueEntity
import com.arny.flightlogbook.data.models.FieldWithValues
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

    override fun getCustomFieldWithValues(externalId: Long?): Single<List<CustomFieldValue>> {
        return fromSingle {
            toValuesList(
                    filterDefaultOrExternalId(
                            externalId,
                            customFieldDAO.getFieldsWithValues()
                    ),
                    externalId
            )
        }
                .subscribeOn(Schedulers.io())
    }

    private fun toValuesList(flightValues: List<FieldWithValues>, externalId: Long?): List<CustomFieldValue> {
        return flightValues.flatMap { flightValue ->
            val field = toField(flightValue.field!!)
            val values = flightValue.values
            val map = if (!values.isNullOrEmpty()) {
                values.map {
                    toValue(it, externalId, field, it.fieldId)
                }
            } else {
                listOf(CustomFieldValue(
                        null,
                        field.id,
                        field,
                        externalId,
                        field.type,
                        null
                ))
            }
            field.values = map
            field.values?.toList() ?: emptyList()
        }
    }

    private fun filterDefaultOrExternalId(externalId: Long?, list: List<FieldWithValues>): List<FieldWithValues> {
        return list.filter { field ->
            field.values?.filter { it.externalId == externalId }?.isNullOrEmpty() == false
                    || field.field?.showByDefault == true
        }
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
        val type = it.type.toCustomFieldType()
        if (type is CustomFieldType.Time) {
            type.addTime = it.addTime
        }
        return CustomField(
                it.id ?: 0,
                it.name ?: "",
                type,
                it.addTime,
                it.showByDefault
        )
    }

    private fun getValues(externalId: Long): List<CustomFieldValue> {
        val allFields = getAllFields()
        return customFieldValuesDAO.getDbCustomFieldValues(externalId)
                .map { entity ->
                    val fieldId = entity.fieldId
                    toValue(entity, externalId, allFields.find { it.id == fieldId }, fieldId)
                }
    }

    private fun toValue(
            entity: CustomFieldValueEntity,
            externalId: Long?,
            customField: CustomField?,
            fieldID: Long?
    ): CustomFieldValue {
        val customFieldValue = CustomFieldValue(
                entity.id ?: 0,
                fieldID,
                customField,
                externalId,
                customField?.type
        )
        setValueFromType(customFieldValue, entity)
        return customFieldValue
    }

    private fun setValueFromType(field: CustomFieldValue, entity: CustomFieldValueEntity) {
        val value = entity.value.toString()
        when (field.type) {
            is CustomFieldType.Text -> {
                field.value = value
            }
            is CustomFieldType.Number -> {
                field.value = value.toIntOrNull()
            }
            is CustomFieldType.Time -> {
                field.value = value.toIntOrNull()
            }
            is CustomFieldType.Bool -> {
                field.value = value.toBoolean() || value == "1"
            }
            else -> field.value = null
        }
    }
}
