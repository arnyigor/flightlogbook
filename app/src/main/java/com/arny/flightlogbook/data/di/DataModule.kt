package com.arny.flightlogbook.data.di

import android.content.Context
import androidx.room.Room
import com.arny.flightlogbook.data.CONSTS
import com.arny.flightlogbook.data.db.DatabaseMigrations
import com.arny.flightlogbook.data.db.MainDB
import com.arny.flightlogbook.data.db.daos.AircraftTypeDAO
import com.arny.flightlogbook.data.db.daos.AirportsDAO
import com.arny.flightlogbook.data.db.daos.CustomFieldDAO
import com.arny.flightlogbook.data.db.daos.CustomFieldValuesDAO
import com.arny.flightlogbook.data.db.daos.FlightDAO
import com.arny.flightlogbook.data.db.daos.FlightTypeDAO
import com.arny.flightlogbook.data.prefs.Prefs
import com.arny.flightlogbook.data.repository.FilesRepositoryImpl
import com.arny.flightlogbook.domain.files.FilesRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

@Module
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

        @Provides
        @Singleton
        fun providePreferences(context: Context): Prefs = Prefs.getInstance(context)

        @Provides
        @Singleton
        fun providesIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
    }

    @Binds
    @Singleton
    fun bindFilesRepository(filesRepository: FilesRepositoryImpl): FilesRepository
}