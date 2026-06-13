package com.mobile.madfya.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface AlertDao {

    @Query("SELECT * FROM alerts ORDER BY timestamp DESC")
    LiveData<List<Alert>> getAll();

    @Insert
    long insert(Alert alert);

    @Query("SELECT COUNT(*) FROM alerts")
    int count();
}
