package com.arny.domain.flights

import android.content.Context
import android.graphics.Color
import android.net.Uri
import com.arny.constants.CONSTS
import com.arny.constants.CONSTS.STRINGS.PARAM_COLOR
import com.arny.domain.R
import com.arny.domain.common.PreferencesProvider
import com.arny.domain.common.ResourcesProvider
import com.arny.domain.flighttypes.FlightTypesRepository
import com.arny.domain.models.*
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
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.ArrayList

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

    fun loadPlaneType(id: Long?): PlaneType? {
        return planeTypesRepository.loadPlaneType(id)
    }

    fun loadFlightType(id: Long?): FlightType? {
        return flightTypesRepository.loadDBFlightType(id)
    }

    private fun getFormattedFlightTimes(): Result<String> {
        val flightsCount = flightsRepository.getFlightsCount()
        if (flightsCount == 0) {
            return BusinessException("Flights not found").toResult()
        }
        val flightsTime = flightsRepository.getFlightsTime()
        val groundTime = flightsRepository.getGroundTime()
        val nightTime = flightsRepository.getNightTime()
        val sumlogTime = flightsTime + groundTime
        return formattedInfo(flightsTime, nightTime, groundTime, sumlogTime, flightsCount)
    }

    private fun formattedInfo(flightsTime: Int, nightTime: Int, groundTime: Int, sumlogTime: Int, flightsCount: Int): Result<String> {
        return String.format("%s %s\n%s %s\n%s %s\n%s %s\n%s %d",
                resourcesProvider.getString(R.string.str_total_flight_time),
                DateTimeUtils.strLogTime(flightsTime),
                resourcesProvider.getString(R.string.stat_total_night_time),
                DateTimeUtils.strLogTime(nightTime),
                resourcesProvider.getString(R.string.cell_ground_time) + ":",
                DateTimeUtils.strLogTime(groundTime),
                resourcesProvider.getString(R.string.str_total_time),
                DateTimeUtils.strLogTime(sumlogTime),
                resourcesProvider.getString(R.string.total_records),
                flightsCount).toResult()
    }

    fun getTotalflightsTimeInfo(): Observable<Result<String>> {
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
                    flight.totalTime = flight.flightTime + flight.groundTime
                    flight
                }
    }

    fun exportFile(): Observable<Result<String>> {
        return getDbFlightsObs()
                .map { result ->
                    var resultPath = ""
                    if (result is Result.Success) {
                        saveExcelFile(result.data)?.let {
                            resultPath = it
                        }
                    }
                    resultPath.toResult()
                }
    }

    private fun getDbFlightsObs(checkAutoExport: Boolean = false): Observable<Result<List<Flight>>> {
        return fromCallable {
            flightsRepository.getDbFlights(
                    getPrefOrderflights(preferencesProvider.getPrefInt(CONSTS.PREFS.PREF_USER_FILTER_FLIGHTS))
            )
        }.doOnNext {
            if (checkAutoExport && preferencesProvider.getPrefBoolean(CONSTS.PREFS.AUTO_EXPORT_XLS, false)) {
                if (it is Result.Success) {
                    saveExcelFile(it.data)
                }
            }
        }
    }

    fun getFilterFlightsObs(checkAutoExport: Boolean = false): Observable<Result<List<Flight>>> {
        val orderType = preferencesProvider.getPrefInt(CONSTS.PREFS.PREF_USER_FILTER_FLIGHTS)
        return getDbFlightsObs(checkAutoExport)
                .flatMap { flightsResult ->
                    fromCallable {
                        val flightTypes = flightTypesRepository.loadDBFlightTypes()
                        val planeTypes = planeTypesRepository.loadPlaneTypes()
                        when (flightsResult) {
                            is Result.Success -> {
                                flightsResult.data.map { flight ->
                                    flight.colorInt = flight.params?.getParam(PARAM_COLOR, "")?.toIntColor()
                                    val masked = flight.colorInt?.let { colorWillBeMasked(it) }
                                            ?: false
                                    flight.colorText = if (masked) Color.WHITE else null
                                    flight.planeType = planeTypes.find { it.typeId == flight.planeId }
                                    flight.flightType = flightTypes.find { it.id == flight.flightTypeId?.toLong() }
                                    flight.totalTime = flight.flightTime + flight.groundTime
                                    flight
                                }
                            }
                            is Result.Error -> throw BusinessException(flightsResult.exception)
                        }
                    }
                }
                .map { flights ->
                    val res = when (orderType) {
                        0 -> flights.sortedBy { it.datetime }
                        1 -> flights.sortedByDescending { it.datetime }
                        2 -> flights.sortedBy { it.flightTime }
                        3 -> flights.sortedByDescending { it.flightTime }
                        else -> flights
                    }
                    res.toResult()
                }
    }

    fun removeFlight(id: Long?): Observable<Boolean> {
        return fromCallable { flightsRepository.removeFlight(id) }
    }

    fun readExcelFile(uri: Uri?, fromSystem: Boolean): String? {
        val ctx = resourcesProvider.provideContext()
        val filename: String = if (fromSystem)
            getDefaultFilePath(ctx)
        else
            FileUtils.getSDFilePath(ctx, uri)
        if (!FileUtils.isExternalStorageAvailable() || FileUtils.isExternalStorageReadOnly()) {
            return null
        }
        val xlsfile = File(filename)
        if (!xlsfile.isFile || !xlsfile.exists()) {
            throw BusinessException(String.format(
                    Locale.getDefault(), getString(R.string.error_file_not_found), filename
            ))
        }
        val fileInputStream = FileInputStream(xlsfile)
        val myWorkBook = HSSFWorkbook(fileInputStream)
        val mySheet = myWorkBook.getSheetAt(0)
        val rowIter = mySheet.rowIterator()
        flightsRepository.removeAllFlights()
        flightsRepository.resetTableFlights()
        return if (flightsRepository.insertFlights(getFlightsFromExcel(rowIter))) filename else null
    }

    private fun getFlightsFromExcel(rowIter: Iterator<*>): ArrayList<Flight> {
        val flights = ArrayList<Flight>()
        var rowCnt = 0
        var strDate: String? = null
        var strDesc: String
        var airplaneTypeId: Long = 0
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
                    var hasBlankLine = false
                    when (cellCnt) {
                        1 -> {
                            strDate = try {
                                val cellValue = myCell.toString()
                                hasBlankLine = cellValue.isBlank()
                                if (hasBlankLine) {
                                    defaultCurrentTime()
                                } else {
                                    cellValue
                                }
                            } catch (e: Exception) {
                                defaultCurrentTime()
                            }
                        }
                        2 -> {
                            var timeStr: String
                            var time = 0
                            try {
                                timeStr = if (myCell.cellType == Cell.CELL_TYPE_NUMERIC) {
                                    Utility.match(
                                            myCell.dateCellValue.toString(),
                                            "(\\d{2}:\\d{2})", 1
                                    )
                                } else {
                                    myCell.toString()
                                }
                            } catch (e: Exception) {
                                timeStr = "00:00"
                                e.printStackTrace()
                            }
                            try {
                                time = DateTimeUtils.convertStringToTime(timeStr)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                            flight.flightTime = time
                        }
                        3 -> {
                            try {
                                val airplaneTypeName = myCell.toString()
                                val planeType = planeTypes.find { it.typeName == airplaneTypeName }
                                if (planeType != null) {
                                    airplaneTypeId = planeType.typeId
                                } else {
                                    if (!airplaneTypeName.isBlank()) {
                                        airplaneTypeId =
                                                planeTypesRepository.addTypeAndGet(airplaneTypeName)
                                        planeTypes = planeTypesRepository.loadPlaneTypes()
                                    }
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                airplaneTypeId = 0
                            }
                            flight.planeId = airplaneTypeId
                        }
                        4 -> {
                            var regNo: String
                            try {
                                regNo = myCell.toString()
                            } catch (e: Exception) {
                                regNo = ""
                                e.printStackTrace()
                            }
                            flight.regNo = regNo
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
                                                    flightTypesRepository.addFlightTypeAndGet(
                                                            oldTypeName
                                                    )
                                            dbFlightTypes =
                                                    flightTypesRepository.loadDBFlightTypes()
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                flightTypeId = -1
                                e.printStackTrace()
                            }
                            flight.flightTypeId = flightTypeId.toInt()
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
                            var timeStr: String
                            var time = 0
                            try {
                                timeStr = if (myCell.cellType == Cell.CELL_TYPE_NUMERIC) {
                                    Utility.match(
                                            myCell.dateCellValue.toString(),
                                            "(\\d{2}:\\d{2})", 1
                                    )
                                } else {
                                    myCell.toString()
                                }
                            } catch (e: Exception) {
                                timeStr = "00:00"
                                e.printStackTrace()
                            }
                            try {
                                time = DateTimeUtils.convertStringToTime(timeStr)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                            flight.nightTime = time
                        }
                        10 -> {
                            var timeStr: String
                            var time = 0
                            try {
                                timeStr = if (myCell.cellType == Cell.CELL_TYPE_NUMERIC) {
                                    Utility.match(
                                            myCell.dateCellValue.toString(),
                                            "(\\d{2}:\\d{2})", 1
                                    )
                                } else {
                                    myCell.toString()
                                }
                            } catch (e: Exception) {
                                timeStr = "00:00"
                                e.printStackTrace()
                            }
                            try {
                                time = DateTimeUtils.convertStringToTime(timeStr)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                            flight.groundTime = time
                        }
                    }
                    if (hasBlankLine) {
                        break
                    }
                    if (lastCell == cellCnt) {
                        var format = "dd MMM yyyy"
                        strDate = strDate!!.replace("-", " ").replace(".", " ")
                                .replace("\\s+".toRegex(), " ")
                        strDate = DateTimeUtils.convertStrMonthToNum(strDate) ?: strDate
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
                }
                cellCnt++
            }
            rowCnt++
        }
        return flights
    }

    private fun defaultCurrentTime(): String? {
        return DateTimeUtils.getDateTime(
                System.currentTimeMillis(),
                "dd MMM yyyy"
        )
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
            0L -> getString(R.string.flight_type_circle)
            1L -> getString(R.string.flight_type_zone)
            2L -> getString(R.string.flight_type_route)
            else -> ""
        }
    }

    private fun getString(strRes: Int): String {
        return resourcesProvider.getString(strRes)
    }

    private fun saveExcelFile(dbFlights: List<Flight>): String? {
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
        c.setCellValue(getString(R.string.cell_night_time))
        c = row.createCell(9)
        c.setCellValue(getString(R.string.cell_ground_time))
        val exportData = dbFlights
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
            c.setCellValue(flight.nightTime.toDouble())
            c = row.createCell(9)
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
        mainSheet.setColumnWidth(8, 15 * 250)
        mainSheet.setColumnWidth(9, 15 * 300)
        val file = File(getDefaultFilePath(resourcesProvider.provideContext()))
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

    fun getFileData(): String? {
        val context = resourcesProvider.provideContext()
        val file = File(getDefaultFilePath(context))
        return if (file.isFile && file.exists()) {
            StringBuilder()
                    .apply {
                        append(getString(R.string.file_name))
                        append(file.path)
                        append(",\n")
                        append(getString(R.string.file_size))
                        append(FileUtils.formatFileSize(file.length()))
                        append(",\n")
                        append(getString(R.string.file_last_modify))
                        append(DateTimeUtils.getDateTime(Date(file.lastModified()), "dd MM yyyy HH:mm:ss"))
                    }.toString()
        } else {
            null
        }
    }

    private fun getDefaultFilePath(context: Context) =
            FileUtils.getWorkDir(context) + File.separator + CONSTS.FILES.EXEL_FILE_NAME

    fun getDefaultFileUri(): Uri? {
        val context = resourcesProvider.provideContext()
        return FileUtils.getFileUri(context, File(getDefaultFilePath(context)))
    }
}
