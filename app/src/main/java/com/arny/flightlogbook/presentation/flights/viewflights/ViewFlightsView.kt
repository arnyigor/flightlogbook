package com.arny.flightlogbook.presentation.flights.viewflights

import com.arny.domain.models.Flight
import moxy.MvpView
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType

/**
 *Created by Sedoy on 09.07.2019
 */
@StateStrategyType(value = OneExecutionStateStrategy::class)
interface ViewFlightsView : MvpView {
    fun updateAdapter(flights: List<Flight>)
    fun toastError(msg: String?)
    fun showEmptyView(vis: Boolean)
    fun viewLoadProgress(vis: Boolean)
}