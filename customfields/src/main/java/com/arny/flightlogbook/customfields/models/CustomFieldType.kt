package com.arny.flightlogbook.customfields.models

import androidx.annotation.StringRes
import com.arny.flightlogbook.customfields.R

enum class CustomFieldType {
    TYPE_TEXT,
    TYPE_NUMBER,
    TYPE_TIME,
    TYPE_BOOLEAN,
    TYPE_NONE;

    @StringRes
    fun getTypeName(): Int {
        return when (this) {
            TYPE_TEXT -> R.string.field_type_text
            TYPE_NUMBER -> R.string.field_type_number
            TYPE_BOOLEAN -> R.string.field_type_boolean
            TYPE_TIME -> R.string.field_type_time
            else -> R.string.field_type_none
        }
    }

    @StringRes
    fun getTypeDescr(): Int {
        return when (this) {
            TYPE_TEXT -> R.string.field_text_descr
            TYPE_NUMBER -> R.string.field_num_descr
            TYPE_BOOLEAN -> R.string.field_bool_descr
            TYPE_TIME -> R.string.field_time_descr
            else -> R.string.field_text_no_descr
        }
    }
}

fun String?.toCustomFieldType(): CustomFieldType {
    return when (this) {
        "TYPE_TEXT" -> CustomFieldType.TYPE_TEXT
        "TYPE_NUMBER" -> CustomFieldType.TYPE_NUMBER
        "TYPE_TIME" -> CustomFieldType.TYPE_TIME
        "TYPE_BOOLEAN" -> CustomFieldType.TYPE_BOOLEAN
        else -> CustomFieldType.TYPE_NONE
    }
}