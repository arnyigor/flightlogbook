package com.arny.flightlogbook.presentation.flights.viewflights

import moxy.MvpView
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType
import com.arny.domain.models.Flight

/**
 *Created by Sedoy on 09.07.2019
 */
@StateStrategyType(value = OneExecutionStateStrategy::class)
interface ViewFlightsView : MvpView {
    fun updateAdapter(flights: List<Flight>)
    fun toastError(msg: String?)
    fun clearAdaper()
    fun showEmptyView(vis: Boolean)
    fun showTotalsInfo(content: String?)
    fun viewLoadProgress(vis: Boolean)
}