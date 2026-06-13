package com.mobile.madfya.data;

import android.content.Context;

import androidx.lifecycle.LiveData;

import java.util.List;

/**
 * Single entry point the screens use to read and write data. It hides the DAOs,
 * runs every write on the background executor, and wires the three features
 * together:
 *
 * <ul>
 *     <li>Adding or removing a user logs an entry in the activity feed.</li>
 *     <li>Posting an "important" community notice also broadcasts an alert.</li>
 * </ul>
 */
public class MadfyaRepository {

    private final AppDatabase db;

    public MadfyaRepository(Context context) {
        db = AppDatabase.get(context);
    }

    // ---------------- Users (Admin page) ----------------

    public LiveData<List<User>> users() {
        return db.userDao().getAll();
    }

    public void addUser(User user) {
        AppDatabase.dbExecutor.execute(() -> {
            db.userDao().insert(user);
            db.alertDao().insert(new Alert(
                    "New user added",
                    user.name + " · " + user.role,
                    "System", "report", System.currentTimeMillis(), false));
        });
    }

    public void updateUser(User user) {
        AppDatabase.dbExecutor.execute(() -> db.userDao().update(user));
    }

    public void deleteUser(User user) {
        AppDatabase.dbExecutor.execute(() -> {
            db.userDao().delete(user);
            db.alertDao().insert(new Alert(
                    "User removed",
                    user.name + " was removed from the network",
                    "System", "announce", System.currentTimeMillis(), false));
        });
    }

    // ---------------- Alerts / activity feed ----------------

    public LiveData<List<Alert>> alerts() {
        return db.alertDao().getAll();
    }

    public void addAlert(Alert alert) {
        AppDatabase.dbExecutor.execute(() -> db.alertDao().insert(alert));
    }

    // ---------------- Community notices ----------------

    public LiveData<List<CommunityNotice>> notices() {
        return db.noticeDao().getAll();
    }

    public void postNotice(CommunityNotice notice) {
        AppDatabase.dbExecutor.execute(() -> {
            db.noticeDao().insert(notice);
            if (notice.important) {
                db.alertDao().insert(new Alert(
                        notice.title,
                        notice.body,
                        "Community",
                        notice.pinned ? "critical" : "community",
                        notice.timestamp, false));
            }
        });
    }

    public void likeNotice(int noticeId) {
        AppDatabase.dbExecutor.execute(() -> db.noticeDao().like(noticeId));
    }

    public LiveData<List<Comment>> commentsFor(int noticeId) {
        return db.commentDao().getForNotice(noticeId);
    }

    public void addComment(Comment comment) {
        AppDatabase.dbExecutor.execute(() -> {
            db.commentDao().insert(comment);
            db.noticeDao().incrementComments(comment.noticeId);
        });
    }
}
