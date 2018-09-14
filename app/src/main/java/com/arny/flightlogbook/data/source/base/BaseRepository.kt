package com.arny.flightlogbook.data.source.base

import android.content.Context
import android.support.annotation.ColorRes
import android.support.annotation.StringRes
import android.support.v4.content.ContextCompat
import com.arny.flightlogbook.utils.Prefs

interface BaseRepository {
    fun getContext(): Context
    fun getString(@StringRes resId: Int): String? {
        return getContext().getString(resId)
    }

    fun getPrefString(key: String, default: String? = null): String? {
        if (default != null) {
            return getContext().let { Prefs.getString(key, it, default) }
        }
        return getContext().let { Prefs.getString(key, it) }
    }

    fun getPrefFloat(key: String): Float? {
        return getContext().let { Prefs.getFloat(key, 0f, it) }
    }

    fun setPrefFloat(key: String, value: Float) {
        getContext().let { Prefs.setFloat(key, value, it) }
    }

    fun getPrefInt(key: String): Int? {
        return getContext().let { Prefs.getInt(key, it) }
    }

    fun setPrefString(key: String, value: String?) {
        getContext().let { Prefs.setString(key, value, it) }
    }

    fun setPrefInt(key: String, value: Int) {
        getContext().let { Prefs.setInt(key, value, it) }
    }

    fun setPrefBoolean(key: String, value: Boolean) {
        getContext().let { Prefs.setBoolean(key, value, it) }
    }

    fun getPrefBoolean(key: String, default: Boolean): Boolean {
        return getContext().let { Prefs.getBoolean(key, default, it) } ?: default
    }

    fun removePref(key: String) {
        return getContext().let { Prefs.remove(key, it) } ?: Unit
    }

    fun getColor(@ColorRes id: Int): Int {
        return ContextCompat.getColor(getContext(), id)
    }

}