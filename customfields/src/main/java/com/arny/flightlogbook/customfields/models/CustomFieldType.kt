package com.arny.flightlogbook.customfields.models

enum class CustomFieldType {
    TYPE_TEXT,
    TYPE_NUMBER_INT,
    TYPE_TIME_INT,
    TYPE_NUMBER_LONG,
    TYPE_NUMBER_DOUBLE,
    TYPE_BOOLEAN,
    TYPE_DATE,
    TYPE_NONE;
}

fun String?.toCustomFieldType(): CustomFieldType {
    return when (this) {
        "TYPE_TEXT" -> CustomFieldType.TYPE_TEXT
        "TYPE_NUMBER_INT" -> CustomFieldType.TYPE_NUMBER_INT
        "TYPE_TIME_INT" -> CustomFieldType.TYPE_TIME_INT
        "TYPE_NUMBER_LONG" -> CustomFieldType.TYPE_NUMBER_LONG
        "TYPE_NUMBER_DOUBLE" -> CustomFieldType.TYPE_NUMBER_DOUBLE
        "TYPE_BOOLEAN" -> CustomFieldType.TYPE_BOOLEAN
        "TYPE_DATE" -> CustomFieldType.TYPE_DATE
        else -> CustomFieldType.TYPE_NONE
    }
}