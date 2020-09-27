package com.arny.flightlogbook.data.db.daos

import androidx.room.Dao
import androidx.room.Query
import com.arny.flightlogbook.data.models.airports.AirportEntity

@Dao
interface AirportsDAO : BaseDao<AirportEntity> {
    @Query("SELECT * FROM airports")
    fun getDbAirports(): List<AirportEntity>

    @Query("SELECT * FROM airports WHERE _id =:id")
    fun getDbAirport(id: Long): AirportEntity?

    @Query("SELECT * FROM airports WHERE icao LIKE :icao OR iata LIKE :iata OR name_rus LIKE :name  OR name_eng LIKE :name  OR city_rus LIKE :city  OR city_eng LIKE :city  OR country_rus LIKE :country  OR country_eng LIKE :country")
    fun getDbAirportsLike(
            icao: String?,
            iata: String?,
            name: String?,
            city: String?,
            country: String?,
    ): List<AirportEntity>

    @Query("DELETE FROM airports WHERE _id=:id")
    fun delete(id: Long?): Int
}
