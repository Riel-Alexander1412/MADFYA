package com.mobile.madfya.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface CommunityNoticeDao {

    /** Pinned (urgent) notices first, then newest. */
    @Query("SELECT * FROM notices ORDER BY pinned DESC, timestamp DESC")
    LiveData<List<CommunityNotice>> getAll();

    @Insert
    long insert(CommunityNotice notice);

    @Query("UPDATE notices SET likes = likes + 1 WHERE id = :id")
    void like(int id);

    @Query("UPDATE notices SET commentsCount = commentsCount + 1 WHERE id = :id")
    void incrementComments(int id);

    @Query("SELECT COUNT(*) FROM notices")
    int count();
}
