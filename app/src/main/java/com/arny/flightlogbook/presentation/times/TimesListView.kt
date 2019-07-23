package com.arny.flightlogbook.presentation.times

import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.viewstate.strategy.OneExecutionStateStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import com.arny.data.db.intities.TimeTypeEntity


@StateStrategyType(value = OneExecutionStateStrategy::class)
interface TimesListView : MvpView {
    fun setEmptyView(vis: Boolean)
    fun setListVisible(vis: Boolean)
    fun toastError(msg: String?)
    fun updateAdapter(timeTypes: List<TimeTypeEntity>)
    fun notifyItemChanged(position: Int)
    fun showDialogSetTime(item: TimeTypeEntity)
    fun confirmSelectedTimeFlight(id: Long?, title: String?, totalTime: Int, addToFlight: Boolean)
}