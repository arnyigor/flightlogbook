package com.arny.flightlogbook.data.models

import android.content.Context
import com.arny.flightlogbook.R
import java.util.Locale

data class PlaneType(
    var typeId: Long? = null,
    var typeName: String? = null,
    var mainType: AircraftType? = null,
    var regNo: String? = null
)

fun PlaneType?.getName(context: Context, default: String? = null): String {
    val hasName = !this?.typeName.isNullOrBlank()
    val hasRegNo = !this?.regNo.isNullOrBlank()
    val mainType = this?.mainType?.nameRes?.let { "${context.getString(it)}\u00A0" }.orEmpty()
    val regNo = context.getString(R.string.str_regnum_formatted, this?.regNo)
    return when {
        this != null && hasName && hasRegNo -> {
            String.format(
                Locale.getDefault(),
                "%s%s\u00A0%s",
                mainType,
                this.typeName,
                regNo,
            )
        }

        this != null && !hasName && hasRegNo -> {
            String.format(
                Locale.getDefault(),
                "%s%s",
                mainType,
                regNo,
            )
        }

        this != null -> mainType
        else -> default ?: context.getString(R.string.no_type)
    }
}