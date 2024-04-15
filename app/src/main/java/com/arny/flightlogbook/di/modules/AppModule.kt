package com.arny.flightlogbook.di.modules

import android.content.Context
import com.arny.flightlogbook.FlightLogbookApp
import dagger.Binds
import dagger.Module

@Module
internal abstract class AppModule {
    @Binds
    abstract fun provideContext(application: FlightLogbookApp): Context
}