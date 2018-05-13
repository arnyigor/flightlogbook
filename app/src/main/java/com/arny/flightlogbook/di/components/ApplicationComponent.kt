package com.arny.flightlogbook.di.components

import android.content.Context
import com.arny.flightlogbook.FlightApp
import com.arny.flightlogbook.data.source.MainRepository
import com.arny.flightlogbook.di.modules.AndroidModule
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [(AndroidModule::class)])
interface ApplicationComponent {
    fun inject(target: FlightApp)
    fun inject(target: MainRepository)
    fun getContext(): Context
//    fun getDb(): MainDB
}