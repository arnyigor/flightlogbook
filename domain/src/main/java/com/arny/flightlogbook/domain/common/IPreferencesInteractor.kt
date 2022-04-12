package com.arny.flightlogbook.domain.common

interface IPreferencesInteractor {
    fun isAutoImportEnabled(): Boolean
    fun isAutoExportXLS(): Boolean
    fun setAutoExportXLS(checked: Boolean)
    fun isSaveLastData(): Boolean
    fun setSaveLastData(checked: Boolean)
    fun setSavedFlightTypeId(flightTypeId: Long?)
    fun getSavedFlightTypeId(): Long?
    fun setSavedAircraftId(aircraftId: Long?)
    fun getSavedAircraftId(): Long?
    fun getFlightsOrderType(): Int
    fun setOrderType(orderType: Int)
}
