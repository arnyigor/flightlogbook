package com.arny.core.strings

import android.content.Context

interface IWrappedString {
    fun toString(context: Context): String?
}