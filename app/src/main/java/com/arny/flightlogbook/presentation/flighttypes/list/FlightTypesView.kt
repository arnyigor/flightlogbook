package com.arny.flightlogbook.presentation.flighttypes.list

import com.arny.flightlogbook.domain.models.FlightType
import moxy.MvpView
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType

/**
 *Created by Sedoy on 01.08.2019
 */
@StateStrategyType(value = OneExecutionStateStrategy::class)
interface FlightTypesView:MvpView {
    fun toastError(msg: String?)
    fun updateAdapter(list: List<FlightType>)
    fun showEmptyView(vis: Boolean)
}