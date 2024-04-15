package com.arny.flightlogbook.data.models

import androidx.annotation.StringRes
import com.arny.flightlogbook.R

sealed class CustomFieldType(
    @StringRes val nameRes: Int,
    @StringRes val descRes: Int
) {
    object Text : CustomFieldType(R.string.field_type_text, R.string.field_text_descr)
    object Number : CustomFieldType(R.string.field_type_number, R.string.field_num_descr)
    class Time(var addTime: Boolean = false) :
        CustomFieldType(R.string.field_type_time, R.string.field_time_descr)
    object Bool : CustomFieldType(R.string.field_type_boolean, R.string.field_bool_descr)
    object None : CustomFieldType(R.string.field_type_none, R.string.field_text_no_descr)

    companion object {
        fun values() = arrayOf(Text, Number, Time(), Bool)
    }

    override fun toString(): String = when (this) {
        is Text -> "TYPE_TEXT"
        is Number -> "TYPE_NUMBER"
        is Time -> "TYPE_TIME"
        is Bool -> "TYPE_BOOLEAN"
        is None -> "TYPE_NONE"
    }
}

fun String?.toCustomFieldType(): CustomFieldType = when (this) {
    "TYPE_TEXT" -> CustomFieldType.Text
    "TYPE_NUMBER" -> CustomFieldType.Number
    "TYPE_TIME" -> CustomFieldType.Time(false)
    "TYPE_BOOLEAN" -> CustomFieldType.Bool
    else -> CustomFieldType.None
}
