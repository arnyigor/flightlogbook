package com.arny.domain.files

import android.content.Context
import com.arny.domain.R
import com.arny.domain.flighttypes.FlightTypesRepository
import com.arny.domain.models.Flight
import com.arny.domain.planetypes.PlaneTypesRepository
import com.arny.flightlogbook.constants.CONSTS
import com.arny.helpers.utils.DateTimeUtils
import com.arny.helpers.utils.FileUtils
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class FilesRepositoryImpl @Inject constructor(
        private val  context: Context,
        private val flightTypesRepository: FlightTypesRepository,
        private val planeTypesRepository: PlaneTypesRepository,
) : FilesRepository {

    companion object {
        private const val LOG_SHEET_MAIN = "Timelog"
    }

    override fun saveExcelFile(dbFlights: List<Flight>): String? {
            var row: Row
            val wb = HSSFWorkbook()
            var c: Cell
            val mainSheet = wb.createSheet(LOG_SHEET_MAIN)
            row = mainSheet.createRow(0)
            c = row.createCell(0)
            c.setCellValue(context.getString(R.string.str_date))
            c = row.createCell(1)
            c.setCellValue(context.getString(R.string.str_itemlogtime))
            c = row.createCell(2)
            c.setCellValue(context.getString(R.string.str_type_null))
            c = row.createCell(3)
            c.setCellValue(context.getString(R.string.str_regnum))
            c = row.createCell(4)
            c.setCellValue(context.getString(R.string.str_day_night))
            c = row.createCell(5)
            c.setCellValue(context.getString(R.string.str_vfr_ifr))
            c = row.createCell(6)
            c.setCellValue(context.getString(R.string.str_flight_type))
            c = row.createCell(7)
            c.setCellValue(context.getString(R.string.str_desc))
            c = row.createCell(8)
            c.setCellValue(context.getString(R.string.cell_night_time))
            c = row.createCell(9)
            c.setCellValue(context.getString(R.string.cell_ground_time))
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
            val file = File(getDefaultFilePath(context))
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

    private fun getDefaultFilePath(context: Context) =
            FileUtils.getWorkDir(context) + File.separator + CONSTS.FILES.EXEL_FILE_NAME
}