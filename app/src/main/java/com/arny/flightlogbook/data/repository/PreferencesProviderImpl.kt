package com.arny.flightlogbook.data.repository

import com.arny.flightlogbook.data.prefs.Prefs
import com.arny.flightlogbook.domain.common.PreferencesProvider
import javax.inject.Inject

class PreferencesProviderImpl @Inject constructor(private val prefs: Prefs) : PreferencesProvider {
    override fun getPrefString(key: String, default: String?): String? = prefs.get(key) ?: default

    override fun getPrefInt(key: String, default: Int): Int = prefs.get<Int>(key) ?: default

    override fun getPrefLong(key: String, default: Long): Long = prefs.get<Long>(key) ?: default

    override fun setPrefBoolean(key: String, value: Boolean) {
        prefs.put(key, value)
    }

    override fun setPref(key: String, value: Any) {
        prefs.put(key, value)
    }

    override fun setPrefInt(key: String, value: Int) {
        prefs.put(key, value)
    }

    override fun setPrefLong(key: String, value: Long) {
        prefs.put(key, value)
    }

    override fun getPrefBoolean(key: String, default: Boolean): Boolean =
        prefs.get<Boolean>(key) ?: false

    override fun removePref(vararg key: String) {
        prefs.remove(*key)
    }

    override fun setPrefString(key: String?, value: String?) {
        prefs.put(key, value)
    }
}