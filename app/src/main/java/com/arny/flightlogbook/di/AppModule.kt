package com.arny.flightlogbook.di

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.arny.constants.CONSTS
import com.arny.data.db.MainDB
import com.arny.data.db.daos.AircraftTypeDAO
import com.arny.data.db.daos.DatabaseMigrations
import com.arny.data.db.daos.FlightDAO
import com.arny.data.db.daos.FlightTypeDAO
import com.arny.data.repositories.*
import com.arny.domain.common.PreferencesProvider
import com.arny.domain.common.ResourcesProvider
import com.arny.domain.flights.FlightsRepository
import com.arny.domain.flighttypes.FlightTypesRepository
import com.arny.domain.planetypes.PlaneTypesRepository
import com.arny.helpers.utils.Prefs
import dagger.Binds
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module(includes = [AppModule.ResourceModule::class, AppModule.FlightsModule::class])
class AppModule(private val application: Application) {
    @Provides
    @Singleton
    fun provideContext(): Context {
        return application
    }

    @Provides
    @Singleton
    fun providePrefs(): Prefs {
        return Prefs.getInstance(application)
    }

    @Provides
    @Singleton
    fun provideDB(): MainDB {
        return Room.databaseBuilder(application, MainDB::class.java, CONSTS.DB.DB_NAME)
                .addMigrations(DatabaseMigrations.MIGRATION_15_16)
                .build()
    }

    @Provides
    @Singleton
    fun provideFlightDao(db: MainDB): FlightDAO {
        return db.flightDAO
    }

    @Provides
    @Singleton
    fun provideFlightTypeDao(db: MainDB): FlightTypeDAO {
        return db.flightTypeDAO
    }

    @Provides
    @Singleton
    fun providePlaneTypeDao(db: MainDB): AircraftTypeDAO {
        return db.aircraftTypeDAO
    }

    @Module
    abstract class FlightsModule {
        @Binds
        @Singleton
        abstract fun bindFlights(sources: FlightsRepositoryImpl): FlightsRepository

        @Binds
        @Singleton
        abstract fun bindFlightTypes(sources: FlightTypesRepositoryImpl): FlightTypesRepository

        @Binds
        @Singleton
        abstract fun bindPlaneTypes(sources: PlaneTypesRepositoryImpl): PlaneTypesRepository
    }

    @Module
    abstract class ResourceModule {
        @Binds
        @Singleton
        abstract fun bindResources(sources: ResourcesProviderImpl): ResourcesProvider

        @Binds
        @Singleton
        abstract fun bindPrefs(sources: PreferencesProviderImpl): PreferencesProvider
    }
}