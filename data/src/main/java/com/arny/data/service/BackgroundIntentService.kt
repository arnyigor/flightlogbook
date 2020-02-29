package com.arny.data.service

import android.app.IntentService
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.arny.constants.CONSTS
import com.arny.data.remote.dropbox.DropboxClientFactory
import com.arny.domain.R
import com.arny.domain.common.PreferencesInteractor
import com.arny.domain.flights.FlightsRepository
import com.arny.domain.flighttypes.FlightTypesRepository
import com.arny.domain.models.Flight
import com.arny.domain.planetypes.PlaneTypesRepository
import com.arny.helpers.utils.BasePermissions
import com.arny.helpers.utils.DateTimeUtils
import com.arny.helpers.utils.FileUtils
import com.arny.helpers.utils.Utility
import com.dropbox.core.DbxException
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.FileMetadata
import com.dropbox.core.v2.files.WriteMode
import org.apache.poi.hssf.usermodel.HSSFCell
import org.apache.poi.hssf.usermodel.HSSFRow
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import javax.inject.Inject

class BackgroundIntentService : IntentService("BackgroundIntentService") {
    private var mIsSuccess: Boolean = false
    private var mIsStopped: Boolean = false
    private var client: DbxClientV2? = null
    private var remoteMetadata: FileMetadata? = null
    private var hashMap: HashMap<String, String>? = null
    private var startId: Int = 0
    @Inject
    lateinit var flightsRepository: FlightsRepository
    @Inject
    lateinit var planeTypesRepository: PlaneTypesRepository
    @Inject
    lateinit var flightTypesRepository: FlightTypesRepository
    @Inject
    lateinit var preferencesInteractor: PreferencesInteractor

    private fun getNotificationResult(): String {
        var notice: String
        if (mIsSuccess) {
            notice = applicationContext.getString(R.string.service_operation_success)
            when (operation) {
                OPERATION_IMPORT_SD -> notice = applicationContext.getString(R.string.str_import_success)
                OPERATION_DBX_SYNC -> notice = applicationContext.getString(R.string.dropbox_sync_complete)
                OPERATION_DBX_DOWNLOAD -> notice = applicationContext.getString(R.string.service_file_download_success)
                OPERATION_DBX_UPLOAD -> notice = applicationContext.getString(R.string.service_file_upload_success)
                OPERATION_EXPORT -> notice = applicationContext.getString(R.string.str_export_success)
            }
        } else {
            notice = applicationContext.getString(R.string.service_operation_fail)
            when (operation) {
                OPERATION_IMPORT_SD -> notice = applicationContext.getString(R.string.service_import_fail)
                OPERATION_DBX_SYNC -> notice = applicationContext.getString(R.string.service_sync_fail)
                OPERATION_DBX_DOWNLOAD -> notice = applicationContext.getString(R.string.service_download_fail)
                OPERATION_DBX_UPLOAD -> notice = applicationContext.getString(R.string.service_upload_fail)
                OPERATION_EXPORT -> notice = applicationContext.getString(R.string.service_export_fail)
            }
        }
        return notice
    }

    private fun setOperation(operation: Int) {
        Companion.operation = operation
    }

