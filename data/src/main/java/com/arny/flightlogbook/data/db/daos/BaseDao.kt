package com.arny.flightlogbook.data.db.daos
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Update

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
