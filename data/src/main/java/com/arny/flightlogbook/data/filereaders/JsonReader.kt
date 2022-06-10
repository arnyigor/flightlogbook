package com.arny.flightlogbook.data.filereaders

import android.util.Log
import com.arny.core.utils.DateTimeUtils
import com.arny.core.utils.fromJson
import com.arny.core.utils.toJson
import com.arny.flightlogbook.customfields.models.CustomField
import com.arny.flightlogbook.customfields.models.CustomFieldValue
import com.arny.flightlogbook.data.repositories.CustomFieldsRepository
import com.arny.flightlogbook.domain.files.FlightFileReadWriter
import com.arny.flightlogbook.domain.flighttypes.FlightTypesRepository
import com.arny.flightlogbook.domain.models.Flight
import com.arny.flightlogbook.domain.models.FlightType
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
    private var dbCustomFields: List<CustomField> = emptyList()
    private var dbFlightTypes: List<FlightType> = emptyList()
    private var dbPlaneTypes: List<PlaneType> = emptyList()
    private val gson: Gson = GsonBuilder().setLenient().create()
    override fun readFile(file: File): List<Flight> {
        updateDbFlights()
        updateDbPlanes()
        updateCustomFielsValues()
        return arrayListOf<Flight>().apply {
            for (line in file.readLines()) {
                if (line.isNotBlank()) {
                    val flightObject = JSONObject(line)
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
                        customParams = flightObject.getValue("customParams")
                        setPlaneType(flightObject)
                        regNo = planeType?.regNo
                        selected = flightObject.getValue("selected") ?: false
                    }
                    add(flight)
                }
            }
        }
    }

    private fun Flight.setPlaneType(flightObject: JSONObject) {
        if (flightObject.has("planeType")) {
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

    private fun updateCustomFielsValues() {
        dbCustomFields = customFieldsRepository.getAllCustomFields()
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
        val customFields by lazy { customFieldsRepository.getAllCustomFields() }
        val exportData = flights
            .map { flight ->
                flight.apply {
                    planeType = aircraftTypesRepository.loadAircraftType(flight.planeId)
                    flightType = flightTypesRepository.loadDBFlightType(flight.flightTypeId)
                    customFieldsValues = flight.id?.let {
                        customFieldsRepository.getCustomFieldValues(it)
                    }
                        .orEmpty()
                        .map { fieldValue ->
                            fieldValue.apply {
                                field = customFields.find { customField ->
                                    customField.id == fieldId
                                }
                            }
                        }
                }
            }
        return try {
            file.bufferedWriter().use { out ->
                val lastIndex = exportData.lastIndex
                for ((index, data) in exportData.withIndex()) {
                    // TODO customfields to string fix
                    val jsonData = data.toJson()
                    Log.d(JsonReader::class.java.simpleName, "jsonData: $jsonData");
                    out.write(jsonData + (if (index != lastIndex) ",\n" else ""))
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    inline fun <reified T> JSONObject?.getValue(extraName: String): T? =
        if (this?.has(extraName) == true) {
            this.get(extraName) as? T
        } else {
            null
        }

    private fun Long?.toFullDateFormat(): String? =
        this?.let { DateTimeUtils.getDateTime(it, "dd MMM yyyy") }

}