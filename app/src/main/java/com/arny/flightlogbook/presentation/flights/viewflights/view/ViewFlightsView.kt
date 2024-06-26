package com.arny.flightlogbook.presentation.flights.viewflights.view

import com.arny.flightlogbook.data.models.Flight
import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(value = OneExecutionStateStrategy::class)
interface ViewFlightsView : MvpView {
    fun updateAdapter(flights: List<Flight>, restoreScroll: Boolean)
    fun toastError(msg: String?)
    fun showEmptyView(vis: Boolean)
    fun showTotalsInfo(content: String?)

    @StateStrategyType(value = AddToEndSingleStrategy::class)
    fun viewLoadProgress(vis: Boolean)
    fun showError(message: String?)

    @StateStrategyType(value = AddToEndSingleStrategy::class)
    fun invalidateMenu(hasSelectedItems: Boolean, hasItems: Boolean)

    @StateStrategyType(value = OneExecutionStateStrategy::class)
    fun invalidateAdapter(position: Int)
}