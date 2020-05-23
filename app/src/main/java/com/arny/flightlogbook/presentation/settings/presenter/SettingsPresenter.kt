package com.arny.flightlogbook.presentation.settings.presenter

import android.net.Uri
import com.arny.domain.flights.FlightsInteractor
import com.arny.flightlogbook.FlightApp
import com.arny.flightlogbook.R
import com.arny.flightlogbook.presentation.settings.view.SettingsView
import com.arny.helpers.utils.CompositeDisposableComponent
import com.arny.helpers.utils.fromNullable
import io.reactivex.disposables.CompositeDisposable
import moxy.InjectViewState
import moxy.MvpPresenter
import javax.inject.Inject

@InjectViewState
class SettingsPresenter : MvpPresenter<SettingsView>(), CompositeDisposableComponent {
    override val compositeDisposable = CompositeDisposable()

    @Inject
    lateinit var interactor: FlightsInteractor

    init {
        FlightApp.appComponent.inject(this)
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
        fromNullable {
            interactor.saveExcelFile()
        }.observeSubscribeAdd({
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

}