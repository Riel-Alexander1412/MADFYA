package com.mobile.madfya.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface UserDao {

    @Query("SELECT * FROM users ORDER BY name COLLATE NOCASE ASC")
    LiveData<List<User>> getAll();

    @Insert
    long insert(User user);

    @Update
    void update(User user);

    @Delete
    void delete(User user);

    @Query("SELECT COUNT(*) FROM users")
    int count();

    @Query("SELECT * FROM users WHERE name = :username AND password = :password AND active = 1 LIMIT 1")
    User login(String username, String password);

    @Query("SELECT * FROM users ORDER BY name COLLATE NOCASE ASC")
    List<User> getAllSync();
}
