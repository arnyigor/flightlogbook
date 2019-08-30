package com.arny.flightlogbook.presentation.settings

import android.util.Log
import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.arny.data.repositories.MainRepositoryImpl
import com.arny.domain.flights.FlightsUseCase
import com.arny.flightlogbook.FlightApp
import com.arny.helpers.utils.CompositeDisposableComponent
import com.arny.helpers.utils.fromCallable
import com.arny.helpers.utils.runOnUI
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

@InjectViewState
class SettingsPresenter : MvpPresenter<SettingsView>(), CompositeDisposableComponent {
    override val compositeDisposable = CompositeDisposable()
    @Inject
    lateinit var useCase: FlightsUseCase
    @Inject
    lateinit var repository: MainRepositoryImpl

    init {
        FlightApp.appComponent.inject(this)
    }

    override fun detachView(view: SettingsView?) {
        super.detachView(view)
        resetCompositeDisposable()
    }

    fun onFileImport(path: String?) {
        viewState?.showProgress("Импорт данных...")
        Log.i(SettingsPresenter::class.java.simpleName, "onFileImport: $path,useCase:$useCase")
        fromCallable {
            useCase.readExcelFile(path, false) { state, perc, total ->
                runOnUI {
                    viewState?.showProgress("$state $perc%")
                }
            }
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