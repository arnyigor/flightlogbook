package com.arny.flightlogbook.domain.di

import com.arny.flightlogbook.domain.airports.AirportsInteractor
import com.arny.flightlogbook.domain.airports.IAirportsInteractor
import com.arny.flightlogbook.domain.files.FilesInteractor
import com.arny.flightlogbook.domain.files.FilesInteractorImpl
import com.arny.flightlogbook.domain.files.FilesRepository
import com.arny.flightlogbook.domain.files.FilesRepositoryImpl
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

@Module
interface DomainModule {
    @Binds
    @Singleton
    fun bindFilesInteractor(filesInteractor: FilesInteractorImpl): FilesInteractor

    @Binds
    @Singleton
    fun bindFilesRepository(filesRepository: FilesRepositoryImpl): FilesRepository

    @Binds
    @Singleton
    fun bindsAirportsRepository(interactor: AirportsInteractor): IAirportsInteractor
}