package com.arny.flightlogbook.presentation.airports.list

import com.arny.domain.airports.IAirportsInteractor
import com.arny.flightlogbook.FlightApp
import com.arny.flightlogbook.presentation.common.BaseMvpPresenter
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import moxy.InjectViewState
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@InjectViewState
class AirportsPresenter : BaseMvpPresenter<AirportsView>() {

    init {
        FlightApp.appComponent.inject(this)
    }

    @Inject
    lateinit var airportsInteractor: IAirportsInteractor

    override fun onFirstViewAttach() {
    }

    fun onQueryChange(observable: Observable<String>) {
        observable.debounce(250, TimeUnit.MILLISECONDS)
                .distinctUntilChanged()
                .map { airportsInteractor.queryAirports(it) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    viewState.setAirports(it)
                }, {
                    viewState.showError(it.message)
                })
                .add()
    }

    fun onEditResultOk() {
        viewState.setAirports(emptyList())
    }
}
