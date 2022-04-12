package com.arny.flightlogbook.data.di

import com.arny.flightlogbook.data.repositories.AircraftTypesRepositoryImpl
import com.arny.flightlogbook.data.repositories.FlightTypesRepositoryImpl
import com.arny.flightlogbook.data.repositories.FlightsRepositoryImpl
import com.arny.flightlogbook.domain.flights.FlightsRepository
import com.arny.flightlogbook.domain.flighttypes.FlightTypesRepository
import com.arny.flightlogbook.domain.planetypes.AircraftTypesRepository
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

@Module
interface FlightsModule {
    @Binds
    @Singleton
    fun bindFlightsRepository(repositoryFlights: FlightsRepositoryImpl): FlightsRepository

    @Binds
    @Singleton
    fun bindFlightTypesRepository(repositoryTypes: FlightTypesRepositoryImpl): FlightTypesRepository

    @Binds
    @Singleton
    fun bindPlaneTypeRepository(repositoryPlaneTypes: AircraftTypesRepositoryImpl): AircraftTypesRepository
}
