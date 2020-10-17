package com.arny.flightlogbook

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.multidex.MultiDex
import com.arny.flightlogbook.di.AppComponent
import com.arny.flightlogbook.di.AppModule
import com.arny.flightlogbook.di.DaggerAppComponent
import com.facebook.stetho.Stetho
import com.github.terrakok.cicerone.NavigatorHolder
import javax.inject.Inject

class FlightApp : Application() {
    @Inject
    lateinit var navigatorHolder: NavigatorHolder

    companion object {
        internal lateinit var INSTANCE: FlightApp
            private set
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
