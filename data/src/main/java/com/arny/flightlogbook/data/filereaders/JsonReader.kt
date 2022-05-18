package com.arny.flightlogbook.data.filereaders

import com.arny.core.utils.DateTimeUtils
import com.arny.core.utils.fromJson
import com.arny.core.utils.toJson
import com.arny.flightlogbook.data.repositories.CustomFieldsRepository
import com.arny.flightlogbook.domain.files.FlightFileReadWriter
import com.arny.flightlogbook.domain.flighttypes.FlightTypesRepository
import com.arny.flightlogbook.domain.models.Flight
import com.arny.flightlogbook.domain.models.FlightType
import com.arny.flightlogbook.domain.models.Params
import com.arny.flightlogbook.domain.models.PlaneType
import com.arny.flightlogbook.domain.planetypes.AircraftTypesRepository
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.json.JSONObject
import java.io.File
import javax.inject.Inject

class JsonReader @Inject constructor(
    private val flightTypesRepository: FlightTypesRepository,
    private val aircraftTypesRepository: AircraftTypesRepository,
    private val customFieldsRepository: CustomFieldsRepository,
) : FlightFileReadWriter {
    private var dbFlightTypes: List<FlightType> = emptyList()
    private var dbPlaneTypes: List<PlaneType> = emptyList()
    private val gson: Gson = GsonBuilder().setLenient().create()
    override fun readFile(file: File): List<Flight> {
        updateDbFlights()
        updateDbPlanes()
        return arrayListOf<Flight>().apply {
            for (line in file.readLines()) {
                if (line.isNotBlank()) {
                    val flightObject = JSONObject(line)
                    println(flightObject)
                    val flight = Flight().apply {
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
                        flightTimeFormatted = DateTimeUtils.strLogTime(flightTime)
                        colorInt = flightObject.getValue("colorInt") ?: 0
                        ifrvfr = flightObject.getValue("ifrvfr") ?: 0
                        customParams = flightObject.getValue("params")
                        setPlaneType(flightObject)
                        regNo = planeType?.regNo
                        selected = flightObject.getValue("selected") ?: false

                    }
                    add(flight)
/*                    {
                        "totalTime": 310,
                        "id": 1,
                        "colorText": -1,
                        "flightTypeId": 1,
                        "arrivalUtcTime": 225,
                        "description": "",
                        "datetimeFormatted": "18 \u043c\u0430\u044f 2022",
                        "planeType": {
                        "typeId": 2,
                        "mainType": "AIRPLANE",
                        "typeName": "\u0411737",
                        "regNo": "gngfx357"
                    },
                        "colorInt": -16737281,
                        "datetime": 1652852008667,
                        "selected": false,
                        "logtimeFormatted": "02:00",
                        "ifrvfr": 0,
                        "departureUtcTime": 105,
                        "flightType": {
                        "typeTitle": "\u0420\u0435\u0439\u0441",
                        "id": 1
                    },
                        "flightTime": 145,
                        "params": {
                        "params": {
                        "nameValuePairs": {
                        "params": {
                        "nameValuePairs": {
                        "nameValuePairs": {
                        "nameValuePairs": {
                        "params": {
                        "nameValuePairs": {
                        "nameValuePairs": {
                        "nameValuePairs": {
                        "nameValuePairs": {
                        "nameValuePairs": {
                        "nameValuePairs": {
                        "nameValuePairs": {}
                    }
                    }
                    }
                    }
                    }
                    }
                    },
                        "color": "#FFDB66"
                    }
                    }
                    }
                    },
                        "nodeString": "#FFDB66",
                        "color": "#009BFF"
                    }
                    },
                        "nodeString": "#009BFF"
                    },
                        "groundTime": 165,
                        "nightTime": 0,
                        "planeId": 2,
                        "ifrTime": 0
                    }*/
                }
            }
        }
    }

    private fun Flight.setPlaneType(flightObject: JSONObject) {
        if(flightObject.has("planeType")){
                val planeTypeTmp = (flightObject.getString("planeType"))
                    .fromJson(gson, PlaneType::class.java)
                dbPlaneTypes.find {
                    it.typeName.equals(other = planeTypeTmp?.typeName, ignoreCase = true)
                }?.let { type ->
                    planeType = type
                    planeId = type.typeId
                } ?: kotlin.run {
                    addPlaneTypeToDb(planeTypeTmp)
                }
        }
    }

    private fun updateDbFlights() {
        dbFlightTypes = flightTypesRepository.loadDBFlightTypes()
    }

    private fun updateDbPlanes() {
        dbPlaneTypes = aircraftTypesRepository.loadAircraftTypes()
    }

    private fun Flight.setFlightType(flightObject: JSONObject) {
        if (flightObject.has("flightType")) {
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
    }

    private fun Flight.addFlightTypeToDb(flightTypeTmp: FlightType?) {
        flightTypeTmp?.let {
            val id = flightTypesRepository.addFlightTypeAndGet(flightTypeTmp)
            if (id > 0) {
                updateDbFlights()
                dbFlightTypes.find { it.id == id }?.let { type ->
                    flightType = type
                    flightTypeId = id
                }
            }
        }
    }

    private fun Flight.addPlaneTypeToDb(planeTypeTmp: PlaneType?) {
        planeTypeTmp?.let {
            val id = aircraftTypesRepository.addType(planeTypeTmp)
            if (id > 0) {
                updateDbPlanes()
                dbPlaneTypes.find { it.typeId == id }?.let { type ->
                    planeType = type
                    planeId = id
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
                    val jsonData = data.toJson()
                    println(jsonData)
                    out.write(jsonData + (if (index != lastIndex) ",\n" else ""))
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