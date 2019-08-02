package com.arny.flightlogbook.presentation.timetypes

import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.viewstate.strategy.OneExecutionStateStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import com.arny.domain.models.TimeType


@StateStrategyType(value = OneExecutionStateStrategy::class)
interface TimesListView : MvpView {
    fun setEmptyView(vis: Boolean)
    fun setListVisible(vis: Boolean)
    fun toastError(msg: String?)
    fun updateAdapter(timeTypes: List<TimeType>)
    fun notifyItemChanged(position: Int)
    fun showDialogSetTime(item: TimeType)
    fun confirmSelectedTimeFlight(id: Long?, title: String?, totalTime: Int, addToFlight: Boolean)
}