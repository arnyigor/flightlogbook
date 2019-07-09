package com.arny.flightlogbook.di

import com.arny.flightlogbook.presenter.addedit.AddEditPresenterImpl
import com.arny.flightlogbook.presenter.types.TypeListPresenterImpl
import com.arny.flightlogbook.presenter.viewflights.ViewFlightsPresenterImpl
import dagger.Component
import javax.inject.Singleton

@Component(modules = [AppModule::class])
interface AppComponent {
    fun inject(typeListPresenter: TypeListPresenterImpl)
    fun inject(viewFlightsPresenter: ViewFlightsPresenterImpl)
    fun inject(addEditPresenter: AddEditPresenterImpl)
}