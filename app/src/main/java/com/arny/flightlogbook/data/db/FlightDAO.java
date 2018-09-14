package com.arny.flightlogbook.data.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.database.Cursor;

import com.arny.flightlogbook.data.models.Flight;

import java.util.ArrayList;
import java.util.List;

@Dao
public interface FlightDAO {
	@Query("SELECT * FROM main_table")
	List<Flight> queryFlights();

	@Query("SELECT * FROM main_table ORDER BY :orderby")
	List<Flight> queryFlightsWithOrder(String orderby);

	@Query("SELECT * FROM main_table WHERE _id=:id")
	Flight queryFlight(long id);

	@Query("SELECT SUM(log_time) FROM main_table")
	Cursor queryFlightTime();

	@Query("SELECT COUNT(*) FROM main_table")
	Cursor queryFlightsCount();

	@Query("DELETE FROM main_table WHERE _id=:id")
	int delete(long id);

	@Query("DELETE FROM main_table")
	int delete();

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	long insert(Flight flight);

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	long[] insertAll(ArrayList<Flight> flights);
}
