package com.arny.flightlogbook.data.di

import com.arny.domain.flights.FlightsRepository
import com.arny.domain.flighttypes.FlightTypesRepository
import com.arny.domain.planetypes.PlaneTypesRepository
import com.arny.flightlogbook.data.repositories.FlightTypesRepositoryImpl
import com.arny.flightlogbook.data.repositories.FlightsRepositoryImpl
import com.arny.flightlogbook.data.repositories.PlaneTypesRepositoryImpl
import dagger.Binds
import dagger.Module

@Module
interface FlightsModule {
    @Binds
    fun bindFlightsRepository(repositoryFlights: FlightsRepositoryImpl): FlightsRepository

    @Binds
    fun bindFlightTypesRepository(repositoryTypes: FlightTypesRepositoryImpl): FlightTypesRepository

    @Binds
    fun bindPlaneTypeRepository(repositoryPlaneTypes: PlaneTypesRepositoryImpl): PlaneTypesRepository
}