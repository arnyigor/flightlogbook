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
class FlightsUseCase @Inject constructor(private val repository: MainRepositoryImpl) {

    fun insertFlights(flights: List<Flight>): Observable<Boolean> {
        return fromCallable { repository.insertFlights(flights.map { it.toFlightEntity() }) }
    }

    fun removeAllFlightsObs(): Observable<Boolean> {
        return fromCallable { repository.removeAllFlights() }
    }

    fun removeAllFlights(): Boolean {
        return repository.removeAllFlights()
    }

    fun updateFlight(flight: Flight, flightTimes: ArrayList<TimeToFlight>?): Observable<Boolean> {
        return fromCallable { repository.updateFlight(flight.toFlightEntity()) }
                .map { update ->
                    if (update) {
                        val flightId = flight.id
                        val dbFlightTimes =  repository.queryDBFlightTimes(flightId).map { it.toTimeFlight() }.toMutableList()
                        if (!flightTimes.isNullOrEmpty()) {
                            for (timeToFlight in flightTimes) {
                                val toFlight = dbFlightTimes.find { it._id == timeToFlight._id }
                                if (toFlight != null) {
                                    dbFlightTimes.remove(toFlight)
                                    if (toFlight!=timeToFlight) {
                                        repository.updateDBFlightTime(flightId, timeToFlight.timeTypeId, timeToFlight.time, timeToFlight.addToFlightTime)
                                    }
                                }else{
                                    repository.insertDBFlightTime(timeToFlight.toTimeEntity())
                                }
                            }
                            for (dbFlightTime in dbFlightTimes) {
                                repository.deleteDBFlightTimesByTime(dbFlightTime._id)
                            }
                        }else{
                            repository.deleteDBFlightTimesByFlight(flightId)
                        }
                    }
                    update
                }
    }

    fun insertFlightAndGet(flight: Flight, flightTimes: ArrayList<TimeToFlight>?): Observable<Boolean> {
        return fromCallable { repository.insertFlightAndGet(flight.toFlightEntity()) }
                .flatMap { id ->
                    if (id > 0) {
                        if (flightTimes != null && flightTimes.isNotEmpty()) {
                            flightTimes.map { it.flight = id }
                            insertDBFlightToTimes(flightTimes)
                        } else {
                            fromCallable { true }
                        }
                    } else {
                        fromCallable { false }
                    }
                }
    }

    fun getFlight(id: Long?): Observable<OptionalNull<Flight?>> {
        return fromNullable { repository.getFlight(id)?.toFlight() }
                .map { nullable->
                    val flight = nullable.value
                    val planeType = repository.loadPlaneType(flight?.planeId)
                    flight?.planeType = planeType?.toPlaneType()
                    flight?.airplanetypetitle = planeType?.typeName
                    flight?.times= repository.queryDBFlightTimes(flight?.id).map { it.toTimeFlight() }
                    flight?.flightType= repository.loadDBFlightType(flight?.flightTypeId?.toLong())?.toFlightType()
                    flight?.sumlogTime = (flight?.logtime?:0) + (flight?.times?.sumBy { it.time }?:0)
                    flight?.sumFlightTime = (flight?.logtime?:0) + (flight?.times?.filter { it.addToFlightTime }?.sumBy { it.time }?:0)
                    flight?.sumGroundTime =  (flight?.times?.filter { !it.addToFlightTime }?.sumBy { it.time }?:0)
                    nullable
                }
    }

    fun loadPlaneTypes(): Observable<List<PlaneType>> {
        return fromCallable { repository.loadPlaneTypes().map { it.toPlaneType() } }
    }

    fun loadPlaneTypeObs(id: Long?): Observable<OptionalNull<PlaneType?>> {
        return fromNullable { repository.loadPlaneType(id)?.toPlaneType() }
    }

    fun loadPlaneType(id: Long?): PlaneType? {
        return repository.loadPlaneType(id)?.toPlaneType()
    }

    fun addPlaneType(name: String ): Boolean {
        return repository.addType(name)
    }

    fun addPlaneTypeAndGet(name: String ): Long {
        return repository.addTypeAndGet(name)
    }

    fun loadPlaneType(title: String?): PlaneType? {
        return repository.loadPlaneType(title)?.toPlaneType()
    }

    fun loadFlightType(id: Long?): Observable<OptionalNull<FlightType?>> {
        return fromNullable { repository.loadDBFlightType(id)?.toFlightType() }
    }

    fun loadDBFlightsToTimes(): Observable<List<TimeToFlight>> {
        return fromCallable { repository.queryDBFlightsTimes().map { it.toTimeFlight() } }
    }

    fun loadDBFlightToTimes(flightId: Long?): Observable<List<TimeToFlight>> {
        return fromCallable { repository.queryDBFlightTimes(flightId).map {
            it.timeTypeEntity = it.timeType?.let { it1 -> repository.queryDBTimeType(it1) }
            it.toTimeFlight()
        } }
    }

