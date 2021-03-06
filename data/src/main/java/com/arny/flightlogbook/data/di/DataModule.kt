package com.arny.flightlogbook.data.di

import android.content.Context
import androidx.room.Room
import com.arny.core.CONSTS
import com.arny.domain.di.DomainModule
import com.arny.flightlogbook.data.db.DatabaseMigrations
import com.arny.flightlogbook.data.db.MainDB
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module(includes = [
    FlightsModule::class,
    DomainModule::class,
    AirportsModule::class,
    CustomFieldsModule::class
])
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
        fun provideFlightDao(db: MainDB) = db.flightDAO

        @Provides
        @Singleton
        fun provideFlightTypeDao(db: MainDB) = db.flightTypeDAO

        @Provides
        @Singleton
        fun providePlaneTypeDao(db: MainDB) = db.aircraftTypeDAO

        @Provides
        @Singleton
        fun provideCustomFieldsDao(db: MainDB) = db.customFieldDAO

        @Provides
        @Singleton
        fun provideCustomFieldValuesDao(db: MainDB) = db.customFieldValuesDAO

        @Provides
        @Singleton
        fun provideAirportsDao(db: MainDB) = db.airportsDAO
    }
}