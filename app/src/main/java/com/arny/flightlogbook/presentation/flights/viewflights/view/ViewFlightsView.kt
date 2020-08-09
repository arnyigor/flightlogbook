package com.arny.flightlogbook.presentation.flights.viewflights.view

import com.arny.domain.models.Flight
import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(value = OneExecutionStateStrategy::class)
interface ViewFlightsView : MvpView {
    fun updateAdapter(flights: List<Flight>)
    fun toastError(msg: String?)
    fun clearAdaper()
    fun showEmptyView(vis: Boolean)
    fun showTotalsInfo(content: String?)

    @StateStrategyType(value = AddToEndSingleStrategy::class)
    fun viewLoadProgress(vis: Boolean)
    fun showError(message: String?)

    @StateStrategyType(value = AddToEndSingleStrategy::class)
    fun invalidateMenuSelected(hasSelectedItems: Boolean)

    @StateStrategyType(value = OneExecutionStateStrategy::class)
    fun invalidateAdapter(position: Int)
}