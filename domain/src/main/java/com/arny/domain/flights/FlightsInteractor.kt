package com.arny.domain.flights

import android.net.Uri
import com.arny.constants.CONSTS
import com.arny.domain.R
import com.arny.domain.common.PreferencesProvider
import com.arny.domain.common.ResourcesProvider
import com.arny.domain.flighttypes.FlightTypesRepository
import com.arny.domain.models.Flight
import com.arny.domain.models.FlightType
import com.arny.domain.models.PlaneType
import com.arny.domain.planetypes.PlaneTypesRepository
import com.arny.helpers.utils.*
import io.reactivex.Observable
import org.apache.poi.hssf.usermodel.HSSFCell
import org.apache.poi.hssf.usermodel.HSSFRow
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Cell
import java.io.File
import java.io.FileInputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FlightsInteractor @Inject constructor(
    private val flightTypesRepository: FlightTypesRepository,
    private val flightsRepository: FlightsRepository,
    private val resourcesProvider: ResourcesProvider,
    private val planeTypesRepository: PlaneTypesRepository,
    private val preferencesProvider: PreferencesProvider
) {
    fun insertFlights(flights: List<Flight>): Observable<Boolean> {
        return fromCallable { flightsRepository.insertFlights(flights) }
    }

    fun removeAllFlightsObs(): Observable<Boolean> {
        return fromCallable { flightsRepository.removeAllFlights() }
    }

    fun removeAllFlights(): Boolean {
        return flightsRepository.removeAllFlights()
    }

    fun updateFlight(flight: Flight): Observable<Boolean> {
        return fromCallable { flightsRepository.updateFlight(flight) }
    }

    fun insertFlightAndGet(flight: Flight): Observable<Boolean> {
        return fromCallable { flightsRepository.insertFlightAndGet(flight) > 0 }
    }

    fun getFlight(id: Long?): Flight? {
        return flightsRepository.getFlight(id)
            ?.apply {
                this.planeType = planeTypesRepository.loadPlaneType(planeId)
                this.flightType = flightTypesRepository.loadDBFlightType(flightTypeId?.toLong())
            }
    }

    fun loadPlaneTypes(): Observable<List<PlaneType>> {
        return fromCallable { planeTypesRepository.loadPlaneTypes() }
    }

    fun loadPlaneType(id: Long?): PlaneType? {
        return planeTypesRepository.loadPlaneType(id)
    }

    fun addPlaneType(name: String): Boolean {
        return planeTypesRepository.addType(name)
    }

    fun addPlaneTypeAndGet(name: String): Long {
        return planeTypesRepository.addTypeAndGet(name)
    }

    fun loadPlaneType(title: String?): PlaneType? {
        return planeTypesRepository.loadPlaneType(title)
    }

    fun loadFlightType(id: Long?): FlightType? {
        return flightTypesRepository.loadDBFlightType(id)
    }

    private fun getFormattedFlightTimes(): String {
        val flightsTime = flightsRepository.getFlightsTime()
        val totalTimes = 0//repository.queryDBFlightsTimesSum()
        val totalFlightTimes = 0// repository.queryDBFlightsTimesSum(true)
        val sumlogTime = flightsTime + totalTimes
        val sumFlightTime = flightsTime + totalFlightTimes
        val flightsCount = flightsRepository.getFlightsCount()
        return String.format(
            "%s %s\n%s %s\n%s %d",
            resourcesProvider.getString(R.string.str_total_time),
            DateTimeUtils.strLogTime(sumlogTime),
            resourcesProvider.getString(R.string.str_total_flight_time),
            DateTimeUtils.strLogTime(sumFlightTime),
            resourcesProvider.getString(R.string.total_records),
            flightsCount
        )
    }

    fun getTotalflightsTimeInfo(): Observable<String> {
        return fromCallable { getFormattedFlightTimes() }
    }

    private fun getPrefOrderflights(filtertype: Int): String = when (filtertype) {
        0 -> CONSTS.DB.COLUMN_DATETIME
        1 -> CONSTS.DB.COLUMN_DATETIME + " DESC"
        2 -> CONSTS.DB.COLUMN_LOG_TIME
        3 -> CONSTS.DB.COLUMN_LOG_TIME + " DESC"
        else -> CONSTS.DB.COLUMN_DATETIME + " ASC"
    }

    fun setFlightsOrder(orderType: Int) {
        preferencesProvider.setPrefInt(CONSTS.PREFS.PREF_USER_FILTER_FLIGHTS, orderType)
    }

    fun loadDBFlights(): List<Flight> {
        return flightsRepository.getDbFlights()
            .map { flight ->
                flight.planeType = planeTypesRepository.loadPlaneType(flight.planeId)
                flight.flightType =
                    flightTypesRepository.loadDBFlightType(flight.flightTypeId?.toLong())
//                    flight.sumFlightTime = (flight.flightTime?:0) + (flight.times?.filter { it.addToFlightTime }?.sumBy { it.time }?:0)
//                    flight.sumGroundTime = (flight.times?.filter { !it.addToFlightTime }?.sumBy { it.time }?:0)
                flight
            }
    }

    fun getFilterFlightsObs(): Observable<List<Flight>> {
        val orderType = preferencesProvider.getPrefInt(CONSTS.PREFS.PREF_USER_FILTER_FLIGHTS)
        return fromCallable {
            val order = getPrefOrderflights(orderType)
            val flightTypes = flightTypesRepository.loadDBFlightTypes()
            val planeTypes = planeTypesRepository.loadPlaneTypes()
            val flights = flightsRepository.getDbFlights(order)
            flights.map { flight ->
                flight.planeType = planeTypes.find { it.typeId == flight.planeId }
                flight.flightType = flightTypes.find { it.id == flight.flightTypeId?.toLong() }
                flight
            }
        }.map { flights ->
            val res = when (orderType) {
                0 -> flights.sortedBy { it.datetime }
                1 -> flights.sortedByDescending { it.datetime }
                2 -> flights.sortedBy { it.flightTime }
                3 -> flights.sortedByDescending { it.flightTime }
                else -> flights
            }
            res
        }
    }

    fun removeFlight(id: Long?): Observable<Boolean> {
        return fromCallable { flightsRepository.removeFlight(id) }
    }

    fun readExcelFile(
        path: String?,
        fromSystem: Boolean,
        onProgress: (state: String, iter: Int, total: Int) -> Unit
    ): Boolean {
        val ctx = resourcesProvider.provideContext()
        val saving = resourcesProvider.getString(R.string.saving)
        val loadingFile = resourcesProvider.getString(R.string.loading_file)
        val fileImport = resourcesProvider.getString(R.string.importing_file)
        onProgress.invoke(loadingFile, 0, 100)
        val fileUri = Uri.fromFile(File(path))
        val filename = FileUtils.getSDFilePath(ctx, fileUri)
        val myWorkBook: HSSFWorkbook
        val xlsfile: File
        val notAccess =
            !FileUtils.isExternalStorageAvailable() || FileUtils.isExternalStorageReadOnly()
        if (notAccess) {
            return false
        }
        xlsfile = if (fromSystem) {
            File(ctx.getExternalFilesDir(null), filename)
        } else {
            File("", filename)
        }
        val fileInputStream = FileInputStream(xlsfile)
        myWorkBook = HSSFWorkbook(fileInputStream)
        // Get the first sheet from workbook
        val mySheet = myWorkBook.getSheetAt(0)
        /** We now need something to iterate through the cells. */
        val numberOfRows = mySheet.physicalNumberOfRows
        val rowIter = mySheet.rowIterator()
        flightsRepository.removeAllFlights()
        flightsRepository.resetTableFlights()
        onProgress.invoke(loadingFile, 100, 100)
        val flightsFromExel = getFlightsFromExcel(rowIter) {
            val percent = MathUtils.getPercent(it.toDouble(), numberOfRows.toDouble()).toInt()
            onProgress.invoke(fileImport, percent, 100)
        }
        onProgress.invoke(saving, 50, 100)
        flightsRepository.insertFlights(flightsFromExel)
        onProgress.invoke(saving, 100, 100)
        return true
    }

    private fun getFlightsFromExcel(
        rowIter: Iterator<*>,
        onProgress: (iter: Int) -> Unit
    ): ArrayList<Flight> {
        val flights = ArrayList<Flight>()
        var rowCnt = 0
        var strDate: String? = null
        var strTime: String?
        var airplane_type: String?
        var reg_no: String?
        var strDesc: String
        var airplane_type_id: Long = 0
        var flight_type: Long
        var logTime = 0
        var mDateTime: Long = 0
        var planeTypes = planeTypesRepository.loadPlaneTypes()
        var flightTypes = flightTypesRepository.loadDBFlightTypes()
        var id = 1L
        while (rowIter.hasNext()) {
            val myRow = rowIter.next() as HSSFRow
            val lastCell = myRow.lastCellNum.toInt()
            val cellIter = myRow.cellIterator()
//            Log.d(this::class.java.simpleName, "rowIter $rowCnt")
            var cellCnt = 1
            val flight = Flight(id)
            while (cellIter.hasNext()) {
                val myCell = cellIter.next() as HSSFCell
//                Log.d(this::class.java.simpleName, "Cell: $cellCnt")
//                Log.d(this::class.java.simpleName, "Cell Value: $myCell")
                if (rowCnt > 0) {
                    when (cellCnt) {
                        1 -> {
                            try {
                                strDate = myCell.toString()
                            } catch (e: Exception) {
                                strDate = DateTimeUtils.getDateTime(
                                    System.currentTimeMillis(),
                                    "dd MMM yyyy"
                                )
                                e.printStackTrace()
                            }
                        }
                        2 -> {
                            try {
                                strTime = if (myCell.cellType == Cell.CELL_TYPE_NUMERIC) {
                                    Utility.match(
                                        myCell.dateCellValue.toString(),
                                        "(\\d{2}:\\d{2})",
                                        1
                                    )
                                } else {
                                    myCell.toString()
                                }
                            } catch (e: Exception) {
                                strTime = "00:00"
                                e.printStackTrace()
                            }
                            try {
                                logTime = DateTimeUtils.convertStringToTime(strTime!!)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                            flight.flightTime = logTime
                        }
                        3 -> {
                            try {
                                airplane_type = myCell.toString()
                                val planeType = planeTypes.find { it.typeName == airplane_type }
                                if (planeType != null) {
                                    airplane_type_id = planeType.typeId ?: 0
                                } else {
                                    if (!airplane_type.isNullOrBlank()) {
                                        airplane_type_id =
                                            planeTypesRepository.addTypeAndGet(airplane_type)
                                        planeTypes = planeTypesRepository.loadPlaneTypes()
                                    }
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                airplane_type_id = 0
                            }
                            flight.planeId = airplane_type_id
                        }
                        4 -> {
                            try {
                                reg_no = myCell.toString()
                            } catch (e: Exception) {
                                reg_no = ""
                                e.printStackTrace()
                            }
                            flight.regNo = reg_no
                        }
                        7 -> {
                            try {
                                val fTypeStr = myCell.toString()
                                flight_type =
                                    if (fTypeStr.contains(".")) fTypeStr.parseDouble()?.toLong()
                                        ?: -1 else fTypeStr.parseLong() ?: -1
                                val planeType = flightTypes.find { it.id == flight_type }
                                if (planeType != null) {
                                    airplane_type_id = planeType.id ?: -1
                                } else {
                                    if (flight_type != -1L) {
                                        val type = getOldFlightType(flight_type)
                                        val flightTypeEntity =
                                            flightTypes.find { it.typeTitle == type }
                                        if (flightTypeEntity != null) {
                                            airplane_type_id = flightTypeEntity.id ?: -1
                                        } else {
                                            flight_type =
                                                flightTypesRepository.addFlightTypeAndGet(type)
                                            flightTypes = flightTypesRepository.loadDBFlightTypes()
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                flight_type = -1
                                e.printStackTrace()
                            }
                            flight.flightTypeId = flight_type.toInt()
                        }
                        8 -> {
                            try {
                                strDesc = myCell.toString()
                            } catch (e: Exception) {
                                strDesc = ""
                                e.printStackTrace()
                            }
                            flight.description = strDesc

                        }
                        9 -> {
                            try {
                                strDesc = myCell.toString()
                            } catch (e: Exception) {
                                strDesc = ""
                                e.printStackTrace()
                            }
                            flight.description = strDesc

                        }
                        10 -> {
                            val cell = try {
                                myCell.toString()
                            } catch (e: Exception) {
                                ""
                            }
                        }
                    }
                    if (lastCell == cellCnt) {
                        var format = "dd MMM yyyy"
                        strDate = strDate!!.replace("-", " ").replace(".", " ")
                            .replace("\\s+".toRegex(), " ")
                        try {
                            format = DateTimeUtils.dateFormatChooser(strDate)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        try {
                            mDateTime = DateTimeUtils.convertTimeStringToLong(strDate, format)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        flight.datetime = mDateTime
                        flights.add(flight)
                        id++
                    }
                }//if (rowCnt>0)
                cellCnt++
            }//cellIter.hasNext()
            onProgress.invoke(rowCnt)
            rowCnt++
        }//while (rowIter.hasNext())
        return flights
    }

    private fun getOldFlightType(flight_type: Long): String {
        return when (flight_type) {
            0L -> resourcesProvider.getString(R.string.flight_type_circle)
            1L -> resourcesProvider.getString(R.string.flight_type_zone)
            2L -> resourcesProvider.getString(R.string.flight_type_route)
            else -> ""
        }
    }
}