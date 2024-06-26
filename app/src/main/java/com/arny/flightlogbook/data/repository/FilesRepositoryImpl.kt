package com.arny.flightlogbook.data.repository

import android.content.Context
import android.net.Uri
import android.os.ParcelFileDescriptor
import com.arny.flightlogbook.data.CONSTS
import com.arny.flightlogbook.data.di.JsonRead
import com.arny.flightlogbook.data.di.XlsRead
import com.arny.flightlogbook.domain.common.IResourceProvider
import com.arny.flightlogbook.domain.files.FilesRepository
import com.arny.flightlogbook.domain.files.FlightFileReadWriter
import com.arny.flightlogbook.data.models.ExportFileType
import com.arny.flightlogbook.data.models.Flight
import com.arny.flightlogbook.data.utils.FilePathUtils.Companion.getFileName
import com.arny.flightlogbook.data.utils.FileUtils
import java.io.*
import javax.inject.Inject

class FilesRepositoryImpl @Inject constructor(
    private val resourcesProvider: IResourceProvider,
    @XlsRead private val xlsReader: FlightFileReadWriter,
    @JsonRead private val jsonReader: FlightFileReadWriter,
) : FilesRepository {
    companion object {
        const val TEMP_DIRECTORY = "temp/"
    }

    private val context = resourcesProvider.provideContext()

    override fun getBackupsPath(): String = FileUtils.getWorkDir(resourcesProvider.provideContext())

    override fun getDefaultFileName(fileName: String): String =
        FileUtils.getWorkDir(context) + File.separator + fileName

    override fun getFileUri(fileName: String?): Uri? = FileUtils.getFileUri(
        context,
        File(getDefaultFileName(fileName ?: CONSTS.FILES.FILE_NAME_XLS))
    )

    override fun getFile(fromSystem: Boolean, uri: Uri?, fileName: String?): File =
        if (fromSystem) {
            when (fileName) {
                CONSTS.FILES.FILE_NAME_XLS -> File(getDefaultFileName(CONSTS.FILES.FILE_NAME_XLS))
                CONSTS.FILES.FILE_NAME_JSON -> File(getDefaultFileName(CONSTS.FILES.FILE_NAME_JSON))
                else -> {
                    var filePath = getDefaultFileName(CONSTS.FILES.FILE_NAME_XLS)
                    val file = File(filePath)
                    if (!file.isFile || !file.exists()) {
                        filePath = getDefaultFileName(CONSTS.FILES.FILE_NAME_JSON)
                    }
                    File(filePath)
                }
            }
        } else {
             requireNotNull(copyFileToLocal(uri))
        }

    override fun saveDataToFile(dbFlights: List<Flight>, type: ExportFileType): String? {
        val file = File(getDefaultFilePath(context, type.fileName))
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

    private fun createTempDirectory() {
        createDirectory(TEMP_DIRECTORY)
    }

    private fun createNewFileForUri(uri: Uri): File {
        createTempDirectory()
        val name = getFileName(uri, context)
        val fileName: String = TEMP_DIRECTORY + name
        return File(getFilesDir(), fileName)
    }

    private fun createNewFileForUri(newFileName: String): File {
        createTempDirectory()
        val fileName: String = TEMP_DIRECTORY + newFileName
        return File(getFilesDir(), fileName)
    }

    private fun createDirectory(dirName: String) {
        val dir = File(getFilesDir(), dirName)
        if (!dir.exists()) {
            dir.mkdir()
        }
    }

    private fun getFilesDir(): File? = context.filesDir

    override fun copyFileToLocal(fileUri: Uri?, newFileName: String?): File? {
        fileUri?.let {
            val copyFile: File = if (newFileName.isNullOrEmpty()) {
                createNewFileForUri(fileUri)
            } else {
                createNewFileForUri(newFileName)
            }
            try {
                val parcelFileDescriptor: ParcelFileDescriptor? =
                    context.contentResolver.openFileDescriptor(fileUri, "r", null)
                try {
                    ParcelFileDescriptor.AutoCloseInputStream(parcelFileDescriptor)
                        .use { inputStream ->
                            FileOutputStream(copyFile).use { outputStream ->
                                copyAllBytes(inputStream, outputStream)
                                return copyFile
                            }
                        }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }
        }
        return null
    }

    @Throws(IOException::class)
    private fun copyAllBytes(`in`: InputStream, out: OutputStream) {
        val buffer = ByteArray(1024)
        while (true) {
            val read: Int = `in`.read(buffer)
            if (read == -1) {
                break
            }
            out.write(buffer, 0, read)
        }
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