package com.arny.flightlogbook.di

import com.arny.flightlogbook.presentation.flights.addedit.AddEditPresenterImpl
import com.arny.flightlogbook.presentation.types.PlaneTypesPresenter
import com.arny.flightlogbook.presentation.flights.viewflights.ViewFlightsPresenterImpl
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class])
interface AppComponent {
    fun inject(typeListPresenter: PlaneTypesPresenter)
    fun inject(viewFlightsPresenter: ViewFlightsPresenterImpl)
    fun inject(addEditPresenter: AddEditPresenterImpl)
}