    fun insertDBFlightToTime(entity: TimeToFlight): Observable<Boolean> {
        return fromCallable { repository.insertDBFlightTime(entity.toTimeEntity()) }
    }

    fun insertDBFlightToTimes(entities: List<TimeToFlight>): Observable<Boolean> {
        return fromCallable { repository.insertDBFlightTimes(entities.map { it.toTimeEntity() }) }
    }

    fun updateDBFlightToTime(flightId: Long?, timeType: Long?, time: Int, addToFlight: Boolean = false): Observable<Boolean> {
        return fromCallable { repository.updateDBFlightTime(flightId, timeType, time, addToFlight) }
    }

    fun removeDBFlightToTime(flightId: Long?, timeType: Long?): Observable<Boolean> {
        return fromCallable { repository.removeDBFlightTime(flightId, timeType) }
    }

    private fun getFormattedFlightTimes(): String {
        val flightsTime = repository.getFlightsTime()
        val totalTimes = repository.queryDBFlightsTimesSum()
        val totalFlightTimes = repository.queryDBFlightsTimesSum(true)
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
                    flight.airplanetypetitle = planeType?.typeName
                    flight.times= repository.queryDBFlightTimes(flight.id).map { it.toTimeFlight() }
                    flight.flightType= repository.loadDBFlightType(flight.flightTypeId?.toLong())?.toFlightType()
                    flight.sumlogTime = (flight.logtime?:0) + (flight.times?.sumBy { it.time }?:0)
                    flight.sumFlightTime = (flight.logtime?:0) + (flight.times?.filter { it.addToFlightTime }?.sumBy { it.time }?:0)
                    flight.sumGroundTime = (flight.times?.filter { !it.addToFlightTime }?.sumBy { it.time }?:0)
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
            val times = repository.queryDBFlightsTimes()
            flights.map { it.toFlight() }
                    .map { flight ->
                        flight.airplanetypetitle = planeTypes.find { it.typeId == flight.planeId }?.typeName
                        flight.times = times.filter { it.flight == flight.id }.map { it.toTimeFlight() }
                        flight.flightType = flightTypes.find { it.id == flight.flightTypeId?.toLong() }?.toFlightType()
                        flight.sumlogTime = (flight.logtime?:0) + (flight.times?.sumBy { it.time }?:0)
                        flight.sumFlightTime = (flight.logtime?:0) + (flight.times?.filter { it.addToFlightTime }?.sumBy { it.time }?:0)
                        flight.sumGroundTime = (flight.times?.filter { !it.addToFlightTime }?.sumBy { it.time }?:0)
                        flight
                    }
        }.map { flights ->
            val res = when (orderType) {
                0 -> flights.sortedBy { it.datetime}
                1 -> flights.sortedByDescending { it.datetime}
                2 -> flights.sortedBy { it.sumlogTime}
                3 -> flights.sortedByDescending { it.sumlogTime}
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
        var logTime: Int = 0
        var mDateTime: Long = 0
        val dbTimeTypes = repository.queryDBTimeTypes()
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
                            flight.logtime = logTime
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
                            flight.reg_no = reg_no
                        }
                        7 -> {
                            try {
                                val fTypeStr = myCell.toString()
                                flight_type = if(fTypeStr.contains(".")) fTypeStr.parseDouble()?.toLong()?:-1 else fTypeStr.parseLong() ?: -1
                                val planeType = flightTypes.find { it.id == flight_type }
                                if (planeType != null) {
                                    airplane_type_id = planeType.id?:-1
                                } else {
                                    if (flight_type!=-1L) {
                                        val type = getOldFlightType(flight_type)
                                        val flightTypeEntity = flightTypes.find { it.typeTitle == type }
                                        if (flightTypeEntity != null) {
                                            airplane_type_id = flightTypeEntity.id?:-1
                                        }else{
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
                            try {
                                val additions = cell//"Смена:00:45;Ночь:00:15"
                                val split = additions.split(";")
                                val times = arrayListOf<TimeToFlight>()
                                for (typeTime in split) {
                                    val regex = "^(\\W+)[:|-](.*)\$".toRegex()
                                    val typeAndTime = regex.matchEntire(typeTime)?.groupValues
                                    if (typeAndTime != null) {
                                        val type = typeAndTime.getOrNull(0)
                                        val time = typeAndTime.getOrNull(1)
                                        if (type != null && time != null) {
                                            val typeEntity = dbTimeTypes.find { it.title == type }
                                            if (typeEntity != null) {
                                                val timeToFlight = TimeToFlight()
                                                val addTime = DateTimeUtils.convertStringToTime(time)
                                                timeToFlight.timeTypeId = typeEntity.id
                                                timeToFlight.timeType = typeEntity.toTimeType()
                                                timeToFlight
                                            } else {
                                                val timeId = repository.addDBTimeTypeAndGet(time)
                                            }
                                        }
                                    }
                                }
                                flight.times = times
                            } catch (e: Exception) {
                                e.printStackTrace()
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