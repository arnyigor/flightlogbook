package com.arny.flightlogbook.presentation.statistic

import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.viewstate.strategy.OneExecutionStateStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType

/**
 *Created by Sedoy on 21.08.2019
 */
@StateStrategyType(value = OneExecutionStateStrategy::class)
interface StatisticsView : MvpView {
}