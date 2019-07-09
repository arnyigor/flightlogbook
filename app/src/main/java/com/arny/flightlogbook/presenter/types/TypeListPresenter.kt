package com.arny.flightlogbook.presenter.types

import com.arny.flightlogbook.data.models.AircraftType

/**
 *Created by Sedoy on 09.07.2019
 */
interface TypeListPresenter {
    fun addType(name: String)
    fun confirmEditType(item: AircraftType)
    fun updateType(type: AircraftType)
    fun removeType(item: AircraftType)
    fun confirmDeleteType(item: AircraftType)
}