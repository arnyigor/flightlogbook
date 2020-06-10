package com.arny.flightlogbook.presentation.settings.presenter

import android.net.Uri
import android.os.Handler
import com.arny.domain.common.PreferencesInteractor
import com.arny.domain.flights.FlightsInteractor
import com.arny.flightlogbook.FlightApp
import com.arny.flightlogbook.R
import com.arny.flightlogbook.presentation.settings.view.SettingsView
import com.arny.helpers.utils.CompositeDisposableComponent
import com.arny.helpers.utils.fromNullable
import com.arny.helpers.utils.toOptionalNull
import io.reactivex.disposables.CompositeDisposable
import moxy.InjectViewState
import moxy.MvpPresenter
import javax.inject.Inject

@InjectViewState
class SettingsPresenter : MvpPresenter<SettingsView>(), CompositeDisposableComponent {
    override val compositeDisposable = CompositeDisposable()

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
        fromNullable { interactor.getFileData() }
                .subsribeFromPresenter({
                    val value = it.value
                    viewState.setShareFileVisible(value != null)
                    if (value != null) {
                        viewState.showResults(value)
                    }
                })
    }

    override fun detachView(view: SettingsView?) {
        super.detachView(view)
        resetCompositeDisposable()
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
            interactor.readExcelFile(uri, false)
        }.subsribeFromPresenter({
            viewState.hideProgress()
            val path = it.value
            if (path != null) {
                viewState.showResults(R.string.import_file_success, path)
            } else {
                viewState.showError(R.string.error_import_file)
            }
        }, {
            viewState.showError(R.string.error_import_file, it.message)
            viewState.hideProgress()
        })
    }

    fun exportToFile() {
        viewState.hideResults()
        viewState.showProgress(R.string.exporting_file)
        viewState.setShareFileVisible(false)
        interactor.getDbFlightsObs()
                .map { interactor.saveExcelFile(it).toOptionalNull() }
                .subsribeFromPresenter({
                    viewState.hideProgress()
                    val path = it.value
                    if (path != null) {
                        viewState.showResults(R.string.export_file_success, path)
                        Handler().postDelayed({
                            showFileData()
                        }, 1500)
                    } else {
                        viewState.showError(R.string.error_export_file, null)
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
            interactor.readExcelFile(null, true)
        }.subsribeFromPresenter({
            viewState.hideProgress()
            val path = it.value
            if (path != null) {
                viewState.showResults(R.string.import_file_success, path)
            } else {
                viewState.showError(R.string.error_import_file)
            }
        }, {
            viewState.showError(R.string.error_import_file, it.message)
            viewState.hideProgress()
        })
    }

    fun onShareFileClick() {
        fromNullable { interactor.getDefaultFileUri() }
                .subsribeFromPresenter({
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
