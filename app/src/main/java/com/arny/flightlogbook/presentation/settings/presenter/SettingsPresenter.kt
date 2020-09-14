package com.arny.flightlogbook.presentation.settings.presenter

import android.net.Uri
import android.os.Handler
import com.arny.domain.common.PreferencesInteractor
import com.arny.domain.files.FilesInteractor
import com.arny.domain.flights.FlightsInteractor
import com.arny.domain.models.Result
import com.arny.flightlogbook.FlightApp
import com.arny.flightlogbook.R
import com.arny.flightlogbook.presentation.common.BaseMvpPresenter
import com.arny.flightlogbook.presentation.settings.view.SettingsView
import com.arny.helpers.utils.fromNullable
import moxy.InjectViewState
import javax.inject.Inject

@InjectViewState
class SettingsPresenter : BaseMvpPresenter<SettingsView>() {
    private val handler = Handler()

    @Inject
    lateinit var filesInteractor: FilesInteractor

    @Inject
    lateinit var interactor: FlightsInteractor

    @Inject
    lateinit var prefs: PreferencesInteractor

    init {
        FlightApp.appComponent.inject(this)
    }

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        initState()
    }

    private fun initState() {
        viewState.setAutoExportChecked(prefs.isAutoExportXLS())
        showFileData()
    }

    private fun showFileData() {
        viewState.setShareFileVisible(false)
        fromNullable { filesInteractor.getFileData() }
                .subscribeFromPresenter({
                    val value = it.value
                    viewState.setShareFileVisible(value != null)
                    if (value != null) {
                        viewState.showResults(value)
                    }
                })
    }

    fun onFileImport(uri: Uri?) {
        if (uri == null) {
            viewState.showError(R.string.error_empty_file_path)
        } else {
            importFile(uri)
        }
    }

    private fun importFile(uri: Uri) {
        viewState.hideResults()
        viewState.showProgress(R.string.import_data)
        fromNullable {
            filesInteractor.readExcelFile(uri, false)
        }.subscribeFromPresenter({
            viewState.hideProgress()
            val path = it.value
            if (path != null) {
                viewState.showResults(R.string.import_file_success, path)
                handler.removeCallbacksAndMessages(null)
                handler.postDelayed({
                    showFileData()
                }, 1500)
            } else {
                viewState.showError(R.string.error_import_file)
            }
        }, {
            viewState.hideProgress()
            viewState.showError(R.string.error_import_file, it.message)
        })
    }

    fun exportToFile() {
        viewState.hideResults()
        viewState.showProgress(R.string.exporting_file)
        viewState.setShareFileVisible(false)
        filesInteractor.exportFile()
                .subscribeFromPresenter({
                    viewState.hideProgress()
                    when (it) {
                        is Result.Success -> {
                            val path = it.data
                            if (!path.isBlank()) {
                                viewState.showResults(R.string.export_file_success, path)
                                handler.removeCallbacksAndMessages(null)
                                handler.postDelayed({
                                    showFileData()
                                }, 1500)
                            } else {
                                viewState.showError(R.string.error_export_file, null)
                            }
                        }
                        is Result.Error-> {
                            viewState.showError(R.string.error_export_file, it.exception?.message)
                        }
                    }
                }, {
                    viewState.showError(R.string.error_export_file, it.message)
                    viewState.hideProgress()
                })
    }

    fun onAutoExportChanged(checked: Boolean) {
        prefs.setAutoExportXLS(checked)
    }

    fun loadDefaultFile() {
        viewState.hideResults()
        viewState.showProgress(R.string.import_data)
        fromNullable {
            filesInteractor.readExcelFile(null, true)
        }.subscribeFromPresenter({
            viewState.hideProgress()
            val path = it.value
            if (path != null) {
                viewState.showResults(R.string.import_file_success, path)
                handler.removeCallbacksAndMessages(null)
                handler.postDelayed({
                    showFileData()
                }, 1500)
            } else {
                viewState.showError(R.string.error_import_file)
            }
        }, {
            viewState.showError(R.string.error_import_file, it.message)
            viewState.hideProgress()
        })
    }

    fun onShareFileClick() {
        fromNullable { filesInteractor.getDefaultFileUri() }
                .subscribeFromPresenter({
                    val value = it.value
                    if (value != null) {
                        viewState.shareFile(value, "application/xls")
                    } else {
                        viewState.showError(R.string.error_share_file)
                        viewState.setShareFileVisible(false)
                    }
                }, {
                    viewState.showError(R.string.error_share_file, it.message)
                    viewState.setShareFileVisible(false)
                })
    }
}
