package com.arny.domain.files

import android.net.Uri
import com.arny.domain.models.ExportFileType
import com.arny.domain.models.Flight

interface FilesRepository {
    fun saveDataToFile(dbFlights: List<Flight>, type: ExportFileType = ExportFileType.XLS): String?
    fun getFlightsFromExcel(rowIter: Iterator<*>): List<Flight>
    fun getDefaultFileName(): String
    fun getDefaultFileUri(): Uri?
    fun getFileName(fromSystem: Boolean, uri: Uri?): String
    fun getBackupsPath(): String
}
