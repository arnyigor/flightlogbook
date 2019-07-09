package com.arny.flightlogbook.presenter.viewflights

import com.arny.flightlogbook.data.models.Flight

/**
 *Created by Sedoy on 09.07.2019
 */
interface ViewFlightsPresenter {
    fun loadFlights()
    fun removeAllFlights()
    fun removeItem(item: Flight?)
}