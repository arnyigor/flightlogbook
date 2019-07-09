package com.arny.flightlogbook.di

import android.content.Context
import com.arny.flightlogbook.data.source.MainRepositoryImpl
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AppModule(private val context: Context) {
    @Provides
    @Singleton
    fun provideContext(): Context {
        return context
    }

}