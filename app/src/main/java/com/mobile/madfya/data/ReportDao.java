package com.mobile.madfya.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ReportDao {
    @Insert
    long insert(Reports report);
    @Query("SELECT * FROM reports ORDER BY ReportedTimeStamps DESC")
    LiveData<List<Reports>> getAll();
    @Query("SELECT * FROM reports ORDER BY ReportedTimeStamps DESC")
    List<Reports> getAllSync();
    @Query("SELECT * FROM reports WHERE id = :id LIMIT 1")
    LiveData<Reports> getById(int id);

    /**
     * Reports filtered by category.
     * Pass "Damaged Pipes", "Unusual Behavior", or "Miscellaneous".
     */
    @Query("SELECT * FROM reports WHERE category = :category ORDER BY ReportedTimeStamps DESC")
    LiveData<List<Reports>> getByCategory(String category);

    @Query("SELECT COUNT(*) FROM reports WHERE category = :category")
    int countByCategory(String category);

    @Query("SELECT * FROM reports WHERE ReportedBy = :userId ORDER BY ReportedTimeStamps DESC")
    LiveData<List<Reports>> getByUser(int userId);

    @Query("SELECT COUNT(*) FROM reports WHERE ReportedBy = :userId")
    int countByUser(int userId);

    @Query("SELECT * FROM reports WHERE ReportedTimeStamps BETWEEN :from AND :to ORDER BY ReportedTimeStamps DESC")
    LiveData<List<Reports>> getByDateRange(long from, long to);

    @Query("SELECT * FROM reports WHERE ImagePath IS NOT NULL ORDER BY ReportedTimeStamps DESC")
    LiveData<List<Reports>> getWithImages();

    @Update
    void update(Reports report);

    @Delete
    void delete(Reports report);

    @Query("DELETE FROM reports WHERE id = :id")
    void deleteById(int id);

    @Query("SELECT COUNT(*) FROM reports")
    int count();
}