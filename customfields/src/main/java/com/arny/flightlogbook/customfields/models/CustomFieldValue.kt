package com.arny.flightlogbook.customfields.models

data class CustomFieldValue(
    val id: Long? = null,
    var fieldId: Long? = null,
    var field: CustomField? = null,
    var externalId: Long? = null,
    var value: Any? = null,
) {
    fun toStringJson(): String =
        "{\"externalId\":$externalId,\"field\":${field?.toStringJson()},\"fieldId\":$fieldId,\"id\":$id,\"value\":\"$value\"}"

    fun setValueByType(value: String) {
        when (field?.type) {
            is CustomFieldType.Text -> {
                this.value = value
            }
            is CustomFieldType.Number -> {
                this.value = value.toIntOrNull()
            }
            is CustomFieldType.Time -> {
                this.value = value.toIntOrNull()
            }
            is CustomFieldType.Bool -> {
                this.value = value.toBoolean() || value == "1"
            }
            else -> this.value = null
        }
    }
}


