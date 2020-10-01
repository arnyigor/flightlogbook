package com.arny.flightlogbook.presentation.airports

import com.arny.domain.airports.IAirportsInteractor
import com.arny.domain.models.Airport
import com.arny.flightlogbook.FlightApp
import com.arny.flightlogbook.presentation.common.BaseMvpPresenter
import com.arny.helpers.utils.fromCallable
import com.jakewharton.rxbinding4.InitialValueObservable
import com.jakewharton.rxbinding4.widget.TextViewAfterTextChangeEvent
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.ObservableSource
import io.reactivex.rxjava3.schedulers.Schedulers
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
        fromCallable { airportsInteractor.getAirports() }
                .doOnSubscribe { viewState.showProgress() }
                .doFinally { viewState.hideProgress() }
                .subscribeFromPresenter({
                    viewState.setAirports(it)
                }, {
                    viewState.showError(it.message)
                })
    }

    fun onQueryChange(valueObservable: InitialValueObservable<TextViewAfterTextChangeEvent>) {
        valueObservable.map { it.editable.toString() }
                .debounce(250, TimeUnit.MILLISECONDS)
                .distinctUntilChanged()
                .switchMap { q ->
                    ObservableSource<List<Airport>> { airportsInteractor.queryAirports(q) }
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ viewState.setAirports(it) }, {
                    viewState.showError(it.message)
                })
    }
}
