package com.arny.flightlogbook.data.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import com.arny.flightlogbook.data.models.FlightType;

import java.util.List;

@Dao
public interface FlightTypeDAO {
	@Query("SELECT * FROM flight_type")
	List<FlightType> getFlightTypes();

	@Query("SELECT * FROM flight_type WHERE _id=:id")
	FlightType getFlightType(long id);

	@Query("DELETE FROM flight_type WHERE _id=:id")
	int delete(long id);

	@Query("DELETE FROM flight_type")
	int delete();

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	long insert(FlightType flightType);
}
