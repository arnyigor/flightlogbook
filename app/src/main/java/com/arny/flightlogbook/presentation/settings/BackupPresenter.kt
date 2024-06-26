package com.arny.flightlogbook.presentation.settings

import android.net.Uri
import android.webkit.MimeTypeMap
import com.arny.core.utils.toOptionalNull
import com.arny.flightlogbook.R
import com.arny.flightlogbook.data.models.AppResult
import com.arny.flightlogbook.data.models.ExportFileType
import com.arny.flightlogbook.domain.files.FilesInteractor
import com.arny.flightlogbook.presentation.mvp.BaseMvpPresenter
import io.reactivex.Single
import moxy.InjectViewState
import java.io.File
import javax.inject.Inject

@InjectViewState
class BackupPresenter @Inject constructor(
    private val filesInteractor: FilesInteractor,
) : BaseMvpPresenter<BackupsView>() {
    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        initState()
    }

    private fun initState() {
        showFileData()
    }

    fun showFileData() {
        viewState.setShareFileVisible(false)
        Single.fromCallable { filesInteractor.getAllBackups().toOptionalNull() }
            .subscribeFromPresenter({
                val value = it.value
                viewState.setShareFileVisible(!value.isNullOrBlank())
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
        Single.fromCallable { filesInteractor.readFile(uri, false, null).toOptionalNull() }
            .subscribeFromPresenter({
                viewState.hideProgress()
                val path = it.value
                if (path != null) {
                    viewState.showSuccess(R.string.import_file_success, path)
                    viewState.showFileData()
                } else {
                    viewState.toastError(R.string.error_import_file)
                }
            }, {
                it.printStackTrace()
                viewState.hideProgress()
                viewState.toastError(R.string.error_import_file, it.message)
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
                    is AppResult.Success -> {
                        val path = it.data
                        if (path.isNotBlank()) {
                            viewState.showResults(R.string.export_file_success, path)
                            viewState.showFileData()
                        } else {
                            viewState.showError(R.string.error_export_file, null)
                        }
                    }

                    is AppResult.Error -> {
                        viewState.showError(R.string.error_export_file, it.exception?.message)
                    }
                }
            }, {
                viewState.showError(R.string.error_export_file, it.message)
                viewState.hideProgress()
            })
    }

    fun loadDefaultFile(fileName: String? = null) {
        viewState.hideResults()
        viewState.showProgress(R.string.import_data)
        Single.fromCallable { filesInteractor.readFile(null, true, fileName).toOptionalNull() }
            .subscribeFromPresenter({
                viewState.hideProgress()
                val path = it.value
                if (path != null) {
                    viewState.showSuccess(R.string.import_file_success, path)
                    viewState.showFileData()
                } else {
                    viewState.showError(R.string.error_import_file)
                }
            }, {
                it.printStackTrace()
                viewState.toastError(R.string.error_import_file, it.message)
                viewState.hideProgress()
            })
    }

    fun chooseDefaultFile() {
        Single.fromCallable { filesInteractor.getAllBackupFileNames() }
            .subscribeFromPresenter({ filenames ->
                if (filenames.isNotEmpty() && filenames.size > 1) {
                    viewState.showAlertChooseDefault(filenames)
                } else {
                    loadDefaultFile()
                }
            }, {
                loadDefaultFile()
            })
    }

    fun onShareFileClick() {
        Single.fromCallable { filesInteractor.getAllBackupFileNames() }
            .subscribeFromPresenter({ filenames ->
                viewState.showFilesToShare(filenames)
            }, {
                viewState.showError(R.string.error_share_file, it.message)
                viewState.setShareFileVisible(false)
            })
    }

    fun shareSelectedFile(selectedName: String) {
        Single.fromCallable { filesInteractor.getFileUri(selectedName).toOptionalNull() }
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

    fun openDefaultFileWith(path: String) {
        val fromFile = Uri.fromFile(File(path))
        val extension = MimeTypeMap.getFileExtensionFromUrl(fromFile.toString())
        val mimetype = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        println(mimetype)
    }
}
