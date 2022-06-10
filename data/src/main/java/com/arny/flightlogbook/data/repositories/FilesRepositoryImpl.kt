package com.arny.flightlogbook.data.repositories

import android.content.Context
import android.net.Uri
import com.arny.core.CONSTS
import com.arny.core.utils.FilePathUtils
import com.arny.core.utils.FileUtils
import com.arny.flightlogbook.data.di.JsonRead
import com.arny.flightlogbook.data.di.XlsRead
import com.arny.flightlogbook.domain.common.IResourceProvider
import com.arny.flightlogbook.domain.files.FilesRepository
import com.arny.flightlogbook.domain.files.FlightFileReadWriter
import com.arny.flightlogbook.domain.models.ExportFileType
import com.arny.flightlogbook.domain.models.Flight
import java.io.File
import javax.inject.Inject

class FilesRepositoryImpl @Inject constructor(
    private val resourcesProvider: IResourceProvider,
    @XlsRead private val xlsReader: FlightFileReadWriter,
    @JsonRead private val jsonReader: FlightFileReadWriter,
) : FilesRepository {
    override fun getBackupsPath(): String = FileUtils.getWorkDir(resourcesProvider.provideContext())

    override fun getDefaultFileName(fileName: String): String =
        FileUtils.getWorkDir(resourcesProvider.provideContext()) + File.separator + fileName

    override fun getFileUri(fileName: String?): Uri? = FileUtils.getFileUri(
        resourcesProvider.provideContext(),
        File(getDefaultFileName(fileName ?: CONSTS.FILES.FILE_NAME_XLS))
    )

    override fun getFileName(fromSystem: Boolean, uri: Uri?, fileName: String?): String =
        if (fromSystem) {
            when (fileName) {
                CONSTS.FILES.FILE_NAME_XLS -> getDefaultFileName(CONSTS.FILES.FILE_NAME_XLS)
                CONSTS.FILES.FILE_NAME_JSON -> getDefaultFileName(CONSTS.FILES.FILE_NAME_JSON)
                else -> {
                    var filePath = getDefaultFileName(CONSTS.FILES.FILE_NAME_XLS)
                    val file = File(filePath)
                    if (!file.isFile || !file.exists()) {
                        filePath = getDefaultFileName(CONSTS.FILES.FILE_NAME_JSON)
                    }
                    filePath
                }
            }
        } else {
            FilePathUtils.getPath(uri, resourcesProvider.provideContext().applicationContext)
                .toString()
        }

    override fun saveDataToFile(dbFlights: List<Flight>, type: ExportFileType): String? {
        val file = File(getDefaultFilePath(resourcesProvider.provideContext(), type.fileName))
        val success: Boolean =
            if (type == ExportFileType.XLS) {
                xlsReader.writeFile(dbFlights, file)
            } else {
                jsonReader.writeFile(dbFlights, file)
            }
        if (success) {
            return file.path
        }
        return null
    }

    override fun readFile(file: File): List<Flight> = when {
        file.absolutePath.endsWith(CONSTS.FILES.FILE_EXTENTION_XLS) -> xlsReader.readFile(file)
        file.absolutePath.endsWith(CONSTS.FILES.FILE_EXTENTION_JSON) -> jsonReader.readFile(file)
        else -> emptyList()
    }

    override fun getAllBackupsNames(): List<String> =
        File(getBackupsPath()).list()
            ?.filter {
                it.endsWith(ExportFileType.JSON.fileName) || it.endsWith(ExportFileType.XLS.fileName)
            } ?: emptyList()

    override fun getAllBackups(): List<File> =
        File(getBackupsPath()).listFiles()
            ?.filter {
                it.absolutePath.endsWith(ExportFileType.JSON.fileName) ||
                        it.absolutePath.endsWith(ExportFileType.XLS.fileName)
            } ?: emptyList()

    private fun getDefaultFilePath(context: Context, fileName: String): String =
        FileUtils.getWorkDir(context) + File.separator + fileName
}