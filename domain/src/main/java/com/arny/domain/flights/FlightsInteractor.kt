package com.arny.domain.flights

import android.content.ContentResolver
import android.graphics.Color
import android.net.Uri
import android.provider.OpenableColumns
import com.arny.constants.CONSTS
import com.arny.constants.CONSTS.STRINGS.PARAM_COLOR
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
import org.apache.poi.ss.usermodel.Row
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
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
    companion object {
        private const val LOG_SHEET_MAIN = "Timelog"
    }

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
                    colorInt = params?.getParam(PARAM_COLOR, "")?.toIntColor()
                    planeType = planeTypesRepository.loadPlaneType(planeId)
                    flightType = flightTypesRepository.loadDBFlightType(flightTypeId?.toLong())
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
                    flight.flightType = flightTypesRepository.loadDBFlightType(flight.flightTypeId?.toLong())
                    flight.totalTime = flight.flightTime + flight.groundTime
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
                flight.colorInt = flight.params?.getParam(PARAM_COLOR, "")?.toIntColor()
                flight.colorInt?.let { colorWillBeMasked(it) }?.takeIf { true }?.let {
                    flight.colorText = Color.WHITE
                }
                flight.planeType = planeTypes.find { it.typeId == flight.planeId }
                flight.flightType = flightTypes.find { it.id == flight.flightTypeId?.toLong() }
                flight.totalTime = flight.flightTime + flight.groundTime
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

    fun ContentResolver.getFileName(fileUri: Uri): String {
        var name = ""
        val returnCursor = this.query(fileUri, null, null, null, null)
        if (returnCursor != null) {
            val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            returnCursor.moveToFirst()
            name = returnCursor.getString(nameIndex)
            returnCursor.close()
        }
        return name
    }

    fun readExcelFile(uri: Uri, fromSystem: Boolean): String? {
        val ctx = resourcesProvider.provideContext()
        val filename = FileUtils.getSDFilePath(ctx, uri)
        val myWorkBook: HSSFWorkbook
        val xlsfile: File
        val notAccess = !FileUtils.isExternalStorageAvailable() || FileUtils.isExternalStorageReadOnly()
        if (notAccess) {
            return null
        }
        xlsfile = if (fromSystem) {
            File(ctx.getExternalFilesDir(null), filename)
        } else {
            File("", filename)
        }
        val fileInputStream = FileInputStream(xlsfile)
        myWorkBook = HSSFWorkbook(fileInputStream)
        val mySheet = myWorkBook.getSheetAt(0)
        val rowIter = mySheet.rowIterator()
        flightsRepository.removeAllFlights()
        flightsRepository.resetTableFlights()
        val insertFlights = flightsRepository.insertFlights(getFlightsFromExcel(rowIter))
        return if (insertFlights) filename else null
    }

    private fun getFlightsFromExcel(rowIter: Iterator<*>): ArrayList<Flight> {
        val flights = ArrayList<Flight>()
        var rowCnt = 0
        var strDate: String? = null
        var strTime: String?
        var airplane_type: String?
        var strDesc: String
        var airplane_type_id: Long = 0
        var logTime = 0
        var mDateTime: Long = 0
        var planeTypes = planeTypesRepository.loadPlaneTypes()
        var dbFlightTypes = flightTypesRepository.loadDBFlightTypes()
        var id = 1L
        while (rowIter.hasNext()) {
            val myRow = rowIter.next() as HSSFRow
            val lastCell = myRow.lastCellNum.toInt()
            val cellIter = myRow.cellIterator()
            var cellCnt = 1
            val flight = Flight(id)
            while (cellIter.hasNext()) {
                val myCell = cellIter.next() as HSSFCell
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
                            flight.regNo = getRegNo(myCell)
                        }
                        7 -> {
                            var flightTypeId: Long
                            try {
                                val fTypeStr = myCell.toString()
                                flightTypeId = getFlightTypeId(fTypeStr)
                                val dbFlightType = dbFlightTypes.find { it.id == flightTypeId }
                                if (dbFlightType != null) {
                                    flightTypeId = dbFlightType.id ?: -1
                                } else {
                                    if (flightTypeId != -1L) {
                                        val oldTypeName = getOldFlightType(flightTypeId)
                                        val oldFlightType =
                                                dbFlightTypes.find { it.typeTitle == oldTypeName }
                                        if (oldFlightType != null) {
                                            flightTypeId = oldFlightType.id ?: -1
                                        } else {
                                            flightTypeId =
                                                    flightTypesRepository.addFlightTypeAndGet(oldTypeName)
                                            dbFlightTypes = flightTypesRepository.loadDBFlightTypes()
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                flightTypeId = -1
                                e.printStackTrace()
                            }
                            val toInt = flightTypeId.toInt()
                            flight.flightTypeId = toInt
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
                            var title:String
                            try {
                                title = myCell.toString()
                            } catch (e: Exception) {
                                title = ""
                                e.printStackTrace()
                            }
                            flight.title = title

                        }
                        10 -> {
                            val cell = try {
                                myCell.toString()
                            } catch (e: Exception) {
                                ""
                            }
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
            rowCnt++
        }//while (rowIter.hasNext())
        return flights
    }

    private fun getRegNo(myCell: HSSFCell): String {
        var regNo = ""
        try {
            regNo = myCell.toString()
        } catch (e: Exception) {
            regNo = ""
            e.printStackTrace()
        }
        return regNo
    }

    private fun getFlightTypeId(fTypeStr: String): Long {
        return if (fTypeStr.contains(".")) {
            fTypeStr.parseDouble()?.toLong() ?: -1
        } else {
            fTypeStr.parseLong() ?: -1
        }
    }

    private fun getOldFlightType(flight_type: Long): String {
        return when (flight_type) {
            0L -> resourcesProvider.getString(R.string.flight_type_circle)
            1L -> resourcesProvider.getString(R.string.flight_type_zone)
            2L -> resourcesProvider.getString(R.string.flight_type_route)
            else -> ""
        }
    }


    private fun getString(strRes: Int): String {
        return resourcesProvider.getString(strRes)
    }

    fun saveExcelFile(): String? {
        var row: Row
        val wb = HSSFWorkbook()
        var c: Cell
        val mainSheet = wb.createSheet(LOG_SHEET_MAIN)
        row = mainSheet.createRow(0)
        c = row.createCell(0)
        c.setCellValue(getString(R.string.str_date))
        c = row.createCell(1)
        c.setCellValue(getString(R.string.str_itemlogtime))
        c = row.createCell(2)
        c.setCellValue(getString(R.string.str_type_null))
        c = row.createCell(3)
        c.setCellValue(getString(R.string.str_regnum))
        c = row.createCell(4)
        c.setCellValue(getString(R.string.str_day_night))
        c = row.createCell(5)
        c.setCellValue(getString(R.string.str_vfr_ifr))
        c = row.createCell(6)
        c.setCellValue(getString(R.string.str_flight_type))
        c = row.createCell(7)
        c.setCellValue(getString(R.string.str_desc))
        c = row.createCell(8)
        c.setCellValue(getString(R.string.str_flight_title))
        c = row.createCell(9)
        c.setCellValue(getString(R.string.cell_night_time))
        c = row.createCell(10)
        c.setCellValue(getString(R.string.cell_ground_time))
        val exportData = flightsRepository.getDbFlights()
                .map { flight ->
                    flight.planeType = planeTypesRepository.loadPlaneType(flight.planeId)
                    flight.flightType =
                            flightTypesRepository.loadDBFlightType(flight.flightTypeId?.toLong())
                    flight
                }
        var rows = 1
        for (flight in exportData) {
            val planeType = flight.planeType
            val airplaneType = if (planeType != null) planeType.typeName else ""
            row = mainSheet.createRow(rows)
            c = row.createCell(0)
            c.setCellValue(DateTimeUtils.getDateTime(flight.datetime!!, "dd MMM yyyy"))
            c = row.createCell(1)
            c.setCellValue(DateTimeUtils.strLogTime(flight.flightTime))
            c = row.createCell(2)
            c.setCellValue(airplaneType)
            c = row.createCell(3)
            c.setCellValue(flight.regNo)
            c = row.createCell(4)
            c.setCellValue(flight.daynight?.toDouble() ?: 0.0)
            c = row.createCell(5)
            c.setCellValue(flight.ifrvfr?.toDouble() ?: 0.0)
            c = row.createCell(6)
            c.setCellValue(flight.flightTypeId?.toDouble() ?: 0.0)
            c = row.createCell(7)
            c.setCellValue(flight.description)
            c = row.createCell(8)
            c.setCellValue(flight.title)
            c = row.createCell(9)
            c.setCellValue(flight.nightTime.toDouble())
            c = row.createCell(10)
            c.setCellValue(flight.groundTime.toDouble())
            rows++
        }

        mainSheet.setColumnWidth(0, 15 * 200)
        mainSheet.setColumnWidth(1, 15 * 150)
        mainSheet.setColumnWidth(2, 15 * 150)
        mainSheet.setColumnWidth(3, 15 * 150)
        mainSheet.setColumnWidth(4, 15 * 250)
        mainSheet.setColumnWidth(5, 15 * 300)
        mainSheet.setColumnWidth(6, 15 * 200)
        mainSheet.setColumnWidth(7, 15 * 500)
        mainSheet.setColumnWidth(8, 15 * 500)
        mainSheet.setColumnWidth(9, 15 * 200)
        mainSheet.setColumnWidth(10, 15 * 200)
        val file = File(
                resourcesProvider.provideContext().getExternalFilesDir(null),
                CONSTS.FILES.EXEL_FILE_NAME
        )
        var os: FileOutputStream? = null
        val success: Boolean
        try {
            os = FileOutputStream(file)
            wb.write(os)
            success = true
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        } finally {
            try {
                os?.close()
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
        if (success) {
            return file.path
        }
        return null
    }
}
