package com.arny.flightlogbook.domain.common

interface IPreferencesInteractor {
    fun isAutoExportXLS(): Boolean
    fun setAutoExportXLS(checked: Boolean)
    fun isSaveLastData(): Boolean
    fun setSavedFlightTypeId(flightTypeId: Long?)
    fun getSavedFlightTypeId(): Long?
    fun setSavedAircraftId(aircraftId: Long?)
    fun getSavedAircraftId(): Long?
    fun getFlightsOrderType(): Int
    fun setOrderType(orderType: Int)
}
