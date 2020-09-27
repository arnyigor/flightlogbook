package com.arny.domain.di

import com.arny.domain.airports.AirportsInteractor
import com.arny.domain.airports.IAirportsInteractor
import com.arny.domain.files.FilesInteractor
import com.arny.domain.files.FilesInteractorImpl
import com.arny.domain.files.FilesRepository
import com.arny.domain.files.FilesRepositoryImpl
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