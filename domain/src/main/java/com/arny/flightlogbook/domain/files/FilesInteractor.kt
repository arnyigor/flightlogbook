package com.arny.flightlogbook.domain.files

import android.net.Uri
import com.arny.flightlogbook.domain.models.ExportFileType
import com.arny.flightlogbook.domain.models.Result
import io.reactivex.Observable

interface FilesInteractor {
    fun readFile(uri: Uri?, fromSystem: Boolean, fileName: String?): String?
    fun exportFile(type: ExportFileType): Observable<Result<String>>
    fun getFileUri(filename: String? = null): Uri?
    fun getAllBackups(): String?
    fun getAllBackupFileNames(): List<String>
    fun getDefaultFilePath(): String
}
