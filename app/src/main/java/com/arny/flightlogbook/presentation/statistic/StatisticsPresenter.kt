package com.arny.flightlogbook.presentation.statistic

import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.arny.domain.common.CommonUseCase
import com.arny.domain.flights.FlightsUseCase
import com.arny.flightlogbook.FlightApp
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

/**
 *Created by Sedoy on 21.08.2019
 */
@InjectViewState
class StatisticsPresenter : MvpPresenter<StatisticsView>() {
    private val compositeDisposable = CompositeDisposable()
    @Inject
    lateinit var flightsUseCase: FlightsUseCase
    @Inject
    lateinit var commonUseCase: CommonUseCase

    init {
        FlightApp.appComponent.inject(this)
    }

    override fun detachView(view: StatisticsView?) {
        super.detachView(view)
        compositeDisposable.clear()
    }

    fun loadData() {
        flightsUseCase.loadDBFlights()
    }
}