package com.arny.flightlogbook.presentation.utils.strings

import android.content.Context

interface IWrappedString {
    fun toString(context: Context): String?
}