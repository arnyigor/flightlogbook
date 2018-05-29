package com.arny.flightlogbook.data.db;

import android.arch.persistence.room.*;
import com.arny.flightlogbook.data.models.Flight;
import io.reactivex.Flowable;

import java.util.List;

@Dao
public interface FlightDAO {
	@Query("SELECT * FROM main_table")
	List<Flight> getFlights();
	@Query("SELECT * FROM main_table ORDER BY :orderby")
	List<Flight> getFlightsWithOrder(String orderby);

	@Query("SELECT * FROM main_table WHERE _id=:id")
	Flight getFlight(long id);

	@Query("DELETE FROM main_table WHERE _id=:id")
	int delete(long id);

	@Query("DELETE FROM main_table")
	int delete();

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	long insert(Flight flight);
}
