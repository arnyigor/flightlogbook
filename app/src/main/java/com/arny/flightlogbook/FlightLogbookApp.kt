package com.arny.flightlogbook

import com.arny.flightlogbook.di.components.DaggerAppComponent
import dagger.android.DaggerApplication
import timber.log.Timber

class FlightLogbookApp : DaggerApplication() {
    private val applicationInjector = DaggerAppComponent.builder()
        .application(this)
        .build()

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    override fun applicationInjector() = applicationInjector
}