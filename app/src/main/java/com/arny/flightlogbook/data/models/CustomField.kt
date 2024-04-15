package com.arny.flightlogbook.data.models

data class CustomField(
    val id: Long? = null,
    val name: String,
    val type: CustomFieldType,
    val showByDefault: Boolean = false,
    val addTime: Boolean = false,
) {
      fun toStringJson(): String =
        "{\"addTime\":$addTime," +
                "\"id\":$id," +
                "\"name\":\"$name\"," +
                "\"showByDefault\":$showByDefault," +
                "\"type\":\"$type\"}"
}
