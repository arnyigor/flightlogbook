package com.arny.flightlogbook.data.source

import android.content.Context
import com.arny.arnylib.repository.BaseRepository
import com.arny.flightlogbook.FlightApp
import com.arny.flightlogbook.FlightApp.Companion.applicationComponent

class MainRepository : BaseRepository() {
    override fun getContext(): Context {
        return applicationComponent.getContext()
    }

    init {
        FlightApp.applicationComponent.inject(this)
    }

}
