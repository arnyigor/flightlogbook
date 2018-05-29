package com.arny.flightlogbook

import android.app.Application
import android.content.Context
import android.support.multidex.MultiDex
import com.arny.arnylib.database.DBProvider
import com.arny.flightlogbook.data.Consts
import com.arny.flightlogbook.di.components.ApplicationComponent
import com.arny.flightlogbook.di.components.DaggerApplicationComponent
import com.arny.flightlogbook.di.modules.AndroidModule
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.core.CrashlyticsCore
import com.facebook.stetho.Stetho
import io.fabric.sdk.android.Fabric

class FlightApp : Application() {
    companion object {
        //platformStatic allow access it from java code
        @JvmStatic lateinit var applicationComponent: ApplicationComponent
    }
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun onCreate() {
        super.onCreate()
        val crashlyticsKit = Crashlytics.Builder()
                .core(CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build())
                .build()
        // Инициализируем Fabric с выключенным crashlytics.
        Fabric.with(this, crashlyticsKit)
        DBProvider.initDB(applicationContext, Consts.DB.DB_NAME, Consts.DB.DB_VERSION)
        applicationComponent = DaggerApplicationComponent.builder().androidModule(AndroidModule(this)).build()
        applicationComponent.inject(this)
        Stetho.initializeWithDefaults(this)
    }
}
