package com.mobile.madfya.data;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * A person on the water network. Shown and managed on the Admin page.
 */
@Entity(tableName = "users")
public class User {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;
    /** "Admin", "Maintenance" or "Resident". */
    public String role;
    public boolean active;
    public long createdAt;
    public String password;

    public User(String name, String role, boolean active, long createdAt) {
        this.name = name;
        this.role = role;
        this.active = active;
        this.createdAt = createdAt;
        this.password = "123456";
    }

    @Ignore
    public User(String name, String role, boolean active, long createdAt, String password) {
        this.name = name;
        this.role = role;
        this.active = active;
        this.createdAt = createdAt;
        this.password = password;
    }
}
