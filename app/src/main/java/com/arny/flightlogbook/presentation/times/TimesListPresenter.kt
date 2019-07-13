package com.arny.flightlogbook.presentation.times

import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.arny.flightlogbook.FlightApp
import com.arny.flightlogbook.data.source.MainRepositoryImpl
import javax.inject.Inject


@InjectViewState
class TimesListPresenter : MvpPresenter<TimesListView>() {
    @Inject
    lateinit var repository: MainRepositoryImpl

    init {
        FlightApp.appComponent.inject(this)
    }
}