package com.arny.domain.files

import android.net.Uri
import com.arny.core.utils.DateTimeUtils
import com.arny.core.utils.FileUtils
import com.arny.domain.R
import com.arny.domain.common.IResourceProvider
import com.arny.domain.flights.FlightsRepository
import com.arny.domain.models.BusinessException
import com.arny.domain.models.ExportFileType
import com.arny.domain.models.Result
import com.arny.domain.models.toResult
import io.reactivex.Observable
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import java.io.File
import java.io.FileInputStream
import java.util.*
import javax.inject.Inject

class FilesInteractorImpl @Inject constructor(
    private val resourcesProvider: IResourceProvider,
    private val flightsRepository: FlightsRepository,
    private val filesRepository: FilesRepository,
) : FilesInteractor {
    override fun readExcelFile(uri: Uri?, fromSystem: Boolean): String? {
        if (!FileUtils.isExternalStorageAvailable() || FileUtils.isExternalStorageReadOnly()) {
            return null
        }
        val filename: String = filesRepository.getFileName(fromSystem, uri)
        val xlsfile = File(filename)
        if (!xlsfile.isFile || !xlsfile.exists()) {
            throw BusinessException(
                String.format(
                    Locale.getDefault(),
                    resourcesProvider.getString(R.string.error_file_not_found),
                    filename
                )
            )
        }
        val fileInputStream = FileInputStream(xlsfile)
        val myWorkBook = HSSFWorkbook(fileInputStream)
        val mySheet = myWorkBook.getSheetAt(0)
        val rowIter = mySheet.rowIterator()
        flightsRepository.removeAllFlights()
        flightsRepository.resetTableFlights()
        return if (flightsRepository.insertFlights(filesRepository.getFlightsFromExcel(rowIter))) filename else null
    }

    override fun exportFile(type: ExportFileType): Observable<Result<String>> {
        return flightsRepository.getDbFlightsOrdered()
            .map { result ->
                var resultPath = ""
                if (result is Result.Success) {
                    filesRepository.saveDataToFile(result.data, type)?.let {
                        resultPath = it
                    }
                }
                resultPath.toResult()
            }
    }

    override fun getDefaultFileUri(): Uri? = filesRepository.getDefaultFileUri()

    override fun getDefaultFilePath() = filesRepository.getDefaultFileName()

    override fun getAllBackups(): String? {
        return File(filesRepository.getBackupsPath()).listFiles()
            ?.filter { it.absolutePath.endsWith(".json") || it.absolutePath.endsWith(".xls") }
            ?.let { files ->
                StringBuilder()
                    .apply {
                        files.forEach { file ->
                            append(resourcesProvider.getString(R.string.file_name))
                            append(file.path)
                            append(",\n")
                            append(resourcesProvider.getString(R.string.file_size))
                            append(FileUtils.formatFileSize(file.length()))
                            append(",\n")
                            append(resourcesProvider.getString(R.string.file_last_modify))
                            append(
                                DateTimeUtils.getDateTime(
                                    Date(file.lastModified()),
                                    "dd.MM.yyyy HH:mm:ss"
                                )
                            )
                        }
                    }.toString()
            }
    }
}