package com.arny.flightlogbook.domain.flighttypes

import com.arny.flightlogbook.data.models.FlightType
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FlightTypesInteractor @Inject constructor(private val flightTypesRepository: FlightTypesRepository) {
    fun updateFlightType(id: Long?, title: String?): Observable<Boolean> {
        return Observable.fromCallable { flightTypesRepository.updateFlightTypeTitle(id, title) }
    }

    fun insertFlightType(title: String): Single<Boolean> {
        return Single.fromCallable {
            flightTypesRepository.addFlightType(FlightType(typeTitle = title))
        }.subscribeOn(Schedulers.io())
    }

    fun loadDBFlightTypes(): Single<List<FlightType>> {
        return Single.fromCallable {
            flightTypesRepository.loadDBFlightTypes()
        }.subscribeOn(Schedulers.io())
    }

    fun removeFlightType(id: Long?): Single<Boolean> {
        return Single.fromCallable {
            flightTypesRepository.removeFlightType(id)
        }.subscribeOn(Schedulers.io())
    }
}
