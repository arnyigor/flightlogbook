package com.arny.flightlogbook.presentation.utils.strings

import android.content.Context
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class WrappedString constructor(val string: String?) : Parcelable, IWrappedString {
    companion object {
        val empty = WrappedString("")
    }

    override fun toString(context: Context): String? {
        return string
    }
}
