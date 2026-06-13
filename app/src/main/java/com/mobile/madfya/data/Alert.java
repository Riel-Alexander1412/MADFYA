package com.mobile.madfya.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * A single entry in the Alerts &amp; Notifications Center — the app's activity feed.
 * Entries come from seed data, from admin actions (user added/removed) and from
 * important community notices, so this one table is the shared "activity" log.
 */
@Entity(tableName = "alerts")
public class Alert {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String title;
    /** Optional second line; may be null. */
    public String message;
    /** Filter bucket: "System", "Personal" or "Community". */
    public String category;
    /** Drives the icon and accent colour: critical, report, maintenance, flow, announce, community, personal. */
    public String type;
    public long timestamp;
    public boolean read;

    public Alert(String title, String message, String category, String type, long timestamp, boolean read) {
        this.title = title;
        this.message = message;
        this.category = category;
        this.type = type;
        this.timestamp = timestamp;
        this.read = read;
    }
}
