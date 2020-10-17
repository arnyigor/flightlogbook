package com.arny.flightlogbook.di

import android.app.Application
import android.content.Context
import com.arny.flightlogbook.data.di.DataModule
import com.arny.helpers.utils.Prefs
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module(
        includes = [
            ResourceModule::class,
            DataModule::class,
            NavigationModule::class
        ]
)
class AppModule(private val application: Application) {
    @Provides
    @Singleton
    fun provideContext(): Context = application

    @Provides
    @Singleton
    fun providePrefs(): Prefs {
        return Prefs.getInstance(application)
    }
}
