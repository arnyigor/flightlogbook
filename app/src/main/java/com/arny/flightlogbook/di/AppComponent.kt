package com.arny.flightlogbook.di

import com.arny.flightlogbook.FlightApp
import com.arny.flightlogbook.data.di.DataModule
import com.arny.flightlogbook.domain.di.DomainModule
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AndroidSupportInjectionModule::class,
        AppModule::class,
        PrefsModule::class,
        ResourceModule::class,
        DataModule::class,
        DomainModule::class,
    ]
)
interface AppComponent : AndroidInjector<FlightApp> {
    override fun inject(application: FlightApp)

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application: FlightApp): Builder

        fun build(): AppComponent
    }
}
