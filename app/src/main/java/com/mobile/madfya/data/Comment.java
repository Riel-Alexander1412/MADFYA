package com.mobile.madfya.data;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * A comment on a {@link CommunityNotice}. Deleting the parent notice cascades to
 * its comments, which keeps the activity database consistent.
 */
@Entity(
        tableName = "comments",
        foreignKeys = @ForeignKey(
                entity = CommunityNotice.class,
                parentColumns = "id",
                childColumns = "noticeId",
                onDelete = ForeignKey.CASCADE),
        indices = {@Index("noticeId")})
public class Comment {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public int noticeId;
    public String author;
    public String text;
    public long timestamp;

    public Comment(int noticeId, String author, String text, long timestamp) {
        this.noticeId = noticeId;
        this.author = author;
        this.text = text;
        this.timestamp = timestamp;
    }
}
