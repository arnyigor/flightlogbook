package com.arny.flightlogbook.di

import com.arny.flightlogbook.presentation.customfields.edit.presenter.CustomFieldsEditPresenter
import com.arny.flightlogbook.presentation.customfields.list.presenter.CustomFieldsListPresenter
import com.arny.flightlogbook.presentation.flights.addedit.presenter.AddEditPresenter
import com.arny.flightlogbook.presentation.flights.viewflights.presenter.ViewFlightsPresenter
import com.arny.flightlogbook.presentation.flighttypes.presenter.FlightTypesPresenter
import com.arny.flightlogbook.presentation.main.MainActivity
import com.arny.flightlogbook.presentation.planetypes.presenter.PlaneTypesPresenter
import com.arny.flightlogbook.presentation.settings.presenter.SettingsPresenter
import com.arny.flightlogbook.presentation.statistic.presenter.StatisticsPresenter
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class])
interface AppComponent {
    fun inject(mainActivity: MainActivity)
    fun inject(typeListPresenter: PlaneTypesPresenter)
    fun inject(viewFlightsPresenter: ViewFlightsPresenter)
    fun inject(addEditPresenter: AddEditPresenter)
    fun inject(customFieldsListPresenter: CustomFieldsListPresenter)
    fun inject(customFieldsEditPresenter: CustomFieldsEditPresenter)
    fun inject(flightTypesPresenter: FlightTypesPresenter)
    fun inject(statisticsPresenter: StatisticsPresenter)
    fun inject(settingsPresenter: SettingsPresenter)
}