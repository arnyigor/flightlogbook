package com.arny.flightlogbook.data.di

import com.arny.domain.airports.IAirportsRepository
import com.arny.flightlogbook.data.repositories.AirportsRepository
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

@Module
interface AirportsModule {
    @Binds
    @Singleton
    fun bindAirportsRepository(repository: AirportsRepository): IAirportsRepository
}
