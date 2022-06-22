package com.arny.flightlogbook.data.di

import com.arny.flightlogbook.customfields.repository.ICustomFieldsRepository
import com.arny.flightlogbook.data.filereaders.JsonReader
import com.arny.flightlogbook.data.filereaders.XlsReader
import com.arny.flightlogbook.data.repositories.CustomFieldsRepository
import com.arny.flightlogbook.domain.airports.IAirportsRepository
import com.arny.flightlogbook.domain.common.IResourceProvider
import com.arny.flightlogbook.domain.files.FlightFileReadWriter
import com.arny.flightlogbook.domain.flighttypes.FlightTypesRepository
import com.arny.flightlogbook.domain.planetypes.AircraftTypesRepository
import dagger.Module
import dagger.Provides
import javax.inject.Qualifier

@Module
object FileReaderModule {
    @JsonRead
    @Provides
    fun providesJsonReader(
        flightTypesRepository: FlightTypesRepository,
        aircraftTypesRepository: AircraftTypesRepository,
        customFieldsRepository: ICustomFieldsRepository,
        airportsRepository: IAirportsRepository,
    ): FlightFileReadWriter =
        JsonReader(
            flightTypesRepository,
            aircraftTypesRepository,
            customFieldsRepository,
            airportsRepository
        )

    @XlsRead
    @Provides
    fun providesMainDispatcher(
        resourcesProvider: IResourceProvider,
        flightTypesRepository: FlightTypesRepository,
        aircraftTypesRepository: AircraftTypesRepository,
    ): FlightFileReadWriter =
        XlsReader(resourcesProvider, flightTypesRepository, aircraftTypesRepository)
}

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class XlsRead

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class JsonRead
