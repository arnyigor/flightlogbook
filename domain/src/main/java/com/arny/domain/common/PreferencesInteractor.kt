package com.arny.domain.common

import com.arny.flightlogbook.constants.CONSTS
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesInteractor @Inject constructor(private val provider: PreferencesProvider) {
    fun isAutoImportEnabled(): Boolean {
        return provider.getPrefBoolean(CONSTS.PREFS.PREF_DROPBOX_AUTOIMPORT_TO_DB, false)
    }

    fun isAutoExportXLS(): Boolean {
        return provider.getPrefBoolean(CONSTS.PREFS.AUTO_EXPORT_XLS, false)
    }

    fun setAutoExportXLS(checked: Boolean) {
        provider.setPref(CONSTS.PREFS.AUTO_EXPORT_XLS, checked)
    }

    fun isSaveLastData(): Boolean {
        return provider.getPrefBoolean(CONSTS.PREFS.PREF_SAVE_LAST_FLIGHT_DATA, false)
    }

    fun setSaveLastData(checked: Boolean) {
        provider.setPref(CONSTS.PREFS.PREF_SAVE_LAST_FLIGHT_DATA, checked)
    }

    fun setSavedFlightTypeId(flightTypeId: Int?) {
        if (isSaveLastData()) {
            flightTypeId?.let { provider.setPref(CONSTS.PREFS.PREF_LAST_FLIGHT_DATA_FLIGHT_TYPE_ID, it) }
        }
    }

    fun getSavedFlightTypeId(): Int? {
        if (isSaveLastData()) {
            with(provider.getPrefInt(CONSTS.PREFS.PREF_LAST_FLIGHT_DATA_FLIGHT_TYPE_ID)) {
                if (this == 0) return null
                return this
            }
        } else {
            return null
        }
    }

    fun setSavedAircraftId(aircraftId: Long?) {
        if (isSaveLastData()) {
            aircraftId?.let { provider.setPref(CONSTS.PREFS.PREF_LAST_FLIGHT_DATA_AIRPLANE_ID, it) }
        }
    }

    fun getSavedAircraftId(): Long? {
        if (isSaveLastData()) {
            with(provider.getPrefLong(CONSTS.PREFS.PREF_LAST_FLIGHT_DATA_AIRPLANE_ID)) {
                if (this == 0L) return null
                return this
            }
        } else {
            return null
        }
    }
}