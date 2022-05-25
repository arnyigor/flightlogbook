package com.arny.flightlogbook.data.repositories

import com.arny.core.utils.DateTimeUtils
import com.arny.core.utils.OptionalNull
import com.arny.core.utils.toOptionalNull
import com.arny.flightlogbook.customfields.models.CustomField
import com.arny.flightlogbook.customfields.models.CustomFieldType
import com.arny.flightlogbook.customfields.models.CustomFieldValue
import com.arny.flightlogbook.customfields.models.toCustomFieldType
import com.arny.flightlogbook.customfields.repository.ICustomFieldsRepository
import com.arny.flightlogbook.data.db.daos.CustomFieldDAO
import com.arny.flightlogbook.data.db.daos.CustomFieldValuesDAO
import com.arny.flightlogbook.data.models.customfields.CustomFieldEntity
import com.arny.flightlogbook.data.models.customfields.CustomFieldValueEntity
import com.arny.flightlogbook.data.models.customfields.FieldWithValues
import javax.inject.Inject

class CustomFieldsRepository @Inject constructor(
    private val customFieldDAO: CustomFieldDAO,
    private val customFieldValuesDAO: CustomFieldValuesDAO
) : ICustomFieldsRepository {
    override fun addCustomField(customField: CustomField): Boolean =
        customFieldDAO.insertReplace(customField.toDBValue()) != 0L

    override fun updateCustomField(customField: CustomField): Boolean =
        customFieldDAO.updateReplace(customField.toDBValue()) != 0

    private fun CustomField.toDBValue() =
        CustomFieldEntity(id, name, type.toString(), showByDefault, addTime)

    override fun addCustomFieldValue(customFieldValue: CustomFieldValue): Boolean =
        customFieldValuesDAO.insertReplace(customFieldValue.toDbValue()) != 0L

    override fun getAllCustomField(id: Long): OptionalNull<CustomField?> =
        customFieldDAO.getDbCustomField(id)?.let { toField(it) }.toOptionalNull()

    override fun saveCustomFieldValues(values: List<CustomFieldValue>): Array<Long> =
        customFieldValuesDAO.insertReplace(values.map { it.toDbValue() })

    private fun valueToString(type: CustomFieldType?, value: Any?): String =
        if (type is CustomFieldType.Time) {
            DateTimeUtils.convertStringToTime(value.toString()).toString()
        } else {
            value.toString()
        }

    private fun CustomFieldValue.toDbValue() = CustomFieldValueEntity(
        id = id,
        fieldId = fieldId,
        externalId = externalId,
        type = type.toString(),
        value = valueToString(type, value)
    )

    override fun getCustomFieldWithValues(externalId: Long?): List<CustomFieldValue> =
        toValuesList(
            flightValues = filterDefaultOrExternalId(
                externalId = externalId,
                list = customFieldDAO.getFieldsWithValues()
            ),
            externalId = externalId
        )

    override fun removeCustomField(id: Long): Boolean = customFieldDAO.delete(id) != 0

    override fun removeCustomFieldValues(idsToRemove: List<Long>) =
        customFieldValuesDAO.delete(idsToRemove) > 0

    private fun toValuesList(
        flightValues: List<FieldWithValues>,
        externalId: Long?
    ): List<CustomFieldValue> =
        flightValues.flatMap { flightValue ->
            val field = toField(flightValue.field!!)
            val values = flightValue.values
            val map = if (!values.isNullOrEmpty() && externalId != null) {
                values.map { toValue(it, externalId, field, it.fieldId) }
            } else {
                listOf(
                    CustomFieldValue(
                        id = null,
                        fieldId = field.id,
                        field = field,
                        externalId = externalId,
                        type = field.type,
                        value = null
                    )
                )
            }
            map.toList()
        }

    private fun filterDefaultOrExternalId(
        externalId: Long?,
        list: List<FieldWithValues>
    ): List<FieldWithValues> = list.filter { field ->
        field.values?.none { it.externalId == externalId } == false
                || (field.field?.showByDefault == true && externalId == null)
    }

    override fun getAllCustomFields(): List<CustomField> = getAllFields()

    override fun getAllAdditionalTime(): List<CustomFieldValue> {
        val dbCustomFields = getAllFields()
        return customFieldValuesDAO.getDbCustomFieldValuesAddTime()
            .map { valueEntity ->
                toValue(
                    valueEntity,
                    valueEntity.externalId,
                    dbCustomFields.firstOrNull { it.id == valueEntity.fieldId },
                    valueEntity.id
                )
            }
    }

    override fun getCustomFieldValues(externalId: Long): List<CustomFieldValue> =
        getValues(externalId)

    private fun getAllFields(): List<CustomField> =
        customFieldDAO.getDbCustomFields().map(::toField)

    private fun toField(it: CustomFieldEntity): CustomField {
        val type = it.type.toCustomFieldType()
        if (type is CustomFieldType.Time) {
            type.addTime = it.addTime
        }
        return CustomField(
            id = it.id ?: 0,
            name = it.name.orEmpty(),
            type = type,
            showByDefault = it.showByDefault,
            addTime = type is CustomFieldType.Time && it.addTime
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
            id = entity.id ?: 0,
            fieldId = fieldID,
            field = customField,
            externalId = externalId,
            type = customField?.type
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
