package com.arny.flightlogbook.data.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.arny.flightlogbook.data.models.FlightType;
import com.arny.flightlogbook.data.models.FlightTypeValue;

import java.util.List;

@Dao
public interface FlightTypeValueDAO {
	@Query("SELECT * FROM flight_type_values")
	List<FlightTypeValue> queryFlightTypeValues();

	@Query("SELECT * FROM flight_type_values WHERE _id=:id")
	FlightTypeValue queryFlightTypeValue(long id);

	@Query("SELECT * FROM flight_type_values WHERE type=:id")
	List<FlightTypeValue> queryFlightTypeValuesByType(long id);

	@Query("DELETE FROM flight_type_values WHERE _id=:id")
	int delete(long id);

	@Query("DELETE FROM flight_type_values")
	int delete();

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	long insert(FlightTypeValue flightTypeValue);
}
