package com.arny.domain.flighttypes

import com.arny.data.repositories.MainRepositoryImpl
import com.arny.domain.models.FlightType
import com.arny.domain.models.toFlightType
import com.arny.domain.models.toFlightTypeEntity
import com.arny.helpers.utils.OptionalNull
import com.arny.helpers.utils.fromCallable
import com.arny.helpers.utils.fromNullable
import io.reactivex.Observable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FlightTypesUseCase @Inject constructor(private val repository: MainRepositoryImpl) {

    fun insertFlightTypes(types: List<FlightType>): Observable<Boolean> {
        return fromCallable { repository.addFlightTypes(types.map { it.toFlightTypeEntity() }) }
    }

    fun removeFlightTypes(): Observable<Boolean> {
        return fromCallable { repository.removeFlightTypes() }
    }

    fun updateFlightType(id: Long?,title: String?): Observable<Boolean> {
        return fromCallable { repository.updateFlightTypeTitle(id,title) }
    }

    fun insertFlightType(type: FlightType): Observable<Boolean> {
        return fromCallable { repository.addFlightType(type.toFlightTypeEntity()) }
    }

    fun insertFlightType(title: String): Observable<Boolean> {
        return fromCallable {
            val type = FlightType()
            type.typeTitle = title
            repository.addFlightType(type.toFlightTypeEntity())
        }
    }


    fun getFlightType(id: Long?): Observable<OptionalNull<FlightType?>> {
        return fromNullable { repository.loadDBFlightType(id)?.toFlightType() }
    }

    fun loadDBFlightTypes(): Observable<List<FlightType>> {
        return fromCallable { repository.loadDBFlightTypes().map { it.toFlightType() } }
    }

    fun loadPlaneType(id: Long?): Observable<OptionalNull<FlightType?>> {
        return fromNullable { repository.loadDBFlightType(id)?.toFlightType() }
    }

    fun removeFlightType(id: Long?): Observable<Boolean> {
        return fromCallable { repository.removeFlightType(id) }
    }
}