package com.arny.flightlogbook.data.source

import com.arny.flightlogbook.data.db.AircraftTypeDAO
import com.arny.flightlogbook.data.models.AircraftType
import com.arny.flightlogbook.data.source.base.BaseRepository
import com.arny.flightlogbook.utils.launchAsync


interface TypesRepository : BaseRepository {
    fun getCraftTypeDAO(): AircraftTypeDAO

    fun loadTypes(onComplete: (ArrayList<AircraftType>) -> Unit? = {}, onError: (Throwable) -> Unit? = {}) {
        launchAsync({
            getCraftTypeDAO().queryAircraftTypes()
        }, {
            onComplete.invoke(ArrayList(it))
        }, {
            onError.invoke(it)
        })
    }

    fun loadType(id: Long, onComplete: (AircraftType?) -> Unit? = {}, onError: (Throwable) -> Unit? = {}) {
        launchAsync({
            getCraftTypeDAO().queryAircraftType(id)
        }, {
            onComplete.invoke(it)
        }, {
            onError.invoke(it)
        })
    }

    fun addType(name: String, onComplete: (Boolean) -> Unit? = {}, onError: (Throwable) -> Unit? = {}) {
        val type = AircraftType()
        type.typeName = name
        launchAsync({
            getCraftTypeDAO().insert(type)
        }, {
            onComplete.invoke(it > 0)
        }, {
            onError.invoke(it)
        })
    }

    fun removeType(type: AircraftType, onComplete: (Boolean) -> Unit? = {}, onError: (Throwable) -> Unit? = {}) {
        launchAsync({
            getCraftTypeDAO().delete(type.typeId)
        }, {
            onComplete.invoke(it > 0)
        }, {
            onError.invoke(it)
        })
    }

    fun updateType(type: AircraftType, onComplete: (Boolean) -> Unit? = {}, onError: (Throwable) -> Unit? = {}) {
        launchAsync({
            getCraftTypeDAO().update(type)
        }, {
            onComplete.invoke(it > 0)
        }, {
            onError.invoke(it)
        })
    }

    fun getAircraftTypesCount(onComplete: (Int) -> Unit? = {}, onError: (Throwable) -> Unit? = {}) {
        launchAsync({
            val cursor = getCraftTypeDAO().queryAirplaneTypesCount()
            var count = 0
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0)
                cursor.close()
            }
            count
        }, {
            onComplete.invoke(it)
        }, {
            onError.invoke(it)
        })
    }
}