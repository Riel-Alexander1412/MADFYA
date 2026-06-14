package com.mobile.madfya.data;


public class Comment {

    public String firebaseKey;
    public String id;

    public String noticeId;
    public String author;
    public String text;
    public long timestamp;

    public Comment(String noticeId, String author, String text, long timestamp) {
        this.noticeId = noticeId;
        this.author = author;
        this.text = text;
        this.timestamp = timestamp;
    }
}
