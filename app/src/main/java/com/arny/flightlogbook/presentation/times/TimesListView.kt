package com.arny.flightlogbook.presentation.times

import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.viewstate.strategy.OneExecutionStateStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import com.arny.flightlogbook.data.db.intities.TimeToFlightEntity


@StateStrategyType(value = OneExecutionStateStrategy::class)
interface TimesListView : MvpView {
    fun setEmptyView(vis: Boolean)
    fun setListVisible(vis: Boolean)
    fun toastError(msg: String?)
    fun updateAdapter(timeTypes: List<TimeToFlightEntity>)
}