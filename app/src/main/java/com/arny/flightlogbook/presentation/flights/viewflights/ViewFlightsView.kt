package com.arny.flightlogbook.presentation.flights.viewflights

import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.viewstate.strategy.OneExecutionStateStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import com.arny.domain.models.Flight

/**
 *Created by Sedoy on 09.07.2019
 */
@StateStrategyType(value = OneExecutionStateStrategy::class)
interface ViewFlightsView : MvpView {
    fun updateAdapter(flights: List<Flight>)
    fun toastError(msg: String?)
}