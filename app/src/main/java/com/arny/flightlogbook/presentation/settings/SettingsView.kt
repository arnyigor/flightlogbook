package com.arny.flightlogbook.presentation.settings

import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.viewstate.strategy.OneExecutionStateStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType


@StateStrategyType(value = OneExecutionStateStrategy::class)
interface SettingsView : MvpView {
    fun showProgress(msg: String)
    fun hideProgress()
    fun toastError(msg: String?)
}