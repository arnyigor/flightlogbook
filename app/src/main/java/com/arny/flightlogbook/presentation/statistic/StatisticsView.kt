package com.arny.flightlogbook.presentation.statistic

import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.viewstate.strategy.OneExecutionStateStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import com.arny.domain.models.Statistic
import java.util.*

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
}