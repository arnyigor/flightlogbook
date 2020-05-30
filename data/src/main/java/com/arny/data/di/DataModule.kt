package com.arny.data.di

import android.content.Context
import androidx.room.Room
import com.arny.constants.CONSTS
import com.arny.data.db.DatabaseMigrations
import com.arny.data.db.MainDB
import com.arny.data.db.daos.AircraftTypeDAO
import com.arny.data.db.daos.FlightDAO
import com.arny.data.db.daos.FlightTypeDAO
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module(includes = [FlightsModule::class])
class DataModule {

    @Provides
    @Singleton
    fun provideDB(context: Context): MainDB {
        return Room.databaseBuilder(context, MainDB::class.java, CONSTS.DB.DB_NAME)
                .addMigrations(DatabaseMigrations(context).getMigration12To13())
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
}