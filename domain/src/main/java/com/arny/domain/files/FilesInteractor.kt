package com.arny.domain.files

import android.net.Uri
import com.arny.domain.models.ExportFileType
import com.arny.domain.models.Result
import io.reactivex.Observable

interface FilesInteractor {
    fun readExcelFile(uri: Uri?, fromSystem: Boolean): String?
    fun exportFile(type: ExportFileType): Observable<Result<String>>
    fun getFileUri(filename: String? = null): Uri?
    fun getAllBackups(): String?
    fun getAllBackupFileNames(): List<String>
    fun getDefaultFilePath(): String
}
