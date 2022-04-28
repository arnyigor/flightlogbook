package com.arny.flightlogbook.data.di

import android.content.Context
import androidx.room.Room
import com.arny.core.CONSTS
import com.arny.flightlogbook.data.db.DatabaseMigrations
import com.arny.flightlogbook.data.db.MainDB
import com.arny.flightlogbook.data.db.daos.*
import com.arny.flightlogbook.data.repositories.FilesRepositoryImpl
import com.arny.flightlogbook.domain.files.FilesRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module(
    includes = [
        FlightsModule::class,
        AirportsModule::class,
        CustomFieldsModule::class,
        FileReaderModule::class
    ]
)
interface DataModule {

    companion object {
        @Provides
        @Singleton
        fun provideDB(context: Context): MainDB {
            return Room.databaseBuilder(context, MainDB::class.java, CONSTS.DB.DB_NAME)
                .addMigrations(DatabaseMigrations(context).getMigration12To13())
                .addMigrations(DatabaseMigrations(context).getMigration13To14())
                .addCallback(DatabaseMigrations(context).onCreateCallback())
                .build()
        }

        @Provides
        @Singleton
        fun provideFlightDao(db: MainDB): FlightDAO = db.flightDAO

        @Provides
        @Singleton
        fun provideFlightTypeDao(db: MainDB): FlightTypeDAO = db.flightTypeDAO

        @Provides
        @Singleton
        fun providePlaneTypeDao(db: MainDB): AircraftTypeDAO = db.aircraftTypeDAO

        @Provides
        @Singleton
        fun provideCustomFieldsDao(db: MainDB): CustomFieldDAO = db.customFieldDAO

        @Provides
        @Singleton
        fun provideCustomFieldValuesDao(db: MainDB): CustomFieldValuesDAO = db.customFieldValuesDAO

        @Provides
        @Singleton
        fun provideAirportsDao(db: MainDB): AirportsDAO = db.airportsDAO
    }

    @Binds
    @Singleton
    fun bindFilesRepository(filesRepository: FilesRepositoryImpl): FilesRepository
}