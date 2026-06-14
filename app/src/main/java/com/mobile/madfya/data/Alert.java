package com.mobile.madfya.data;

public class Alert {

    public String firebaseKey;
    public String id;

    public String title;
    /** Optional second line; may be null. */
    public String message;
    /** Filter bucket: "System", "Personal" or "Community". */
    public String category;
    /** Drives the icon and accent colour: critical, report, maintenance, flow, announce, community, personal. */
    public String type;
    public long timestamp;
    public boolean read;

    public Alert(){}
    public Alert(String title, String message, String category, String type, long timestamp, boolean read) {
        this.title = title;
        this.message = message;
        this.category = category;
        this.type = type;
        this.timestamp = timestamp;
        this.read = read;
    }
}
