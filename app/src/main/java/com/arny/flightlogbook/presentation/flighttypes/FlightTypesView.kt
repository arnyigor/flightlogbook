package com.arny.flightlogbook.presentation.flighttypes

import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.viewstate.strategy.OneExecutionStateStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import com.arny.domain.models.FlightType

/**
 *Created by Sedoy on 01.08.2019
 */
@StateStrategyType(value = OneExecutionStateStrategy::class)
interface FlightTypesView:MvpView {
    fun toastError(msg: String?)
    fun updateAdapter(list: List<FlightType>)
    fun showEmptyView(vis: Boolean)
}