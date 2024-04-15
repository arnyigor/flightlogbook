package com.arny.flightlogbook.data.db.intity.airports

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.arny.flightlogbook.data.models.Airport

@Entity(
        tableName = "airports",
        indices = [Index("icao", "iata", unique = true)]
)
data class AirportEntity(
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "_id")
        val id: Long? = null,
        val icao: String? = null,
        val iata: String? = null,
        @ColumnInfo(name = "name_rus")
        val nameRus: String? = null,
        @ColumnInfo(name = "name_eng")
        val nameEng: String? = null,
        @ColumnInfo(name = "city_rus")
        val cityRus: String? = null,
        @ColumnInfo(name = "city_eng")
        val cityEng: String? = null,
        @ColumnInfo(name = "country_rus")
        val countryRus: String? = null,
        @ColumnInfo(name = "country_eng")
        val countryEng: String? = null,
        @ColumnInfo(name = "latitude")
        val latitude: Double? = null,
        @ColumnInfo(name = "longitude")
        val longitude: Double? = null,
        @ColumnInfo(name = "elevation")
        val elevation: Double? = null,
) {
    fun toAirport(): Airport {
        return Airport(
                id,
                icao,
                iata,
                nameRus,
                nameEng,
                cityRus,
                cityEng,
                countryRus,
                countryEng,
                latitude,
                longitude,
                elevation
        )
    }
}

fun Airport.toAirportEntity(): AirportEntity {
    return AirportEntity(id, icao, iata, nameRus, nameEng, cityRus, cityEng, countryRus, countryEng, latitude, longitude, elevation)
}
