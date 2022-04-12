package com.arny.flightlogbook.presentation.statistic.view

import com.arny.flightlogbook.domain.models.Statistic
import moxy.MvpView
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType

/**
 *Created by Sedoy on 21.08.2019
 */
@StateStrategyType(value = OneExecutionStateStrategy::class)
interface StatisticsView : MvpView {
    fun setPeriodTypeVisible(vis: Boolean)
    fun setCustomPeriodVisible(vis: Boolean)
    fun setPeriodItemText(periodItem: String?)
    fun setStartDateText(date: String?)
    fun setEndDateText(date: String?)
    fun toastError(string: String?)
    fun showDateDialogStart(year: Int, month: Int, day: Int)
    fun showDateDialogEnd(year: Int, month: Int, day: Int)
    fun updateAdapter(stats: ArrayList<Statistic>)
    fun clearAdapter()
    fun setFilterStatisticVisible(vis: Boolean)
    fun showEmptyView(showEmpty: Boolean)
    fun setFilterSpinnerItems(items: List<String>)
    fun onColorSelect(colors: IntArray)
    fun setViewColorVisible(visible: Boolean)
    fun setFilterTypeVisible(visible: Boolean)
    fun setViewColor(color: Int)
}