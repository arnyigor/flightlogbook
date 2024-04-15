package com.arny.flightlogbook.data.di

import com.arny.flightlogbook.data.repository.AirportsRepository
import com.arny.flightlogbook.domain.airports.IAirportsRepository
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

@Module
interface AirportsModule {
    @Binds
    @Singleton
    fun bindAirportsRepository(repository: AirportsRepository): IAirportsRepository
}
