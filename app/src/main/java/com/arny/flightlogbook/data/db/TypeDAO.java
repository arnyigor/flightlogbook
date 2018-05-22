package com.arny.flightlogbook.data.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import com.arny.flightlogbook.data.models.Flight;
import com.arny.flightlogbook.data.models.Type;
import io.reactivex.Flowable;

import java.util.List;

@Dao
public interface TypeDAO {
	@Query("SELECT * FROM type_table")
	List<Type> getTypes();

	@Query("SELECT * FROM type_table WHERE type_id=:id")
	Type getType(long id);

	@Query("DELETE FROM type_table WHERE type_id=:id")
	int delete(long id);

	@Query("DELETE FROM type_table")
	int delete();

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	long insert(Type type);
}
