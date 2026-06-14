package com.mobile.madfya.data;


public class User {

    public String firebaseKey;
    public String id;

    public String name;
    public String role;        // "Admin", "Maintenance", "Resident"


    public boolean active;
    public long createdAt;
    public String password;
    public String  profileImagePath;

    public User() {}
    public User(String name, String role, boolean active, long createdAt) {
        this.name      = name;
        this.role      = role;
        this.active    = active;
        this.createdAt = createdAt;
    }
}