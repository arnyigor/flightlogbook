package com.arny.flightlogbook.presentation.settings

import moxy.MvpView
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType


@StateStrategyType(value = OneExecutionStateStrategy::class)
interface SettingsView : MvpView {
    fun showProgress(msg: String)
    fun hideProgress()
    fun toastError(msg: String?)
}