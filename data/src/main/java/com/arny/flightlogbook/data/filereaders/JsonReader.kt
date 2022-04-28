package com.arny.flightlogbook.data.filereaders

import com.arny.core.utils.DateTimeUtils
import com.arny.core.utils.fromJson
import com.arny.core.utils.toJson
import com.arny.flightlogbook.domain.files.FlightFileReadWriter
import com.arny.flightlogbook.domain.flighttypes.FlightTypesRepository
import com.arny.flightlogbook.domain.models.Flight
import com.arny.flightlogbook.domain.models.FlightType
import com.arny.flightlogbook.domain.models.Params
import com.arny.flightlogbook.domain.planetypes.AircraftTypesRepository
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.json.JSONObject
import java.io.File
import javax.inject.Inject

class JsonReader @Inject constructor(
    private val flightTypesRepository: FlightTypesRepository,
    private val aircraftTypesRepository: AircraftTypesRepository,
) : FlightFileReadWriter {
    private var dbFlightTypes = flightTypesRepository.loadDBFlightTypes()
    private val gson: Gson = GsonBuilder().setLenient().create()
    override fun readFile(file: File): List<Flight> {
        return arrayListOf<Flight>().apply {
            for (line in file.readLines()) {
                if (line.isNotBlank()) {
                    val flightObject = JSONObject(line)
                    val apply = Flight().apply {
                        arrivalUtcTime = flightObject.getValue("arrivalUtcTime")
                        datetime = flightObject.getValue("datetime")
                        datetimeFormatted = this.datetime?.toFullDateFormat()
                        departureUtcTime = flightObject.getValue("departureUtcTime")
                        description = flightObject.getValue("description")
                        flightTime = flightObject.getValue("flightTime") ?: 0
                        setFlightType(flightObject)
                        groundTime = flightObject.getValue("flightTime") ?: 0
                        ifrTime = flightObject.getValue("ifrTime") ?: 0
                        nightTime = flightObject.getValue("nightTime") ?: 0
                        totalTime = flightObject.getValue("totalTime") ?: 0
                        params = Params(flightObject.getValue("params") as? JSONObject)
                    }
                    println(apply)
                    //                    {"arrivalUtcTime":0,
                    //                        "datetime":1447621200000,
                    //                        "datetimeFormatted":"16 нояб. 2015",
                    //                        "departureUtcTime":0,
                    //                        "description":"налет после обучения",
                    //                        "flightTime":2298,
                    //                        "flightType":{"id":0,"typeTitle":"Круги"},
                    //                        "flightTypeId":0,
                    //                        "groundTime":0,
                    //                        "id":1,
                    //                        "ifrTime":0,
                    //                        "logtimeFormatted":"38:18",
                    //                        "nightTime":0,
                    //                        "params":{"params":{"nameValuePairs":{}}},
                    //                        "planeId":2,
                    //                        "planeType":{"mainType":"AIRPLANE","regNo":"01785","typeId":2,"typeName":"P2002"},
                    //                        "regNo":"01785",
                    //                        "selected":false,
                    //                        "totalTime":2298
                    //                    }
                    line.fromJson(gson, Flight::class.java)?.let { flight ->
                        println(flight)
                        add(flight)
                    }
                }
            }
        }
    }

    private fun Flight.setFlightType(flightObject: JSONObject) {
        val flightTypeTmp = (flightObject.getString("flightType"))
            .fromJson(gson, FlightType::class.java)
        dbFlightTypes.find {
            it.typeTitle.equals(other = flightTypeTmp?.typeTitle, ignoreCase = true)
        }?.let { type ->
            flightType = type
            flightTypeId = type.id
        } ?: kotlin.run {
            addFlightTypeToDb(flightTypeTmp)
        }
    }

    private fun Flight.addFlightTypeToDb(flightTypeTmp: FlightType?) {
        flightTypeTmp?.let {
            val id = flightTypesRepository.addFlightTypeAndGet(flightTypeTmp)
            if (id > 0) {
                dbFlightTypes = flightTypesRepository.loadDBFlightTypes()
                dbFlightTypes.find { it.id == id }?.let { type ->
                    flightType = type
                    flightTypeId = id
                }
            }
        }
    }

    override fun writeFile(flights: List<Flight>, file: File): Boolean {
        val exportData = flights
            .map { flight ->
                flight.planeType = aircraftTypesRepository.loadAircraftType(flight.planeId)
                flight.flightType =
                    flightTypesRepository.loadDBFlightType(flight.flightTypeId)
                flight
            }

        return try {
            file.bufferedWriter().use { out ->
                val lastIndex = exportData.lastIndex
                for ((index, data) in exportData.withIndex()) {
                    out.write(data.toJson() + (if (index != lastIndex) ",\n" else ""))
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    inline fun <reified T> JSONObject?.getValue(extraName: String): T? = this?.get(extraName) as? T
    private fun Long?.toFullDateFormat(): String? =
        this?.let { DateTimeUtils.getDateTime(it, "dd MMM yyyy") }

}