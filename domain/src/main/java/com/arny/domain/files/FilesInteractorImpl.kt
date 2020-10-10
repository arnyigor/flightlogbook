package com.arny.domain.files

import android.content.Context
import android.net.Uri
import com.arny.domain.R
import com.arny.domain.flights.FlightsRepository
import com.arny.domain.flighttypes.FlightTypesRepository
import com.arny.domain.models.*
import com.arny.domain.planetypes.PlaneTypesRepository
import com.arny.flightlogbook.constants.CONSTS
import com.arny.flightlogbook.constants.CONSTS.FLIGHT.TYPE_CIRCLE
import com.arny.flightlogbook.constants.CONSTS.FLIGHT.TYPE_RUOTE
import com.arny.flightlogbook.constants.CONSTS.FLIGHT.TYPE_ZONE
import com.arny.helpers.utils.*
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
        private val planeTypesRepository: PlaneTypesRepository,
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
            getDefaultFilePath(context)
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
        var planeType = PlaneType()
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
                                val typeId = planeTypes.find { it.typeName == airplaneTypeName }?.typeId
                                if (typeId != null) {
                                    airplaneTypeId = typeId
                                } else {
                                    if (!airplaneTypeName.isBlank()) {
                                        planeType.typeName = airplaneTypeName
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
                            planeType.regNo = regNo
                            airplaneTypeId =  planeTypesRepository.addType(planeType)
                            planeTypes = planeTypesRepository.loadPlaneTypes()
                            flight.planeId = airplaneTypeId
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
        return FileUtils.getFileUri(context, File(getDefaultFilePath(context)))
    }

    private fun getDefaultFilePath(context: Context) =
            FileUtils.getWorkDir(context) + File.separator + CONSTS.FILES.EXEL_FILE_NAME

    override fun getFileData(): String? {
        val file = File(getDefaultFilePath(context))
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