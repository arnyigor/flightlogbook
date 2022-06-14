package com.arny.flightlogbook.data.filereaders

import com.arny.core.utils.*
import com.arny.flightlogbook.customfields.models.CustomField
import com.arny.flightlogbook.customfields.models.CustomFieldValue
import com.arny.flightlogbook.customfields.models.toCustomFieldType
import com.arny.flightlogbook.customfields.repository.ICustomFieldsRepository
import com.arny.flightlogbook.data.repositories.CustomFieldsRepository
import com.arny.flightlogbook.domain.files.FlightFileReadWriter
import com.arny.flightlogbook.domain.flighttypes.FlightTypesRepository
import com.arny.flightlogbook.domain.models.Flight
import com.arny.flightlogbook.domain.models.FlightType
import com.arny.flightlogbook.domain.models.PlaneType
import com.arny.flightlogbook.domain.planetypes.AircraftTypesRepository
import com.google.gson.*
import org.json.JSONObject
import java.io.File
import java.lang.StringBuilder
import javax.inject.Inject

class JsonReader @Inject constructor(
    private val flightTypesRepository: FlightTypesRepository,
    private val aircraftTypesRepository: AircraftTypesRepository,
    private val customFieldsRepository: ICustomFieldsRepository,
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
                        customParams =
                            Utility.jsonToMap(flightObject.getValue<JSONObject>("customParams"))
                        setPlaneType(flightObject)
                        regNo = planeType?.regNo
                        selected = flightObject.getValue("selected") ?: false
                        customFieldsValues = getCustomFieldsValues(flightObject)
                    }
                    add(flight)
                }
            }
        }
    }

    private fun getCustomFieldsValues(
        flightObject: JSONObject,
    ): List<CustomFieldValue>? {
        val jsonArray = flightObject.getJSONArray("customFieldsValues")
        return if (jsonArray.length() > 0) {
            mutableListOf<CustomFieldValue>().apply {
                for (i in 0 until jsonArray.length()) {
                    add(jsonArray[i].toString().fromJson(CustomFieldValue::class.java) {
                        getCustomFieldValue(it)
                    })
                }
            }
        } else {
            null
        }
    }

    private fun getCustomFieldValue(element: JsonElement): CustomFieldValue {
        val valueObj = element.asJsonObject
        val fieldObj = valueObj.get("field").asJsonObject
        val customField = CustomField(
            id = fieldObj.get("id").asLong,
            type = fieldObj?.get("type")?.asString.toCustomFieldType(),
            name = fieldObj.get("name").asString,
            showByDefault = fieldObj.get("showByDefault").asBoolean,
            addTime = fieldObj.get("addTime").asBoolean
        )
        return CustomFieldValue(
            id = valueObj.get("id").asLong,
            field = customField,
        ).apply {
            setValueByType(valueObj.get("value").asString)
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
                    val json = data.toJson(Flight::class.java, serialize = { fl ->
                        val cFieldValues = fl.customFieldsValues ?: emptyList()
                        fl.customFieldsValues = null
                        val sb = getCustomFieldsJsonElements(cFieldValues)
                        val flJson = fl.toJson().orEmpty()
                        JsonPrimitive("${flJson.substring(0, flJson.length - 2)},$sb}")
                    }).replace("\\\"", "\"")
                    val result = json.substring(1, json.length - 1)
                    out.write(result + (if (index < lastIndex) ",\n" else ""))
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun getCustomFieldsJsonElements(cFieldValues: List<CustomFieldValue>): String =
        StringBuilder().apply {
            append("\"customFieldsValues\":[")
            for ((ind, fieldValue) in cFieldValues.withIndex()) {
                append(getValueJson(fieldValue))
                if (ind < cFieldValues.size - 1) {
                    append(",")
                }
            }
            append("]")
        }.toString()

    private fun getValueJson(fieldValue: CustomFieldValue): String =
        fieldValue.toStringJson()

    private fun Long?.toFullDateFormat(): String? =
        this?.let { DateTimeUtils.getDateTime(it, "dd MMM yyyy") }

}