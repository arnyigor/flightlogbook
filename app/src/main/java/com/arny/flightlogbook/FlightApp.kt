package com.arny.flightlogbook

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.multidex.MultiDex
import com.arny.flightlogbook.di.AppComponent
import com.arny.flightlogbook.di.AppModule
import com.arny.flightlogbook.di.DaggerAppComponent
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.core.CrashlyticsCore
import com.facebook.stetho.Stetho
import io.fabric.sdk.android.Fabric

class FlightApp : Application() {

    companion object {
        @JvmStatic
        lateinit var appContext: Context
        @JvmStatic
        lateinit var appComponent: AppComponent
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        appComponent = DaggerAppComponent
                .builder()
                .appModule(AppModule(this))
                .build()
        val crashlyticsKit = Crashlytics.Builder()
                .core(CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build())
                .build()
        Fabric.with(this, crashlyticsKit)
        Stetho.initializeWithDefaults(this)
    }
}
