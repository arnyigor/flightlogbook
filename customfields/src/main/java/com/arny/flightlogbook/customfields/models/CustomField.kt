package com.arny.flightlogbook.customfields.models

import java.io.Serializable

data class CustomField(
        val id: Long? = null,
        val name: String,
        val type: CustomFieldType,
        val showByDefault: Boolean = false,
        val addTime: Boolean = false,
        var values: List<CustomFieldValue>? = null
) : Serializable
