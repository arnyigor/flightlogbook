package com.arny.flightlogbook.presentation.settings.presenter

import android.net.Uri
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
    }

    override fun detachView(view: SettingsView?) {
        super.detachView(view)
        resetCompositeDisposable()
    }

    fun onFileImport(uri: Uri?) {
        if (uri == null) {
            viewState.toastError(R.string.error_empty_file_path)
        } else {
            importFile(uri)
        }
    }

    private fun importFile(uri: Uri) {
        viewState.hideResults()
        viewState.showProgress(R.string.import_data)
        fromNullable {
            interactor.readExcelFile(uri, false)
        }.observeSubscribeAdd({
            viewState.hideProgress()
            val path = it.value
            if (path != null) {
                viewState.showResults(R.string.import_file_success, path)
            } else {
                viewState.toastError(R.string.error_import_file)
            }
        }, {
            viewState.toastError(R.string.error_import_file, it.message)
            viewState.hideProgress()
        })
    }

    fun exportToFile() {
        viewState.hideResults()
        viewState.showProgress(R.string.exporting_file)
        interactor.getDbFlightsObs()
                .map { interactor.saveExcelFile(it).toOptionalNull() }
                .observeSubscribeAdd({
                    viewState.hideProgress()
                    val path = it.value
                    if (path != null) {
                        viewState.showResults(R.string.export_file_success, path)
                    } else {
                        viewState.toastError(R.string.error_export_file, null)
                    }
                }, {
                    viewState.toastError(R.string.error_export_file, it.message)
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
        }.observeSubscribeAdd({
            viewState.hideProgress()
            val path = it.value
            if (path != null) {
                viewState.showResults(R.string.import_file_success, path)
            } else {
                viewState.toastError(R.string.error_import_file)
            }
        }, {
            viewState.toastError(R.string.error_import_file, it.message)
            viewState.hideProgress()
        })
    }
}
