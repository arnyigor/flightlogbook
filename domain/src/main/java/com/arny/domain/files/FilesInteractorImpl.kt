package com.arny.domain.files

import android.net.Uri
import com.arny.core.CONSTS
import com.arny.core.utils.DateTimeUtils
import com.arny.core.utils.FileUtils
import com.arny.domain.R
import com.arny.domain.common.IResourceProvider
import com.arny.domain.flights.FlightsInteractor
import com.arny.domain.flights.FlightsRepository
import com.arny.domain.models.*
import io.reactivex.Observable
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import java.io.File
import java.io.FileInputStream
import java.util.*
import javax.inject.Inject

class FilesInteractorImpl @Inject constructor(
    private val resourcesProvider: IResourceProvider,
    private val flightsInteractor: FlightsInteractor,
    private val flightsRepository: FlightsRepository,
    private val filesRepository: FilesRepository,
) : FilesInteractor {
    override fun readFile(uri: Uri?, fromSystem: Boolean): String? {
        val filename: String = filesRepository.getFileName(fromSystem, uri)
        val file = File(filename)
        if (!file.isFile || !file.exists()) {
            throw BusinessException(
                String.format(
                    Locale.getDefault(),
                    resourcesProvider.getString(R.string.error_file_not_found),
                    filename
                )
            )
        }
        var flights: List<Flight> = emptyList()
        when {
            file.absolutePath.endsWith(CONSTS.FILES.FILE_EXTENTION_XLS) -> {
                flights = filesRepository.getFlightsFromExcel(
                    HSSFWorkbook(FileInputStream(file)).getSheetAt(0).rowIterator()
                )
            }
            file.absolutePath.endsWith(CONSTS.FILES.FILE_EXTENTION_JSON) -> {
                flights = filesRepository.readJsonFile(file)
            }
        }
        var result = false
        if (flights.isNotEmpty()) {
            flightsRepository.removeAllFlights()
            flightsRepository.resetTableFlights()
            result = flightsRepository.insertFlights(flights)
        }
        return if (result) filename else null
    }

    override fun exportFile(type: ExportFileType): Observable<Result<String>> {
        return flightsInteractor.getFilterFlightsObs()
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

    override fun getFileUri(filename: String?): Uri? = filesRepository.getFileUri(filename)

    override fun getDefaultFilePath() =
        filesRepository.getDefaultFileName(CONSTS.FILES.FILE_NAME_XLS)

    override fun getAllBackupFileNames(): List<String> =
        File(filesRepository.getBackupsPath()).list()
            ?.filter { it.endsWith(".json") || it.endsWith(".xls") } ?: emptyList()

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
                            append("\n")
                        }
                    }.toString()
            }
    }
}