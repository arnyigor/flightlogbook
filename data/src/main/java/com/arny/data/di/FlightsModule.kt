package com.arny.data.di

import com.arny.data.repositories.FlightTypesRepositoryImpl
import com.arny.data.repositories.FlightsRepositoryImpl
import com.arny.data.repositories.PlaneTypesRepositoryImpl
import com.arny.domain.flights.FlightsRepository
import com.arny.domain.flighttypes.FlightTypesRepository
import com.arny.domain.planetypes.PlaneTypesRepository
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
    fun bindPlaneTypeRepository(repositoryPlaneTypes: PlaneTypesRepositoryImpl): PlaneTypesRepository
}