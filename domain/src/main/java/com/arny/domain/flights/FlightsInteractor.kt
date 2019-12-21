package com.arny.domain.flights

import android.net.Uri
import android.util.Log
import com.arny.constants.CONSTS
import com.arny.data.repositories.MainRepositoryImpl
import com.arny.domain.R
import com.arny.domain.models.*
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
class FlightsInteractor @Inject constructor(private val repository: MainRepositoryImpl) {

    fun insertFlights(flights: List<Flight>): Observable<Boolean> {
        return fromCallable { repository.insertFlights(flights.map { it.toFlightEntity() }) }
    }

    fun removeAllFlightsObs(): Observable<Boolean> {
        return fromCallable { repository.removeAllFlights() }
    }

    fun removeAllFlights(): Boolean {
        return repository.removeAllFlights()
    }

    fun updateFlight(flight: Flight): Observable<Boolean> {
        return fromCallable { repository.updateFlight(flight.toFlightEntity()) }
    }

    fun insertFlightAndGet(flight: Flight): Observable<Boolean> {
        return fromCallable { repository.insertFlightAndGet(flight.toFlightEntity()) > 0 }
    }

    fun getFlight(id: Long?): Flight? {
        return repository.getFlight(id)?.toFlight()
                ?.apply {
                    val planeType = repository.loadPlaneType(this.planeId)
                    val flightTypeEntity = repository.loadDBFlightType(this.flightTypeId?.toLong())
                    this.planeType = planeType?.toPlaneType()
                    this.flightType = flightTypeEntity?.toFlightType()
                }
    }

    fun loadPlaneTypes(): Observable<List<PlaneType>> {
        return fromCallable { repository.loadPlaneTypes().map { it.toPlaneType() } }
    }

    fun loadPlaneTypeObs(id: Long?): PlaneType? {
        return repository.loadPlaneType(id)?.toPlaneType()
    }

    fun loadPlaneType(id: Long?): PlaneType? {
        return repository.loadPlaneType(id)?.toPlaneType()
    }

    fun addPlaneType(name: String): Boolean {
        return repository.addType(name)
    }

    fun addPlaneTypeAndGet(name: String): Long {
        return repository.addTypeAndGet(name)
    }

    fun loadPlaneType(title: String?): PlaneType? {
        return repository.loadPlaneType(title)?.toPlaneType()
    }

    fun loadFlightType(id: Long?): FlightType? {
        return repository.loadDBFlightType(id)?.toFlightType()
    }


    private fun getFormattedFlightTimes(): String {
        val flightsTime = repository.getFlightsTime()
        val totalTimes = 0//repository.queryDBFlightsTimesSum()
        val totalFlightTimes = 0// repository.queryDBFlightsTimesSum(true)
        val sumlogTime = flightsTime + totalTimes
        val sumFlightTime = flightsTime + totalFlightTimes
        val flightsCount = repository.getFlightsCount()
        return String.format("%s %s\n%s %s\n%s %d",
                repository.getString(R.string.str_total_time),
                DateTimeUtils.strLogTime(sumlogTime),
                repository.getString(R.string.str_total_flight_time),
                DateTimeUtils.strLogTime(sumFlightTime),
                repository.getString(R.string.total_records),
                flightsCount)
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
        repository.setPrefInt(CONSTS.PREFS.PREF_USER_FILTER_FLIGHTS, orderType)
    }

    fun loadDBFlights(): List<Flight> {
        return repository.getDbFlights()
                .map { it.toFlight() }
                .map { flight ->
                    val planeType = repository.loadPlaneType(flight.planeId)
                    flight.planeType = planeType?.toPlaneType()
                    flight.flightType = repository.loadDBFlightType(flight.flightTypeId?.toLong())?.toFlightType()
//                    flight.sumFlightTime = (flight.flightTime?:0) + (flight.times?.filter { it.addToFlightTime }?.sumBy { it.time }?:0)
//                    flight.sumGroundTime = (flight.times?.filter { !it.addToFlightTime }?.sumBy { it.time }?:0)
                    flight
                }
    }

