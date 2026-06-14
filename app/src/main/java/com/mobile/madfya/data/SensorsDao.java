package com.mobile.madfya.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface SensorsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Sensors sensor);

    @Query("SELECT * FROM sensors ORDER BY CurrentTimeStamp ASC")
    LiveData<List<Sensors>> getAll();

    @Query("SELECT * FROM sensors ORDER BY CurrentTimeStamp DESC")
    List<Sensors> getAllSync();

    @Query("SELECT * FROM sensors WHERE id = :id LIMIT 1")
    LiveData<Sensors> getById(int id);

    @Query("SELECT * FROM sensors WHERE status = :status ORDER BY CurrentTimeStamp DESC")
    LiveData<List<Sensors>> getByStatus(String status);

    @Query("SELECT COUNT(*) FROM sensors WHERE status = :status")
    int countByStatus(String status);

    @Query("SELECT * FROM sensors WHERE InstalledBy = :userId ORDER BY CurrentTimeStamp DESC")
    LiveData<List<Sensors>> getByInstaller(int userId);

    @Query("SELECT * FROM sensors WHERE LastMaintenance < :beforeTimestamp ORDER BY LastMaintenance ASC")
    LiveData<List<Sensors>> getOverdueMaintenance(long beforeTimestamp);

    @Update
    void update(Sensors sensor);

    @Query("UPDATE sensors SET ph_level = :ph, turbidity = :turbidity, " +
            "temperature = :temperature, water_flow_rate = :waterFlowRate, " +
            "HP = :hp, status = :status, CurrentTimeStamp = :timestamp WHERE id = :id")
    void updateReadings(int id, String ph, String turbidity, String temperature,
                        String waterFlowRate, int hp, String status, long timestamp);

    @Query("UPDATE sensors SET LastMaintenance = :timestamp WHERE id = :id")
    void recordMaintenance(int id, long timestamp);

    @Query("UPDATE sensors SET status = :status WHERE id = :id")
    void updateStatus(int id, String status);

    @Delete
    void delete(Sensors sensor);

    @Query("DELETE FROM sensors WHERE id = :id")
    void deleteById(int id);

    @Query("SELECT COUNT(*) FROM sensors")
    int count();
    @Query("SELECT * FROM sensors WHERE filterId = :filterId ORDER BY CurrentTimeStamp DESC LIMIT 1")
    LiveData<Sensors> getLatestByFilter(int filterId);

    @Query("SELECT * FROM sensors WHERE filterId = :filterId ORDER BY CurrentTimeStamp ASC")
    LiveData<List<Sensors>> getHistoryByFilter(int filterId);
}