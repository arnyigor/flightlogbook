package com.arny.flightlogbook.domain.files

import android.net.Uri
import com.arny.flightlogbook.R
import com.arny.flightlogbook.data.CONSTS
import com.arny.flightlogbook.domain.customfields.ICustomFieldsRepository
import com.arny.flightlogbook.domain.common.IResourceProvider
import com.arny.flightlogbook.domain.flights.FlightsInteractor
import com.arny.flightlogbook.domain.flights.FlightsRepository
import com.arny.flightlogbook.data.models.ExportFileType
import com.arny.flightlogbook.data.models.AppResult
import com.arny.flightlogbook.data.models.toResult
import com.arny.flightlogbook.data.utils.DateTimeUtils
import com.arny.flightlogbook.data.utils.FileUtils
import com.arny.flightlogbook.domain.models.BusinessException
import io.reactivex.Observable
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class FilesInteractorImpl @Inject constructor(
    private val resourcesProvider: IResourceProvider,
    private val flightsInteractor: FlightsInteractor,
    private val flightsRepository: FlightsRepository,
    private val filesRepository: FilesRepository,
    private val customFieldsRepository: ICustomFieldsRepository
) : FilesInteractor {
    override fun readFile(uri: Uri?, fromSystem: Boolean, fileName: String?): String? {
        val file = filesRepository.getFile(fromSystem, uri, fileName)
        if (!file.isFile || !file.exists()) {
            throw BusinessException(
                String.format(
                    Locale.getDefault(),
                    resourcesProvider.getString(R.string.error_file_not_found),
                    fileName
                )
            )
        }
        val flights = filesRepository.readFile(file)
        var result = false
        if (flights.isNotEmpty()) {
            flightsRepository.removeAllFlights()
            flightsRepository.resetTableFlights()
            customFieldsRepository.removeCustomFieldValues()
            customFieldsRepository.removeCustomFields()
            customFieldsRepository.resetTableCustomFieldValues()
            customFieldsRepository.resetTableCustomFields()
            for (flight in flights) {
                val flightId = flightsRepository.insertFlightAndGet(flight)
                result = flightId > 0
                for (fieldValue in flight.customFieldsValues.orEmpty()) {
                    fieldValue.externalId = flightId
                    fieldValue.fieldId = fieldValue.field?.let {
                        customFieldsRepository.addCustomFieldAndGet(it)
                    }
                    val addValue = customFieldsRepository.addCustomFieldValue(fieldValue)
                    if (!addValue) {
                        result = false
                        break
                    }
                }
            }
        }
        return if (result) "${file.nameWithoutExtension}.${file.extension}" else null
    }

    override fun exportFile(type: ExportFileType): Observable<AppResult<String>> {
        return flightsInteractor.getFilterFlightsObs()
            .map { result ->
                var resultPath = ""
                if (result is AppResult.Success) {
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

    override fun getAllBackupFileNames(): List<String> = filesRepository.getAllBackupsNames()

    override fun getAllBackups(): String = filesRepository.getAllBackups().let { files ->
        StringBuilder().apply {
            files.forEach { file ->
                append(resourcesProvider.getString(R.string.file_name))
                append(file.path)
                append(",\n")
                append(resourcesProvider.getString(R.string.file_size))
                append(FileUtils.formatFileSize(file.length()))
                append(",\n")
                append(resourcesProvider.getString(R.string.file_last_modify))
                append(DateTimeUtils.getDateTime(Date(file.lastModified()), "dd.MM.yyyy HH:mm:ss"))
                append("\n")
            }
        }.toString()
    }
}