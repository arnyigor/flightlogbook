package com.arny.flightlogbook.utils

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager

class Prefs private constructor(val context: Context) {
    private var settings: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    companion object : SingletonHolder<Prefs, Context>(::Prefs)

    fun <T> get(key: String): T? {
        return settings.all[key] as? T
    }

    fun put(key: String?, value: Any?) {
        settings.edit().put(key, value).apply()
    }

    fun remove(vararg key: String) {
        val edit = settings.edit()
        for (k in key) {
            edit?.remove(k)
        }
        edit?.apply()
    }

    private fun SharedPreferences.Editor.put(key: String?, value: Any?): SharedPreferences.Editor {
        when (value) {
            is Int -> putInt(key, value)
            is Long -> putLong(key, value)
            is Float -> putFloat(key, value)
            is Double -> putFloat(key, value.toFloat())
            is String -> putString(key, value)
            is Boolean -> putBoolean(key, value)
        }
        return this
    }
}