package com.arny.flightlogbook

import android.content.Context
import androidx.multidex.MultiDex
import com.arny.flightlogbook.di.DaggerAppComponent
import com.facebook.stetho.Stetho
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication

class FlightApp : DaggerApplication() {

    private val applicationInjector = DaggerAppComponent.builder()
        .application(this)
        .build()

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun onCreate() {
        super.onCreate()
        applicationInjector.inject(this)
        Stetho.initializeWithDefaults(this)
    }

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> = applicationInjector
}
