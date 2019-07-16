package com.arny.flightlogbook.di

import com.arny.flightlogbook.presentation.flights.addedit.AddEditPresenter
import com.arny.flightlogbook.presentation.flights.viewflights.ViewFlightsPresenter
import com.arny.flightlogbook.presentation.times.TimesListPresenter
import com.arny.flightlogbook.presentation.types.PlaneTypesPresenter
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class])
interface AppComponent {
    fun inject(typeListPresenter: PlaneTypesPresenter)
    fun inject(viewFlightsPresenter: ViewFlightsPresenter)
    fun inject(addEditPresenter: AddEditPresenter)
    fun inject(timesListPresenter: TimesListPresenter)
}