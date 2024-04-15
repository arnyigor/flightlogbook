package com.arny.flightlogbook.presentation.utils.strings

import android.content.Context
import android.os.Parcelable
import androidx.annotation.StringRes
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import java.util.*

@Parcelize
class ParameterizedString(@StringRes val resource: Int, vararg val params:@RawValue Any?) : IWrappedString,
    Parcelable {
    override fun toString(context: Context): String {
        return if (params.isEmpty()) context.getString(resource)
        else context.getString(
            resource,
            *(params.map { if (it is IWrappedString) it.toString(context) else it }.toTypedArray())
        )
    }

    override fun describeContents() = 0

    override fun equals(other: Any?): Boolean {
        if (this === other)
            return true
        if (other == null || javaClass != other.javaClass)
            return false
        val another = other as ParameterizedString
        return (resource == another.resource && params.contentEquals(another.params))
    }

    override fun hashCode(): Int {
        return Objects.hash(resource, *params)
    }
}
