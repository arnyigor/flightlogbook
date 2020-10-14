package com.arny.flightlogbook.customfields.models

data class CustomFieldValue(
        val id: Long? = null,
        var fieldId: Long? = null,
        var field: CustomField? = null,
        var externalId: Long? = null,
        var type: CustomFieldType? = null,
        var value: Any? = null
)
