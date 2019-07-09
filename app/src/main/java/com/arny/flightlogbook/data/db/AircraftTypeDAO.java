package com.arny.flightlogbook.data.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;
import android.database.Cursor;

import com.arny.flightlogbook.data.models.AircraftType;

import java.util.List;

@Dao
public interface AircraftTypeDAO {
	@Query("SELECT * FROM type_table")
	List<AircraftType> queryAircraftTypes();

	@Query("SELECT * FROM type_table WHERE type_id=:id")
	AircraftType queryAircraftType(long id);

	@Query("SELECT COUNT(*) FROM type_table")
	Cursor queryAirplaneTypesCount();

	@Query("DELETE FROM type_table WHERE type_id=:id")
	int delete(long id);

	@Query("DELETE FROM type_table")
	int delete();

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	long insert(AircraftType aircraftType);

	@Update(onConflict = OnConflictStrategy.REPLACE)
	int update(AircraftType aircraftType);
}
