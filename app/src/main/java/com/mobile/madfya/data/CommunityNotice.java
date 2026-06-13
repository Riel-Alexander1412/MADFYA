package com.mobile.madfya.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * A notice shared on the Community page by a user or admin. When {@link #important}
 * is set, the repository also pushes a matching {@link Alert} into the activity feed.
 */
@Entity(tableName = "notices")
public class CommunityNotice {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String title;
    public String body;
    /** "General", "Event", "Alert" or "Maintenance". */
    public String tag;
    public String authorName;
    public boolean authorIsAdmin;
    public long timestamp;
    public String locationName;
    public double distanceKm;
    public int likes;
    public int commentsCount;
    /** Urgent notice rendered as the red card pinned to the top of the list. */
    public boolean pinned;
    /** When true the notice is also broadcast as an alert. */
    public boolean important;

    public CommunityNotice(String title, String body, String tag, String authorName,
                           boolean authorIsAdmin, long timestamp, String locationName,
                           double distanceKm, int likes, int commentsCount,
                           boolean pinned, boolean important) {
        this.title = title;
        this.body = body;
        this.tag = tag;
        this.authorName = authorName;
        this.authorIsAdmin = authorIsAdmin;
        this.timestamp = timestamp;
        this.locationName = locationName;
        this.distanceKm = distanceKm;
        this.likes = likes;
        this.commentsCount = commentsCount;
        this.pinned = pinned;
        this.important = important;
    }
}
