package com.arny.domain.common

import com.arny.constants.CONSTS
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesInteractor @Inject constructor(private val provider: PreferencesProvider) {
    fun isAutoImportEnabled(): Boolean {
        return provider.getPrefBoolean(CONSTS.PREFS.PREF_DROPBOX_AUTOIMPORT_TO_DB, false)
    }

    fun isShowMoto(): Boolean {
        return provider.getPrefBoolean(CONSTS.PREFS.PREF_MOTO_TIME, false)
    }
}