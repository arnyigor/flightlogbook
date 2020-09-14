package com.arny.domain.files

import android.net.Uri
import com.arny.domain.models.Result
import io.reactivex.Observable

interface FilesInteractor {
    fun readExcelFile(uri: Uri?, fromSystem: Boolean): String?
    fun exportFile(): Observable<Result<String>>
    fun getDefaultFileUri(): Uri?
    fun getFileData(): String?
}
