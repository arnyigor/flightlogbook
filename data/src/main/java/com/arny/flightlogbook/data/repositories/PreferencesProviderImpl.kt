package com.arny.flightlogbook.data.repositories

import com.arny.domain.common.PreferencesProvider
import com.arny.helpers.utils.Prefs
import javax.inject.Inject

class PreferencesProviderImpl @Inject constructor(private val prefs: Prefs) : PreferencesProvider {
    override fun getPrefString(key: String, default: String?): String? {
        return prefs.get(key) ?: default
    }

    override fun getPrefInt(key: String, default: Int): Int {
        return prefs.get<Int>(key) ?: default
    }

    override fun getPrefLong(key: String, default: Long): Long {
        return prefs.get<Long>(key) ?: default
    }

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

    override fun getPrefBoolean(key: String, default: Boolean): Boolean {
        return prefs.get<Boolean>(key) ?: false
    }

    override fun removePref(vararg key: String) {
        prefs.remove(*key)
    }

    override fun setPrefString(key: String?, value: String?) {
        prefs.put(key, value)
    }
}