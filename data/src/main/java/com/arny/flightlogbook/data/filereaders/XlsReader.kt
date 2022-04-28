package com.arny.flightlogbook.data.filereaders

import com.arny.core.CONSTS
import com.arny.core.utils.DateTimeUtils
import com.arny.core.utils.Utility
import com.arny.core.utils.parseDouble
import com.arny.core.utils.parseLong
import com.arny.flightlogbook.domain.R
import com.arny.flightlogbook.domain.common.IResourceProvider
import com.arny.flightlogbook.domain.files.FlightFileReadWriter
import com.arny.flightlogbook.domain.flighttypes.FlightTypesRepository
import com.arny.flightlogbook.domain.models.Flight
import com.arny.flightlogbook.domain.models.FlightType
import com.arny.flightlogbook.domain.models.PlaneType
import com.arny.flightlogbook.domain.planetypes.AircraftTypesRepository
import org.apache.poi.hssf.usermodel.HSSFCell
import org.apache.poi.hssf.usermodel.HSSFRow
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.Row
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import javax.inject.Inject

class XlsReader @Inject constructor(
    private val resourcesProvider: IResourceProvider,
    private val flightTypesRepository: FlightTypesRepository,
    private val aircraftTypesRepository: AircraftTypesRepository,
) : FlightFileReadWriter {
    private companion object {
        const val LOG_SHEET_MAIN = "Timelog"
    }

    override fun readFile(file: File): List<Flight> {
        val rowIter = HSSFWorkbook(FileInputStream(file)).getSheetAt(0).rowIterator()
        val flights = ArrayList<Flight>()
        var rowCnt = 0
        var strDate: String? = null
        var strDesc: String
        var airplaneTypeId: Long
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
                                timeStr = if (myCell.cellType == CellType.NUMERIC) {
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
                            val type =
                                planeTypes.find { it.typeName.equals(airplaneTypeName, true) }
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
                                    planeType.regNo
                                ) {
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
                                    val oldFlightType =
                                        dbFlightTypes.find { it.typeTitle == oldTypeName }
                                    if (oldFlightType != null) {
                                        flightTypeId = oldFlightType.id ?: -1
                                    } else {
                                        flightTypeId = flightTypesRepository.addFlightTypeAndGet(
                                            FlightType(
                                                flightTypeId,
                                                oldTypeName
                                            )
                                        )
                                        dbFlightTypes = flightTypesRepository.loadDBFlightTypes()
                                    }
                                }
                            }
                            flight.flightTypeId = flightTypeId
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
                                timeStr = if (myCell.cellType == CellType.NUMERIC) {
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
                                timeStr = if (myCell.cellType == CellType.NUMERIC) {
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

    override fun writeFile(flights: List<Flight>, file: File): Boolean {
        var row: Row
        val wb = HSSFWorkbook()
        var c: Cell
        val mainSheet = wb.createSheet(LOG_SHEET_MAIN)
        row = mainSheet.createRow(0)
        c = row.createCell(0)
        c.setCellValue(resourcesProvider.getString(R.string.str_date))
        c = row.createCell(1)
        c.setCellValue(resourcesProvider.getString(R.string.str_itemlogtime))
        c = row.createCell(2)
        c.setCellValue(resourcesProvider.getString(R.string.str_aircraft_type))
        c = row.createCell(3)
        c.setCellValue(resourcesProvider.getString(R.string.str_regnum))
        c = row.createCell(4)
        c.setCellValue(resourcesProvider.getString(R.string.str_day_night))
        c = row.createCell(5)
        c.setCellValue(resourcesProvider.getString(R.string.str_vfr_ifr))
        c = row.createCell(6)
        c.setCellValue(resourcesProvider.getString(R.string.str_flight_type))
        c = row.createCell(7)
        c.setCellValue(resourcesProvider.getString(R.string.str_desc))
        c = row.createCell(8)
        c.setCellValue(resourcesProvider.getString(R.string.cell_night_time))
        c = row.createCell(9)
        c.setCellValue(resourcesProvider.getString(R.string.cell_ground_time))
        val exportData = flights
            .map { flight ->
                flight.planeType = aircraftTypesRepository.loadAircraftType(flight.planeId)
                flight.flightType =
                    flightTypesRepository.loadDBFlightType(flight.flightTypeId)
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
       return writeXlsFile(file, wb)
    }

    private fun writeXlsFile(
        file: File,
        wb: HSSFWorkbook
    ): Boolean {
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
        return success
    }

    private fun getOldFlightType(type: Long): String {
        return when (type) {
            CONSTS.FLIGHT.TYPE_CIRCLE -> resourcesProvider.getString(R.string.flight_type_circle)
            CONSTS.FLIGHT.TYPE_ZONE -> resourcesProvider.getString(R.string.flight_type_zone)
            CONSTS.FLIGHT.TYPE_RUOTE -> resourcesProvider.getString(R.string.flight_type_route)
            else -> ""
        }
    }

    private fun getFlightTypeId(fTypeStr: String): Long {
        return if (fTypeStr.contains(".")) {
            fTypeStr.parseDouble()?.toLong() ?: -1
        } else {
            fTypeStr.parseLong() ?: -1
        }
    }

    private fun defaultCurrentTime(): String? = DateTimeUtils.getDateTime(
        System.currentTimeMillis(),
        "dd MMM yyyy"
    )
}