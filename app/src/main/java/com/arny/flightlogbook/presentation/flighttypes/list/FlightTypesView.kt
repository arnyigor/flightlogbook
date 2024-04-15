package com.arny.flightlogbook.presentation.flighttypes.list

import com.arny.flightlogbook.data.models.FlightType
import moxy.MvpView
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(value = OneExecutionStateStrategy::class)
interface FlightTypesView:MvpView {
    fun toastError(msg: String?)
    fun updateAdapter(list: List<FlightType>)
    fun showEmptyView(vis: Boolean)
}