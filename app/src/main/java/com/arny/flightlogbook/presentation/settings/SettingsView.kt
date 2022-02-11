package com.arny.flightlogbook.presentation.settings

import android.net.Uri
import com.arny.core.strings.IWrappedString
import moxy.MvpView
import moxy.viewstate.strategy.alias.AddToEndSingle
import moxy.viewstate.strategy.alias.OneExecution

@AddToEndSingle
interface SettingsView : MvpView {
    fun showProgress(msg: Int)
    fun hideProgress()

    @OneExecution
    fun showError(msg: Int, error: String? = null)
    fun showResults(intRes: Int, path: String)
    fun showResults(results: String)
    fun hideResults()
    fun setAutoExportChecked(checked: Boolean)
    fun setShareFileVisible(visible: Boolean)

    @OneExecution
    fun shareFile(uri: Uri, fileType: String)
    fun setSaveLastFlightData(checked: Boolean)

    @OneExecution
    fun openWith(pair: Pair<Uri, String?>)
    fun setSavedExportPath(path: IWrappedString)
}