package com.arny.flightlogbook.di.modules

import android.app.Application
import android.content.Context
import android.support.annotation.NonNull
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AndroidModule(private val application: Application) {
    @Provides
    @Singleton
    @NonNull
    fun provideApplicationContext(): Context = application

//    @Provides
//    @Singleton
//    @NonNull
//    fun providesAppDatabase(context: Context): MainDB =
//            Room.databaseBuilder(context, MainDB::class.java, "PilotDB.db")
//                    .fallbackToDestructiveMigration()
//                    .build()

}