package com.arny.flightlogbook.domain.files

import android.net.Uri
import com.arny.flightlogbook.data.models.ExportFileType
import com.arny.flightlogbook.data.models.AppResult
import io.reactivex.Observable

interface FilesInteractor {
    fun readFile(uri: Uri?, fromSystem: Boolean, fileName: String?): String?
    fun exportFile(type: ExportFileType): Observable<AppResult<String>>
    fun getFileUri(filename: String? = null): Uri?
    fun getAllBackups(): String?
    fun getAllBackupFileNames(): List<String>
    fun getDefaultFilePath(): String
}
