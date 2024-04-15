package com.arny.flightlogbook.domain.common

import android.content.ContentResolver
import android.content.Context
import android.graphics.drawable.Drawable
import androidx.annotation.ArrayRes
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

interface IResourceProvider {
    fun provideContext(): Context
    fun getContentResolver(): ContentResolver
    fun getString(@StringRes res: Int?): String
    fun getColor(@ColorRes id: Int): Int
    fun getDrawable(@DrawableRes id: Int): Drawable?
    fun getStringArray(@ArrayRes res: Int?): Array<String>
}