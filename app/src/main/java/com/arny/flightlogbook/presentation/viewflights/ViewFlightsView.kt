package com.arny.flightlogbook.presentation.viewflights

import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.viewstate.strategy.OneExecutionStateStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import com.arny.flightlogbook.data.models.Flight

/**
 *Created by Sedoy on 09.07.2019
 */
@StateStrategyType(value = OneExecutionStateStrategy::class)
interface ViewFlightsView : MvpView {
    fun updateAdapter(flights: ArrayList<Flight>)
    fun displayTotalTime(time: String)
    fun toastError(msg: String?)
}