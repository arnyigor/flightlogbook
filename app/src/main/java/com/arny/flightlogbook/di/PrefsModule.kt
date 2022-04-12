package com.arny.flightlogbook.di

import android.content.Context
import com.arny.core.utils.Prefs
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
interface PrefsModule {

    companion object {
        @Provides
        @Singleton
        fun providePrefs(context: Context): Prefs = Prefs.getInstance(context)
    }
}