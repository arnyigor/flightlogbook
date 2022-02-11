package com.arny.flightlogbook.presentation.settings

import android.net.Uri
import android.webkit.MimeTypeMap
import com.arny.core.utils.fromNullable
import com.arny.domain.common.IPreferencesInteractor
import com.arny.domain.files.FilesInteractor
import com.arny.domain.flights.FlightsInteractor
import com.arny.domain.models.ExportFileType
import com.arny.domain.models.Result
import com.arny.flightlogbook.FlightApp
import com.arny.flightlogbook.R
import com.arny.flightlogbook.presentation.common.BaseMvpPresenter
import moxy.InjectViewState
import java.io.File
import javax.inject.Inject

@InjectViewState
class SettingsPresenter : BaseMvpPresenter<SettingsView>() {
    @Inject
    lateinit var filesInteractor: FilesInteractor

    @Inject
    lateinit var interactor: FlightsInteractor

    @Inject
    lateinit var prefsInteractor: IPreferencesInteractor

    init {
        FlightApp.appComponent.inject(this)
    }

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        initState()
    }

    private fun initState() {
        viewState.setAutoExportChecked(prefsInteractor.isAutoExportXLS())
        viewState.setSaveLastFlightData(prefsInteractor.isSaveLastData())
        showFileData()
    }

    fun showFileData() {
        viewState.setShareFileVisible(false)
        fromNullable { filesInteractor.getAllBackups() }
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
                viewState.resultSuccess()
            } else {
                viewState.showError(R.string.error_import_file)
            }
        }, {
            viewState.hideProgress()
            viewState.showError(R.string.error_import_file, it.message)
        })
    }

    fun exportToFile(type: ExportFileType = ExportFileType.XLS) {
        viewState.hideResults()
        viewState.showProgress(R.string.exporting_file)
        viewState.setShareFileVisible(false)
        filesInteractor.exportFile(type)
            .subscribeFromPresenter({
                viewState.hideProgress()
                when (it) {
                    is Result.Success -> {
                        val path = it.data
                        if (path.isNotBlank()) {
                            viewState.showResults(R.string.export_file_success, path)
                            viewState.resultSuccess()
                        } else {
                            viewState.showError(R.string.error_export_file, null)
                        }
                    }
                    is Result.Error -> {
                        viewState.showError(R.string.error_export_file, it.exception?.message)
                    }
                }
            }, {
                viewState.showError(R.string.error_export_file, it.message)
                viewState.hideProgress()
            })
    }

    fun onAutoExportChanged(checked: Boolean) {
        prefsInteractor.setAutoExportXLS(checked)
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
                viewState.resultSuccess()
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

    fun openDefaultFileWith() {
        val fromFile = Uri.fromFile(File(filesInteractor.getDefaultFilePath()))
        val extension = MimeTypeMap.getFileExtensionFromUrl(fromFile.toString())
        val mimetype = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        viewState.openWith(Pair(fromFile, mimetype))
    }

    fun onSaveLastDataChanged(checked: Boolean) {
        prefsInteractor.setSaveLastData(checked)
    }
}