    init {
        mIsSuccess = false
        mIsStopped = false
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onDestroy() {
        onServiceDestroy()
        super.onDestroy()
    }

    private fun onServiceDestroy() {
        mIsStopped = true
        sendBroadcastIntent(getNotificationResult())
        stopSelf(startId)
        super.onDestroy()
    }

    private fun initProadcastIntent(): Intent {
        val intent = Intent()
        intent.action = ACTION
        intent.addCategory(Intent.CATEGORY_DEFAULT)
        return intent
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        this.startId = startId
        return Service.START_NOT_STICKY
    }

    override fun onHandleIntent(intent: Intent?) {
        hashMap = HashMap()
        setOperation(intent!!.getIntExtra(EXTRA_KEY_OPERATION_CODE, 0))
        when (getOperation()) {
            OPERATION_IMPORT_SD -> {
                val mPath = intent.getStringExtra(EXTRA_KEY_IMPORT_SD_FILENAME)
                if (mPath.isNullOrBlank()) {
                    readExcelFile(applicationContext, CONSTS.FILES.EXEL_FILE_NAME, true)
                } else {
                    readExcelFile(applicationContext, FileUtils.getSDFilePath(applicationContext, Uri.fromFile(File(mPath))), false)
                }
            }
            OPERATION_DBX_SYNC -> try {
                client = DropboxClientFactory.getClient()
                if (client != null) {
                    getRemoteMetaData()
                    syncFile(remoteMetadata)
                } else {
                    mIsSuccess = false
                }
            } catch (e: DbxException) {
                e.printStackTrace()
                mIsSuccess = false
            }
            OPERATION_DBX_DOWNLOAD -> try {
                client = DropboxClientFactory.getClient()
                if (client != null) {
                    getRemoteMetaData()
                    downloadFile(remoteMetadata)
                } else {
                    mIsSuccess = false
                }
            } catch (e: DbxException) {
                e.printStackTrace()
                mIsSuccess = false
            }
            OPERATION_DBX_UPLOAD -> try {
                client = DropboxClientFactory.getClient()
                if (client != null) {
                    uploadFile()
                } else {
                    mIsSuccess = false
                }
            } catch (e: Exception) {
                e.printStackTrace()
                mIsSuccess = false
            }
            OPERATION_EXPORT -> mIsSuccess = saveExcelFile(applicationContext)
        }

    }

    @Throws(Exception::class)
    private fun getRemoteMetaData() {
        var result = client!!.files().listFolder("")
        while (true) {
            for (metadata in result.entries) {
                if (metadata.name.compareTo(CONSTS.FILES.EXEL_FILE_NAME, ignoreCase = true) == 0) {
                    if (metadata is FileMetadata) {
                        remoteMetadata = metadata
                        break
                    }
                }
            }
            if (!result.hasMore) {
                break
            }
            result = client!!.files().listFolderContinue(result.cursor)
        }
    }

    private fun sendBroadcastIntent(result: String?) {
        val intent = initProadcastIntent()
        intent.putExtra(EXTRA_KEY_FINISH, mIsStopped)
        intent.putExtra(EXTRA_KEY_FINISH_SUCCESS, mIsSuccess)
        intent.putExtra(EXTRA_KEY_OPERATION_CODE, operation)
        intent.putExtra(EXTRA_KEY_OPERATION_RESULT, result)
        intent.putExtra(EXTRA_KEY_OPERATION_DATA, hashMap)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun saveExcelFile(context: Context): Boolean {
        var row: Row
        if (!BasePermissions.isStoragePermissonGranted(context)) {
            return false
        }
        var success = false
        //New Workbook
        val wb = HSSFWorkbook()
        //Cell style for header row
        val cs = wb.createCellStyle()
//        cs.fillForegroundColor = HSSFColor.LIME
//        cs.fillPattern = HSSFCellStyle.SOLID_FOREGROUND
        var c: Cell
        //New Sheet
        val sheet_main = wb.createSheet(LOG_SHEET_MAIN)
        //create base row
        row = sheet_main.createRow(0)
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
        val exportData = flightsRepository.getDbFlights()
                .map { flight ->
                    flight.planeType = planeTypesRepository.loadPlaneType(flight.planeId)
                    flight.flightType = flightTypesRepository.loadDBFlightType(flight.flightTypeId?.toLong())
                    flight
                }
        var rows = 1
        for (flight in exportData) {
            val airplane_type_id = flight.planeId!!
            val planeType = flight.planeType
            val airplane_type = if (planeType != null) planeType.typeName else ""
            row = sheet_main.createRow(rows)
            c = row.createCell(0)
            c.setCellValue(DateTimeUtils.getDateTime(flight.datetime!!, "dd MMM yyyy"))
            c = row.createCell(1)
            c.setCellValue(DateTimeUtils.strLogTime(flight.flightTime!!))
            c = row.createCell(2)
            c.setCellValue(airplane_type)
            c = row.createCell(3)
            c.setCellValue(flight.regNo)
            c = row.createCell(4)
            c.setCellValue(flight.daynight!!.toDouble())
            c = row.createCell(5)
            c.setCellValue(flight.ifrvfr!!.toDouble())
            c = row.createCell(6)
            c.setCellValue(flight.flightTypeId!!.toDouble())
            c = row.createCell(7)
            c.setCellValue(flight.description)
            rows++
        }

        sheet_main.setColumnWidth(0, 15 * 200)
        sheet_main.setColumnWidth(1, 15 * 150)
        sheet_main.setColumnWidth(2, 15 * 150)
        sheet_main.setColumnWidth(3, 15 * 150)
        sheet_main.setColumnWidth(4, 15 * 250)
        sheet_main.setColumnWidth(5, 15 * 300)
        sheet_main.setColumnWidth(6, 15 * 200)
        sheet_main.setColumnWidth(7, 15 * 500)
        // Create a path where we will place our List of objects on external storage
        val file = File(context.getExternalFilesDir(null), CONSTS.FILES.EXEL_FILE_NAME)
        var os: FileOutputStream? = null

        try {
            os = FileOutputStream(file)
            wb.write(os)
            //            Toasty.success(context, getString(R.string.str_file_saved) + " " + file, Toast.LENGTH_SHORT).show();
            //            Toast.makeText(context, getString(R.string.str_file_saved) + " " + file, Toast.LENGTH_SHORT).show();
            success = true
        } catch (e: Exception) {
            e.printStackTrace()
            success = false
        } finally {
            try {
                os?.close()
            } catch (ex: Exception) {
                ex.printStackTrace()
            }

        }
        return success
    }

    private fun readExcelFile(context: Context, filename: String?, fromSystem: Boolean) {
        Log.i(BackgroundIntentService::class.java.simpleName, "readExcelFile: $filename");
        val hasType = false
        val checked = false
        val myWorkBook: HSSFWorkbook
        val xlsfile: File
        val notAccess = !FileUtils.isExternalStorageAvailable() || FileUtils.isExternalStorageReadOnly()
        Log.i(BackgroundIntentService::class.java.simpleName, "readExcelFile: $notAccess");
        if (notAccess) {
            return
        }
        try {
            xlsfile = if (fromSystem) {
                File(context.getExternalFilesDir(null), filename)
            } else {
                File("", filename)
            }
            try {
                val fileInputStream = FileInputStream(xlsfile)
                myWorkBook = HSSFWorkbook(fileInputStream)
            } catch (e: IOException) {
                e.printStackTrace()
                mIsSuccess = false
                return
            }
            val mySheet = myWorkBook.getSheetAt(0)
            val rowIter = mySheet.rowIterator()
            val flightsFromExel = getFlightsFromExcel(rowIter)
            flightsRepository.removeAllFlights()
            flightsRepository.insertFlights(flightsFromExel)
            mIsSuccess = true
        } catch (e: Exception) {
            e.printStackTrace()
            mIsSuccess = false

        }

    }//readFile

    private fun getFlightsFromExcel(rowIter: Iterator<*>): ArrayList<Flight> {
        val flights = ArrayList<Flight>()
        var rowCnt = 0
        var strDate: String? = null
        var strTime: String? = null
        var airplane_type: String? = null
        var reg_no: String? = null
        var strDesc: String
        var airplane_type_id: Long = 0
        var day_night: Long = 0
        var ifr_vfr: Long = 0
        var flight_type: Long = 0
        var logTime: Long = 0
        var mDateTime: Long = 0
        while (rowIter.hasNext()) {
            val myRow = rowIter.next() as HSSFRow
            val cellIter = myRow.cellIterator()
            Log.d(BackgroundIntentService::class.java.simpleName, "rowIter $rowCnt")
            var cellCnt = 0
            while (cellIter.hasNext()) {
                val myCell = cellIter.next() as HSSFCell
                Log.d(BackgroundIntentService::class.java.simpleName, "Cell: $cellCnt")
                Log.d(BackgroundIntentService::class.java.simpleName, "Cell Value: $myCell")
                if (rowCnt > 0) {
                    when (cellCnt) {
                        0 -> {
                            try {
                                strDate = myCell.toString()
                            } catch (e: Exception) {
                                strDate = DateTimeUtils.getDateTime(0.toLong(), "dd MMM yyyy")
                                e.printStackTrace()
                            }

                            Log.d(BackgroundIntentService::class.java.simpleName, "strDate " + strDate!!)
                        }
                        1 -> {
                            try {
                                if (myCell.cellType == Cell.CELL_TYPE_NUMERIC) {
                                    strTime = Utility.match(myCell.dateCellValue.toString(), "(\\d{2}:\\d{2})", 1)
                                } else {
                                    strTime = myCell.toString()
                                }
                            } catch (e: Exception) {
                                strTime = "00:00"
                                e.printStackTrace()
                            }

                            Log.d(BackgroundIntentService::class.java.simpleName, "strTime " + strTime!!)
                        }
                        2 -> {
                            try {
                                airplane_type = myCell.toString()
                                val planeType = planeTypesRepository.loadPlaneType(airplane_type)
                                if (planeType != null) {
                                    airplane_type_id = planeType.typeId
                                } else {
                                    if (!Utility.empty(airplane_type)) {
                                        airplane_type_id = planeTypesRepository.addTypeAndGet(airplane_type)
                                    }
                                }
                            } catch (e: Exception) {
                                airplane_type = ""
                                e.printStackTrace()
                            }

                            Log.d(BackgroundIntentService::class.java.simpleName, "airplane_type " + airplane_type!!)
                        }
                        3 -> {
                            try {
                                reg_no = myCell.toString()
                            } catch (e: Exception) {
                                reg_no = ""
                                e.printStackTrace()
                            }

                            Log.d(BackgroundIntentService::class.java.simpleName, "regNo " + reg_no!!)
                        }
                        4 -> {
                            try {
                                day_night = java.lang.Float.parseFloat(myCell.toString()).toInt().toLong()
                            } catch (e: Exception) {
                                day_night = 0
                                e.printStackTrace()
                            }

                            Log.d(BackgroundIntentService::class.java.simpleName, "day_night $day_night")
                        }
                        5 -> {
                            try {
                                ifr_vfr = java.lang.Float.parseFloat(myCell.toString()).toInt().toLong()
                            } catch (e: Exception) {
                                ifr_vfr = 0
                                e.printStackTrace()
                            }

                            Log.d(BackgroundIntentService::class.java.simpleName, "ifr_vfr $ifr_vfr")
                        }
                        6 -> {
                            try {
                                flight_type = java.lang.Float.parseFloat(myCell.toString()).toInt().toLong()
                            } catch (e: Exception) {
                                flight_type = 0
                                e.printStackTrace()
                            }

                            Log.d(BackgroundIntentService::class.java.simpleName, "flight_type $flight_type")
                        }
                        7 -> {
                            try {
                                strDesc = myCell.toString()
                            } catch (e: Exception) {
                                strDesc = ""
                                e.printStackTrace()
                            }

                            Log.d(BackgroundIntentService::class.java.simpleName, "strDesc $strDesc")
                            try {
                                logTime = DateTimeUtils.convertStringToTime(strTime!!).toLong()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
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

                            try {
                                Log.d(BackgroundIntentService::class.java.simpleName, "\nstrDesc: $strDesc")
                                Log.d(BackgroundIntentService::class.java.simpleName, "strDate: $strDate")
                                Log.d(BackgroundIntentService::class.java.simpleName, "mDateTime: $mDateTime")
                                Log.d(BackgroundIntentService::class.java.simpleName, "strTime: " + strTime!!)
                                Log.d(BackgroundIntentService::class.java.simpleName, "logTime: $logTime")
                                Log.d(BackgroundIntentService::class.java.simpleName, "regNo: " + reg_no!!)
                                Log.d(BackgroundIntentService::class.java.simpleName, "airplane_type_id: $airplane_type_id")
                                Log.d(BackgroundIntentService::class.java.simpleName, "airplane_type: " + airplane_type!!)
                                Log.d(BackgroundIntentService::class.java.simpleName, "day_night: $day_night")
                                Log.d(BackgroundIntentService::class.java.simpleName, "ifr_vfr: $ifr_vfr")
                                Log.d(BackgroundIntentService::class.java.simpleName, "flight_type: $flight_type")
                                Log.d(BackgroundIntentService::class.java.simpleName, "strDesc: $strDesc")
                                Log.d(BackgroundIntentService::class.java.simpleName, "\n")
                                val flight = Flight()
                                flights.add(flight)
                                sendBroadcastIntent(null)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }

                        }
                    }//switch (cellCnt)
                }//if (rowCnt>0)
                cellCnt++
            }//cellIter.hasNext()
            rowCnt++
        }//while (rowIter.hasNext())
        return flights
    }

    private fun downloadFile(metadata: FileMetadata?) {
        try {
            val syncFolder = applicationContext.getExternalFilesDir(null)
            val file = File(syncFolder, CONSTS.FILES.EXEL_FILE_NAME)
            try {
                val outputStream = FileOutputStream(file)
                client!!.files().download(metadata!!.pathLower, metadata.rev).download(outputStream)
                val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                intent.data = Uri.fromFile(file)
                applicationContext.sendBroadcast(intent)
                mIsSuccess = file.length() > 0
            } catch (e: DbxException) {
                e.printStackTrace()
                mIsSuccess = false

            } catch (e: IOException) {
                e.printStackTrace()
                mIsSuccess = false
            }
            val autoimport = preferencesInteractor.isAutoImportEnabled()
            if (autoimport) {
                readExcelFile(applicationContext, CONSTS.FILES.EXEL_FILE_NAME, true)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            mIsSuccess = false

        }

    }

    private fun uploadFile() {
        try {
            val localFile = File(applicationContext.getExternalFilesDir(null), CONSTS.FILES.EXEL_FILE_NAME)
            val remoteFileName = localFile.name
            val inputStream = FileInputStream(localFile)
            val result = client!!.files().uploadBuilder("/$remoteFileName").withMode(WriteMode.OVERWRITE).uploadAndFinish(inputStream)
            mIsSuccess = result != null
        } catch (e: DbxException) {
            e.printStackTrace()
            mIsSuccess = false
        } catch (e: IOException) {
            e.printStackTrace()
            mIsSuccess = false
        }

    }

    private fun syncFile(remoteFile: FileMetadata?) {
        val localFile = File(applicationContext.getExternalFilesDir(null), CONSTS.FILES.EXEL_FILE_NAME)
        try {
            val remoteVal = if (remoteFile == null) {
                null
            } else {
                DateTimeUtils.getDateTime(remoteFile.clientModified, "dd MM yyyy HH:mm:ss")
            }
            val localVal = if (localFile.length() == 0L) {
                null
            } else {
                DateTimeUtils.getDateTime(Date(localFile.lastModified()), "dd MM yyyy HH:mm:ss")
            }
            if (remoteVal != null) {
                hashMap?.put(EXTRA_KEY_OPERATION_DATA_REMOTE_DATE, remoteVal)
            }
            if (localVal != null) {
                hashMap?.put(EXTRA_KEY_OPERATION_DATA_LOCAL_DATE, localVal)
            }
            mIsSuccess = true
        } catch (e: Exception) {
            e.printStackTrace()
            mIsSuccess = false
        }

    }

    companion object {
        /*EXTRAS*/
        val ACTION = "com.arny.flightlogbook.data.service.BackgroundIntentService"
        val EXTRA_KEY_OPERATION_CODE = "BackgroundIntentService.operation.code"
        val EXTRA_KEY_OPERATION_RESULT = "BackgroundIntentService.operation.result"
        val EXTRA_KEY_FINISH = "BackgroundIntentService.operation.finish"
        val EXTRA_KEY_FINISH_SUCCESS = "BackgroundIntentService.operation.success"
        val EXTRA_KEY_IMPORT_SD_FILENAME = "BackgroundIntentService.operation.import.sd.filename"
        val EXTRA_KEY_OPERATION_DATA = "BackgroundIntentService.operation.data"
        val EXTRA_KEY_OPERATION_DATA_REMOTE_DATE = "BackgroundIntentService.operation.data.remote.date"
        val EXTRA_KEY_OPERATION_DATA_LOCAL_DATE = "BackgroundIntentService.operation.data.local.date"
        /*Opearations*/
        val OPERATION_IMPORT_SD = 100
        val OPERATION_DBX_SYNC = 102
        val OPERATION_EXPORT = 101
        val OPERATION_DBX_DOWNLOAD = 103
        val OPERATION_DBX_UPLOAD = 104
        /*other*/
        private val LOG_SHEET_MAIN = "Timelog"
        private var operation: Int = 0

        fun getOperation(): Int {
            return operation
        }
    }

}
