package com.arny.domain.times

import com.arny.data.repositories.MainRepositoryImpl
import com.arny.domain.models.TimeType
import com.arny.domain.models.toTimeType
import com.arny.helpers.utils.fromCallable
import io.reactivex.Observable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimeTypesUseCase @Inject constructor(private val repository: MainRepositoryImpl) {
    fun queryTimeTypes(): Observable<List<TimeType>> {
       return fromCallable { repository.queryDBTimeTypes().map { it.toTimeType() } }
    }

    fun updateTimeType(id: Long?, newName: String): Observable<Boolean> {
        return fromCallable{repository.updateDBTimeType(id,newName)}
    }

    fun removeTimeType(id: Long?): Observable<Boolean> {
        return fromCallable{repository.removeDBTimeType(id)}
    }

    fun addTimeType(title: String?): Observable<Boolean> {
        return fromCallable{repository.addDBTimeType(title)}
    }

}