package com.arny.domain.flighttypes

import com.arny.domain.models.FlightType
import com.arny.helpers.utils.fromCallable
import io.reactivex.Observable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FlightTypesInteractor @Inject constructor(private val flightTypesRepository: FlightTypesRepository) {

    fun updateFlightType(id: Long?, title: String?): Observable<Boolean> {
        return fromCallable { flightTypesRepository.updateFlightTypeTitle(id, title) }
    }

    fun insertFlightType(title: String): Observable<Boolean> {
        return fromCallable {
            flightTypesRepository.addFlightType(FlightType(typeTitle = title))
        }
    }

    fun loadDBFlightTypes(): Observable<List<FlightType>> {
        return fromCallable { flightTypesRepository.loadDBFlightTypes() }
    }

    fun removeFlightType(id: Long?): Observable<Boolean> {
        return fromCallable { flightTypesRepository.removeFlightType(id) }
    }
}
