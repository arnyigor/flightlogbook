package com.arny.flightlogbook.presentation.settings.view

import android.net.Uri
import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(value = AddToEndSingleStrategy::class)
interface SettingsView : MvpView {
    fun showProgress(msg: Int)
    fun hideProgress()
    fun showError(msg: Int, error: String? = null)
    fun showResults(intRes: Int, path: String)
    fun showResults(results: String)
    fun hideResults()
    fun setAutoExportChecked(checked: Boolean)
    fun setShareFileVisible(visible: Boolean)
    fun shareFile(uri: Uri, fileType: String)
}