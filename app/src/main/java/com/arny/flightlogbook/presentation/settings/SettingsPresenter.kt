package com.arny.flightlogbook.presentation.settings

import android.util.Log
import com.arny.domain.flights.FlightsInteractor
import com.arny.flightlogbook.FlightApp
import com.arny.helpers.utils.CompositeDisposableComponent
import com.arny.helpers.utils.fromCallable
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

    fun onFileImport(path: String?) {
        viewState?.showProgress("Импорт данных...")
        Log.i(SettingsPresenter::class.java.simpleName, "onFileImport: $path,useCase:$interactor")
        fromCallable {
            interactor.readExcelFile(path, false){state, iter, total ->  }
        }.observeSubscribeAdd({
            viewState?.hideProgress()
            Log.i(SettingsPresenter::class.java.simpleName, "onFileImport result: $it");
        }, {
            viewState?.toastError("Ошибка импорта файла:${it.message}")
            viewState?.hideProgress()
            it.printStackTrace()
        })
    }

}