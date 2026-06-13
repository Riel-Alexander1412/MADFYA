package com.mobile.madfya.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface CommentDao {

    @Query("SELECT * FROM comments WHERE noticeId = :noticeId ORDER BY timestamp ASC")
    LiveData<List<Comment>> getForNotice(int noticeId);

    @Insert
    long insert(Comment comment);
}
