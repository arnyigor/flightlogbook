package com.arny.flightlogbook.domain.files

import com.arny.flightlogbook.domain.models.Flight
import java.io.File

interface FlightFileReadWriter {
    fun readFile(file: File): List<Flight>
    fun writeFile(flights: List<Flight>, file: File): Boolean
}