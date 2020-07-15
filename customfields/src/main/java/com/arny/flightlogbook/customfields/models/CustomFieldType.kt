package com.arny.flightlogbook.customfields.models

import androidx.annotation.StringRes
import com.arny.flightlogbook.customfields.R

enum class CustomFieldType {
    TYPE_TEXT,
    TYPE_NUMBER_INT,
    TYPE_TIME_INT,
    TYPE_NUMBER_LONG,
    TYPE_NUMBER_DOUBLE,
    TYPE_BOOLEAN,
    TYPE_DATE,
    TYPE_NONE;

    @StringRes
    fun getTypeName(): Int {
        return when (this) {
            TYPE_TEXT -> R.string.field_text_name
            TYPE_NUMBER_INT -> R.string.field_num_name
            TYPE_BOOLEAN -> R.string.field_bool_name
            else -> R.string.field_text_no_name
        }
    }

    @StringRes
    fun getTypeDescr(): Int {
        return when (this) {
            TYPE_TEXT -> R.string.field_text_descr
            TYPE_NUMBER_INT -> R.string.field_num_descr
            TYPE_BOOLEAN -> R.string.field_bool_descr
            else -> R.string.field_text_no_descr
        }
    }
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