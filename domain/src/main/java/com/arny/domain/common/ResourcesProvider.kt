package com.arny.domain.common

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

interface ResourcesProvider {
    fun provideContext(): Context
    fun getString(@StringRes res: Int): String
    fun getColor(@ColorRes id: Int): Int
    fun getDrawable(@DrawableRes id: Int): Drawable?
}