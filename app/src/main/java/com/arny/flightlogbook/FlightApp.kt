package com.arny.flightlogbook

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.multidex.MultiDex
import com.arny.flightlogbook.di.AppComponent
import com.arny.flightlogbook.di.AppModule
import com.arny.flightlogbook.di.DaggerAppComponent
import com.facebook.stetho.Stetho

class FlightApp : Application() {

    companion object {
        lateinit var appComponent: AppComponent
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        appComponent = DaggerAppComponent
                .builder()
                .appModule(AppModule(this))
                .build()
        appComponent.inject(this)
        Stetho.initializeWithDefaults(this)
    }
}
