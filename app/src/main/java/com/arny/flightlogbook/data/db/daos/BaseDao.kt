package com.arny.flightlogbook.data.db.daos
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Update

interface BaseDao<T> {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertReplace(item: T): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertIgnore(item: T): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertReplace(items: Collection<T>): Array<Long>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertIgnore(items: Collection<T>): Array<Long>

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateReplace(item: T): Int

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateReplace(items: Collection<T>): Int

    @Update(onConflict = OnConflictStrategy.IGNORE)
    fun updateIgnore(item: T): Int

    @Update(onConflict = OnConflictStrategy.IGNORE)
    fun updateIgnore(items: Collection<T>): Int

    @Delete
    fun delete(item: T):Int
}
