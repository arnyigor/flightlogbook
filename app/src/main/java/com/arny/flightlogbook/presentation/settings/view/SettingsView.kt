package com.arny.flightlogbook.presentation.settings.view

import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(value = AddToEndSingleStrategy::class)
interface SettingsView : MvpView {
    fun showProgress(msg: Int)
    fun hideProgress()
    fun toastError(msg: Int, error: String? = null)
    fun showResults(intRes: Int, path: String)
}