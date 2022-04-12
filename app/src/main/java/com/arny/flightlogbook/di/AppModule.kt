package com.arny.flightlogbook.di

import android.content.Context
import com.arny.flightlogbook.FlightApp
import dagger.Binds
import dagger.Module

@Module
internal abstract class AppModule {
    @Binds
    abstract fun provideContext(application: FlightApp): Context
}