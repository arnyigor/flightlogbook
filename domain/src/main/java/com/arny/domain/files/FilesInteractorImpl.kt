package com.arny.domain.files

import android.content.Context
import android.net.Uri
import com.arny.core.CONSTS
import com.arny.core.CONSTS.FLIGHT.TYPE_CIRCLE
import com.arny.core.CONSTS.FLIGHT.TYPE_RUOTE
import com.arny.core.CONSTS.FLIGHT.TYPE_ZONE
import com.arny.core.utils.*
import com.arny.domain.R
import com.arny.domain.flights.FlightsRepository
import com.arny.domain.flighttypes.FlightTypesRepository
import com.arny.domain.models.*
import com.arny.domain.planetypes.AircraftTypesRepository
import io.reactivex.Observable
import org.apache.poi.hssf.usermodel.HSSFCell
import org.apache.poi.hssf.usermodel.HSSFRow
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Cell
import java.io.File
import java.io.FileInputStream
import java.util.*
import javax.inject.Inject

class FilesInteractorImpl @Inject constructor(
        private val context: Context,
        private val flightsRepository: FlightsRepository,
        private val aircraftTypesRepository: AircraftTypesRepository,
        private val filesRepository: FilesRepository,
        private val flightTypesRepository: FlightTypesRepository
) : FilesInteractor {

    override fun readExcelFile(uri: Uri?, fromSystem: Boolean): String? {
        if (!FileUtils.isExternalStorageAvailable() || FileUtils.isExternalStorageReadOnly()) {
            return null
        }
        val filename: String = getFileName(fromSystem, uri)
        val xlsfile = File(filename)
        if (!xlsfile.isFile || !xlsfile.exists()) {
            throw BusinessException(String.format(
                    Locale.getDefault(), context.getString(R.string.error_file_not_found), filename
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

    private fun getFileName(fromSystem: Boolean, uri: Uri?): String {
        return if (fromSystem)
            getDefaultFilePath()
        else
            FilePathUtils.getPath(uri, context).toString()
    }

    private fun getFlightsFromExcel(rowIter: Iterator<*>): ArrayList<Flight> {
        val flights = ArrayList<Flight>()
        var rowCnt = 0
        var strDate: String? = null
        var strDesc: String
        var airplaneTypeId: Long = 0
        var mDateTime: Long = 0
        var planeType: PlaneType? = null
        var planeTypes = aircraftTypesRepository.loadAircraftTypes()
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
                            val airplaneTypeName = myCell.toString()
                            val type = planeTypes.find { it.typeName.equals(airplaneTypeName, true) }
                            if (type?.typeId == null && airplaneTypeName.isNotBlank()) {
                                planeType = PlaneType().apply {
                                    typeName = airplaneTypeName
                                }
                            } else {
                                planeType = type
                            }
                        }
                        4 -> {
                            val regNo: String = myCell.toString()
                            if (planeType?.typeId != null && !planeType.regNo.isNullOrBlank() && regNo == planeType.regNo) {
                                flight.planeId = planeType.typeId
                                flight.regNo = planeType.regNo
                            } else {
                                val type = planeTypes.find { it.regNo == regNo }
                                if (type?.typeId != null) {
                                    planeType = type
                                } else {
                                    if (planeType != null) {
                                        val typeName = planeType.typeName
                                        planeType = PlaneType().apply {
                                            this.typeName = typeName
                                            this.regNo = regNo
                                        }
                                        airplaneTypeId = aircraftTypesRepository.addType(planeType)
                                        planeType.typeId = airplaneTypeId
                                        planeTypes = aircraftTypesRepository.loadAircraftTypes()
                                    } else {
                                        planeType = PlaneType().apply {
                                            this.regNo = regNo
                                        }
                                        airplaneTypeId = aircraftTypesRepository.addType(planeType)
                                        planeType.typeId = airplaneTypeId
                                        planeTypes = aircraftTypesRepository.loadAircraftTypes()
                                    }
                                }
                                if (planeType.typeId != null && !planeType.regNo.isNullOrBlank() && regNo ==
                                        planeType.regNo) {
                                    flight.planeId = planeType.typeId
                                    flight.regNo = planeType.regNo
                                }
                            }
                        }
                        7 -> {
                            val fTypeStr = myCell.toString()
                            var flightTypeId = getFlightTypeId(fTypeStr)
                            val dbFlightType = dbFlightTypes.find { it.id == flightTypeId }
                            if (dbFlightType != null) {
                                flightTypeId = dbFlightType.id ?: -1
                            } else {
                                if (flightTypeId != -1L) {
                                    val oldTypeName = getOldFlightType(flightTypeId)
                                    val oldFlightType = dbFlightTypes.find { it.typeTitle == oldTypeName }
                                    if (oldFlightType != null) {
                                        flightTypeId = oldFlightType.id ?: -1
                                    } else {
                                        flightTypeId = flightTypesRepository.addFlightTypeAndGet(FlightType(flightTypeId, oldTypeName))
                                        dbFlightTypes = flightTypesRepository.loadDBFlightTypes()
                                    }
                                }
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

    private fun getOldFlightType(type: Long): String {
        return when (type) {
            TYPE_CIRCLE -> context.getString(R.string.flight_type_circle)
            TYPE_ZONE -> context.getString(R.string.flight_type_zone)
            TYPE_RUOTE -> context.getString(R.string.flight_type_route)
            else -> ""
        }
    }

    override fun exportFile(): Observable<Result<String>> {
        return flightsRepository.getDbFlightsOrdered()
                .map { result ->
                    var resultPath = ""
                    if (result is Result.Success) {
                        filesRepository.saveExcelFile(result.data)?.let {
                            resultPath = it
                        }
                    }
                    resultPath.toResult()
                }
    }

    override fun getDefaultFileUri(): Uri? {
        return FileUtils.getFileUri(context, File(getDefaultFilePath()))
    }

    override fun getDefaultFilePath() =
            FileUtils.getWorkDir(context) + File.separator + CONSTS.FILES.EXEL_FILE_NAME

    override fun getFileData(): String? {
        val file = File(getDefaultFilePath())
        return if (file.isFile && file.exists()) {
            StringBuilder()
                    .apply {
                        append(context.getString(R.string.file_name))
                        append(file.path)
                        append(",\n")
                        append(context.getString(R.string.file_size))
                        append(FileUtils.formatFileSize(file.length()))
                        append(",\n")
                        append(context.getString(R.string.file_last_modify))
                        append(DateTimeUtils.getDateTime(Date(file.lastModified()), "dd.MM.yyyy HH:mm:ss"))
                    }.toString()
        } else {
            null
        }
    }
}