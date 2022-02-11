package com.arny.domain.files

import com.arny.domain.models.Flight

interface FilesRepository {
    fun saveExcelFile(dbFlights: List<Flight>, exportfilePath: String?): String?
}
