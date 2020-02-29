package com.arny.domain.flighttypes

import com.arny.domain.models.FlightType
import com.arny.helpers.utils.OptionalNull
import com.arny.helpers.utils.fromCallable
import com.arny.helpers.utils.fromNullable
import io.reactivex.Observable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FlightTypesInteractor @Inject constructor(private val flightTypesRepository: FlightTypesRepository) {
    fun insertFlightTypes(types: List<FlightType>): Observable<Boolean> {
        return fromCallable { flightTypesRepository.addFlightTypes(types) }
    }

    fun removeFlightTypes(): Observable<Boolean> {
        return fromCallable { flightTypesRepository.removeFlightTypes() }
    }

    fun updateFlightType(id: Long?, title: String?): Observable<Boolean> {
        return fromCallable { flightTypesRepository.updateFlightTypeTitle(id, title) }
    }

    fun insertFlightType(type: FlightType): Observable<Boolean> {
        return fromCallable { flightTypesRepository.addFlightType(type) }
    }

    fun insertFlightType(title: String): Observable<Boolean> {
        return fromCallable {
            val type = FlightType()
            type.typeTitle = title
            flightTypesRepository.addFlightType(type)
        }
    }

    fun getFlightType(id: Long?): Observable<OptionalNull<FlightType?>> {
        return fromNullable { flightTypesRepository.loadDBFlightType(id) }
    }

    fun loadDBFlightTypes(): Observable<List<FlightType>> {
        return fromCallable { flightTypesRepository.loadDBFlightTypes() }
    }

    fun loadPlaneType(id: Long?): Observable<OptionalNull<FlightType?>> {
        return fromNullable { flightTypesRepository.loadDBFlightType(id) }
    }

    fun removeFlightType(id: Long?): Observable<Boolean> {
        return fromCallable { flightTypesRepository.removeFlightType(id) }
    }
}