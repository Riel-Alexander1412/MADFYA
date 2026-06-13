package com.mobile.madfya.data;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Room database for MADFYA. Holds users, the alerts/activity feed, community
 * notices and their comments. A background executor is exposed for write
 * operations so the UI thread is never blocked.
 */
@Database(
        entities = {User.class, Alert.class, CommunityNotice.class, Comment.class},
        version = 1,
        exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract UserDao userDao();

    public abstract AlertDao alertDao();

    public abstract CommunityNoticeDao noticeDao();

    public abstract CommentDao commentDao();

    private static volatile AppDatabase instance;

    /** Shared pool for all database writes. */
    public static final ExecutorService dbExecutor = Executors.newFixedThreadPool(4);

    public static AppDatabase get(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "madfya.db")
                            .addCallback(SEED_CALLBACK)
                            .build();
                }
            }
        }
        return instance;
    }

    /** Populates the demo data the first time the database is created. */
    private static final Callback SEED_CALLBACK = new Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            dbExecutor.execute(() -> Seed.populate(instance));
        }
    };
}
