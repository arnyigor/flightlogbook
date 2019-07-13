package com.arny.flightlogbook.presentation.flights.addedit

import com.arny.flightlogbook.data.db.intities.TimeToFlightEntity
import com.arny.flightlogbook.data.models.Flight
import com.arny.flightlogbook.data.models.PlaneType

/**
 *Created by Sedoy on 09.07.2019
 */
interface AddEditPresenter {
    fun initState(id: Long?)
    fun initEmptyUI()
    fun initUIFromId(id: Long?)
    fun setUIFromFlight(flight: Flight)
    fun setAircraftType(aircraftType: PlaneType?)
    fun correctLogTime(stringTime: String)
    fun onMotoTimeChange(startTime: String, finishTime: String)
    fun setMotoResult()
    fun addAircraftType(name: String)
    fun saveState(time: String, descr: String, regno: String)
    fun setExtractedDateTime(extractedValue: String)
    fun onDateSet(dayOfMonth: Int, monthOfYear: Int, year: Int)
    fun initDateFromMask(maskFilled: Boolean, extractedValue: String)
    fun loadPlaneTypes()
    fun onTimeItemAddToFlightTime(position: Int, item: TimeToFlightEntity)
    fun onTimeExcludeFromFlightTime(position: Int, item: TimeToFlightEntity)
    fun onAddTimeChange(position: Int, item: TimeToFlightEntity)
}