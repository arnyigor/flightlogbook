package com.arny.flightlogbook.domain.common

interface PreferencesProvider {
    fun getPrefString(key: String, default: String? = null): String?
    fun getPrefInt(key: String, default: Int = 0): Int
    fun getPrefLong(key: String, default: Long = 0L): Long
    fun setPrefBoolean(key: String, value: Boolean)
    fun setPref(key: String, value: Any)
    fun setPrefInt(key: String, value: Int)
    fun setPrefLong(key: String, value: Long)
    fun getPrefBoolean(key: String, default: Boolean): Boolean
    fun removePref(vararg key: String)
    fun setPrefString(key: String?, value: String?)
}