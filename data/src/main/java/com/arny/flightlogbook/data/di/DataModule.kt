package com.arny.flightlogbook.data.di

import android.content.Context
import androidx.room.Room
import com.arny.flightlogbook.constants.CONSTS
import com.arny.flightlogbook.data.db.MainDB
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module(includes = [FlightsModule::class])
class DataModule {

    @Provides
    @Singleton
    fun provideDB(context: Context): MainDB {
        return Room.databaseBuilder(context, MainDB::class.java, CONSTS.DB.DB_NAME)
                .fallbackToDestructiveMigrationFrom()
//                .addMigrations(DatabaseMigrations(context).getMigration12To13())
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
}