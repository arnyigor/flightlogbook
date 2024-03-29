package com.arny.flightlogbook.presentation.settings

import android.net.Uri
import androidx.annotation.StringRes
import moxy.MvpView
import moxy.viewstate.strategy.alias.AddToEndSingle
import moxy.viewstate.strategy.alias.OneExecution

@AddToEndSingle
interface BackupsView : MvpView {
    fun showProgress(msg: Int)
    fun hideProgress()

    @OneExecution
    fun showError(@StringRes msgRes: Int, error: String? = null)

    @OneExecution
    fun toastError(@StringRes msgRes: Int, error: String? = null)

    @OneExecution
    fun showSuccess(@StringRes msgRes: Int, msg: String? = null)
    fun showResults(intRes: Int, path: String)
    fun showResults(results: String)
    fun hideResults()
    fun setShareFileVisible(visible: Boolean)

    @OneExecution
    fun shareFile(uri: Uri, fileType: String)

    @OneExecution
    fun openWith(pair: Pair<Uri, String?>)
    fun showFileData()
    fun showFilesToShare(filenames: List<String>)
    @OneExecution
    fun showAlertChooseDefault(filenames: List<String>)
}