package com.arny.flightlogbook.domain.common

import com.arny.core.CONSTS
import com.arny.flightlogbook.domain.R
import javax.inject.Inject

class PreferencesInteractor @Inject constructor(
    private val provider: PreferencesProvider,
    private val resourcesInteractor: ResourcesInteractor
) : IPreferencesInteractor {

    override fun getFlightsOrderType(): Int =
        provider.getPrefInt(CONSTS.PREFS.PREF_USER_FILTER_FLIGHTS)

    override fun setOrderType(orderType: Int) {
        provider.setPrefInt(CONSTS.PREFS.PREF_USER_FILTER_FLIGHTS, orderType)
    }

    override fun isAutoExportXLS(): Boolean =
        provider.getPrefBoolean(
            key = resourcesInteractor.getString(R.string.pref_auto_export_xls_key),
            default = false
        )

    override fun setAutoExportXLS(checked: Boolean) {
        provider.setPref(
            key = resourcesInteractor.getString(R.string.pref_auto_export_xls_key),
            value = checked
        )
    }

    override fun isSaveLastData(): Boolean =
        provider.getPrefBoolean(
            key = resourcesInteractor.getString(R.string.pref_save_last_flight_data_key),
            default = false
        )

    override fun setSavedFlightTypeId(flightTypeId: Long?) {
        if (isSaveLastData()) {
            flightTypeId?.let {
                provider.setPref(
                    CONSTS.PREFS.PREF_LAST_FLIGHT_DATA_FLIGHT_TYPE_ID,
                    it
                )
            }
        }
    }

    override fun getSavedFlightTypeId(): Long? {
        if (isSaveLastData()) {
            with(provider.getPrefLong(CONSTS.PREFS.PREF_LAST_FLIGHT_DATA_FLIGHT_TYPE_ID)) {
                if (this == 0L) return null
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
}