package com.arny.domain.common

import com.arny.core.CONSTS
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesInteractor @Inject constructor(private val provider: PreferencesProvider) :
    IPreferencesInteractor {
    override fun isAutoImportEnabled(): Boolean {
        return provider.getPrefBoolean(CONSTS.PREFS.PREF_DROPBOX_AUTOIMPORT_TO_DB, false)
    }

    override fun getFlightsOrderType(): Int {
        return provider.getPrefInt(CONSTS.PREFS.PREF_USER_FILTER_FLIGHTS)
    }

    override fun setOrderType(orderType: Int) {
        provider.setPrefInt(CONSTS.PREFS.PREF_USER_FILTER_FLIGHTS, orderType)
    }

    override fun isAutoExportXLS(): Boolean {
        return provider.getPrefBoolean(CONSTS.PREFS.AUTO_EXPORT_XLS, false)
    }

    override fun setAutoExportXLS(checked: Boolean) {
        provider.setPref(CONSTS.PREFS.AUTO_EXPORT_XLS, checked)
    }

    override fun isSaveLastData(): Boolean {
        return provider.getPrefBoolean(CONSTS.PREFS.PREF_SAVE_LAST_FLIGHT_DATA, false)
    }

    override fun setSaveLastData(checked: Boolean) {
        provider.setPref(CONSTS.PREFS.PREF_SAVE_LAST_FLIGHT_DATA, checked)
    }

    override fun setSavedFlightTypeId(flightTypeId: Int?) {
        if (isSaveLastData()) {
            flightTypeId?.let {
                provider.setPref(
                    CONSTS.PREFS.PREF_LAST_FLIGHT_DATA_FLIGHT_TYPE_ID,
                    it
                )
            }
        }
    }

    override fun getSavedFlightTypeId(): Int? {
        if (isSaveLastData()) {
            with(provider.getPrefInt(CONSTS.PREFS.PREF_LAST_FLIGHT_DATA_FLIGHT_TYPE_ID)) {
                if (this == 0) return null
                return this
            }
        } else {
            return null
        }
    }

    override fun setSavedAircraftId(aircraftId: Long?) {
        if (isSaveLastData()) {
            aircraftId?.let { provider.setPref(CONSTS.PREFS.PREF_LAST_FLIGHT_DATA_AIRPLANE_ID, it) }
        }
    }

    override fun getSavedAircraftId(): Long? {
        if (isSaveLastData()) {
            with(provider.getPrefLong(CONSTS.PREFS.PREF_LAST_FLIGHT_DATA_AIRPLANE_ID)) {
                if (this == 0L) return null
                return this
            }
        } else {
            return null
        }
    }

    override fun getSavedExportPath(): String? =
        provider.getPrefString(CONSTS.PREFS.PREF_EXPORT_FILE_PATH, "")

    override fun setExportFilePath(dir: String?) {
        provider.setPrefString(CONSTS.PREFS.PREF_EXPORT_FILE_PATH, dir)
    }
}