    fun getFilterflightsObs(): Observable<List<Flight>> {
        val orderType = repository.getPrefInt(CONSTS.PREFS.PREF_USER_FILTER_FLIGHTS)
        return fromCallable {
            val order = getPrefOrderflights(orderType)
            val flightTypes = repository.loadDBFlightTypes()
            val planeTypes = repository.loadPlaneTypes()
            val flights = repository.getDbFlights(order)
            flights.map { it.toFlight() }
                    .map { flight ->
                        val planeTypeEntity = planeTypes.find { it.typeId == flight.planeId }
                        flight.planeType = planeTypeEntity?.toPlaneType()
                        flight.flightType = flightTypes.find { it.id == flight.flightTypeId?.toLong() }?.toFlightType()
//                        flight.sumFlightTime = (flight.flightTime?:0) + (flight.times?.filter { it.addToFlightTime }?.sumBy { it.time }?:0)
//                        flight.sumGroundTime = (flight.times?.filter { !it.addToFlightTime }?.sumBy { it.time }?:0)
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
        return fromCallable { repository.removeFlight(id) }
    }

    fun readExcelFile(path: String?, fromSystem: Boolean, onProgress: (state: String, iter: Int, total: Int) -> Unit): Boolean {
        val ctx = repository.getContext()
        val saving = repository.getString(R.string.saving)
        val loadingFile = repository.getString(R.string.loading_file)
        val fileImport = repository.getString(R.string.importing_file)
        onProgress.invoke(loadingFile, 0, 100)
        val fileUri = Uri.fromFile(File(path))
        val filename = FileUtils.getSDFilePath(ctx, fileUri)
        var mIsSuccess = false
        Log.i(this::class.java.simpleName, "readExcelFile:fileUri:$fileUri, filename:$filename");
        val myWorkBook: HSSFWorkbook
        val xlsfile: File
        val notAccess = !FileUtils.isExternalStorageAvailable() || FileUtils.isExternalStorageReadOnly()
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
        val numberOfRows = mySheet.getPhysicalNumberOfRows()
        val rowIter = mySheet.rowIterator()
        repository.removeAllFlights()
        repository.runSQl("UPDATE sqlite_sequence SET seq = (SELECT count(*) FROM main_table) WHERE name='main_table'", false)
        onProgress.invoke(loadingFile, 100, 100)
        val flightsFromExel = getFlightsFromExcel(rowIter) {
            val percent = MathUtils.getPercent(it.toDouble(), numberOfRows.toDouble()).toInt()
            onProgress.invoke(fileImport, percent, 100)
        }
        onProgress.invoke(saving, 50, 100)
        repository.insertFlights(flightsFromExel.map { it.toFlightEntity() })
        onProgress.invoke(saving, 100, 100)
        mIsSuccess = true
        return mIsSuccess
    }

    private fun getFlightsFromExcel(rowIter: Iterator<*>, onProgress: (iter: Int) -> Unit): ArrayList<Flight> {
        val flights = ArrayList<Flight>()
        var rowCnt = 0
        var strDate: String? = null
        var strTime: String? = null
        var airplane_type: String? = null
        var reg_no: String? = null
        var strDesc: String
        var airplane_type_id: Long = 0
        var flight_type: Long = 0
        var logTime = 0
        var mDateTime: Long = 0
        var planeTypes = repository.loadPlaneTypes()
        var flightTypes = repository.loadDBFlightTypes()
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
                                strDate = DateTimeUtils.getDateTime(System.currentTimeMillis(), "dd MMM yyyy")
                                e.printStackTrace()
                            }
                        }
                        2 -> {
                            try {
                                strTime = if (myCell.cellType == Cell.CELL_TYPE_NUMERIC) {
                                    Utility.match(myCell.dateCellValue.toString(), "(\\d{2}:\\d{2})", 1)
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
                                    airplane_type_id = planeType.typeId
                                } else {
                                    if (!airplane_type.isNullOrBlank()) {
                                        airplane_type_id = repository.addTypeAndGet(airplane_type)
                                        planeTypes = repository.loadPlaneTypes()
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
                                flight_type = if (fTypeStr.contains(".")) fTypeStr.parseDouble()?.toLong()
                                        ?: -1 else fTypeStr.parseLong() ?: -1
                                val planeType = flightTypes.find { it.id == flight_type }
                                if (planeType != null) {
                                    airplane_type_id = planeType.id ?: -1
                                } else {
                                    if (flight_type != -1L) {
                                        val type = getOldFlightType(flight_type)
                                        val flightTypeEntity = flightTypes.find { it.typeTitle == type }
                                        if (flightTypeEntity != null) {
                                            airplane_type_id = flightTypeEntity.id ?: -1
                                        } else {
                                            flight_type = repository.addFlightTypeAndGet(type)
                                            flightTypes = repository.loadDBFlightTypes()
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
                        strDate = strDate!!.replace("-", " ").replace(".", " ").replace("\\s+".toRegex(), " ")
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
            0L -> repository.getString(R.string.flight_type_circle)
            1L -> repository.getString(R.string.flight_type_zone)
            2L -> repository.getString(R.string.flight_type_route)
            else -> ""
        }
    }
}