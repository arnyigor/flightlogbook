package com.arny.flightlogbook.di

import com.arny.flightlogbook.presentation.flights.addedit.AddEditPresenter
import com.arny.flightlogbook.presentation.flights.viewflights.ViewFlightsPresenter
import com.arny.flightlogbook.presentation.flighttypes.FlightTypesPresenter
import com.arny.flightlogbook.presentation.planetypes.PlaneTypesPresenter
import com.arny.flightlogbook.presentation.settings.SettingsPresenter
import com.arny.flightlogbook.presentation.statistic.StatisticsPresenter
import com.arny.flightlogbook.presentation.timetypes.TimesListPresenter
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class])
interface AppComponent {
    fun inject(typeListPresenter: PlaneTypesPresenter)
    fun inject(viewFlightsPresenter: ViewFlightsPresenter)
    fun inject(addEditPresenter: AddEditPresenter)
    fun inject(timesListPresenter: TimesListPresenter)
    fun inject(flightTypesPresenter: FlightTypesPresenter)
    fun inject(statisticsPresenter: StatisticsPresenter)
    fun inject(settingsPresenter: SettingsPresenter)
}