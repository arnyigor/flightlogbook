package com.arny.flightlogbook.domain.files

import android.net.Uri
import com.arny.flightlogbook.data.models.ExportFileType
import com.arny.flightlogbook.data.models.Flight
import java.io.File

interface FilesRepository {
    fun saveDataToFile(dbFlights: List<Flight>, type: ExportFileType = ExportFileType.XLS): String?
    fun getDefaultFileName(fileName: String): String
    fun getFileUri(fileName: String? = null): Uri?
    fun getFile(fromSystem: Boolean, uri: Uri?, fileName: String?): File
    fun getBackupsPath(): String
    fun copyFileToLocal(fileUri: Uri?,newFileName: String? = null): File?
    fun readFile(file: File): List<Flight>
    fun getAllBackupsNames(): List<String>
    fun getAllBackups(): List<File>
}
