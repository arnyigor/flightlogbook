package com.arny.flightlogbook.di.components

import com.arny.flightlogbook.FlightLogbookApp
import com.arny.flightlogbook.data.di.AirportsModule
import com.arny.flightlogbook.data.di.CustomFieldsModule
import com.arny.flightlogbook.data.di.DataModule
import com.arny.flightlogbook.data.di.FileReaderModule
import com.arny.flightlogbook.data.di.FlightsModule
import com.arny.flightlogbook.data.di.ResourceModule
import com.arny.flightlogbook.di.modules.ActivitiesModule
import com.arny.flightlogbook.di.modules.AppModule
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
        ActivitiesModule::class,
        DomainModule::class,
        ResourceModule::class,
        DataModule::class,
        FileReaderModule::class,
        CustomFieldsModule::class,
        AirportsModule::class,
        FlightsModule::class,
    ]
)
interface AppComponent : AndroidInjector<FlightLogbookApp> {
    override fun inject(application: FlightLogbookApp)

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application: FlightLogbookApp): Builder

        fun build(): AppComponent
    }
}