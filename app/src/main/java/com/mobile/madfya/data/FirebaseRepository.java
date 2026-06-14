package com.mobile.madfya.data;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FirebaseRepository {

    private static FirebaseRepository instance;

    // Root database reference
    private final FirebaseDatabase db;

    // Node references
    private final DatabaseReference usersRef;
    private final DatabaseReference sensorsRef;
    private final DatabaseReference alertsRef;
    private final DatabaseReference reportsRef;
    private final DatabaseReference noticesRef;
    private final DatabaseReference commentsRef;

    private FirebaseRepository() {
        db          = FirebaseDatabase.getInstance();
        usersRef    = db.getReference("users");
        sensorsRef  = db.getReference("sensors");
        alertsRef   = db.getReference("alerts");
        reportsRef  = db.getReference("reports");
        noticesRef  = db.getReference("notices");
        commentsRef = db.getReference("comments");
    }

    public static FirebaseRepository get() {
        if (instance == null) instance = new FirebaseRepository();
        return instance;
    }

    // =========================================================================
    // USERS
    // =========================================================================

    /**
     * Attempts login by matching username + password.
     * Calls back with the User object on success, or null on failure.
     */
    public void login(String username, String password, OnResult<User> callback) {
        usersRef.orderByChild("name").equalTo(username)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        for (DataSnapshot child : snapshot.getChildren()) {
                            User user = child.getValue(User.class);
                            if (user != null && password.equals(user.password) && user.active) {
                                user.firebaseKey = child.getKey();
                                user.id = child.getKey  (); // store Firebase key as ID
                                callback.onSuccess(user);
                                return;
                            }
                        }
                        callback.onSuccess(null); // no match
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        callback.onError(error.getMessage());
                    }
                });
    }

    /** Returns LiveData list of all users — for Admin page. */
    public LiveData<List<User>> getAllUsers() {
        MutableLiveData<List<User>> liveData = new MutableLiveData<>();
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<User> list = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    User u = child.getValue(User.class);
                    if (u != null) {
                        u.firebaseKey = child.getKey();
                        u.id = child.getKey();
                        list.add(u);
                    }
                }
                liveData.setValue(list);
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });
        return liveData;
    }

    /** Inserts a new user. Firebase auto-generates the key. */
    public void insertUser(User user) {
        usersRef.push().setValue(user);
    }

    /** Updates an existing user by their Firebase key. */
    public void updateUser(String userId, User user) {
        if (userId == null) return;
        usersRef.child(userId).setValue(user);
    }

    /** Deletes a user by their Firebase key. */
    public void deleteUser(String userId) {
        if (userId == null) return;
        usersRef.child(userId).removeValue();
    }

    // =========================================================================
    // SENSORS
    // =========================================================================

    /** Returns LiveData of all sensors, observed in real-time. */
    public LiveData<List<Sensors>> getAllSensors() {
        MutableLiveData<List<Sensors>> liveData = new MutableLiveData<>();
        sensorsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<Sensors> list = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Sensors s = child.getValue(Sensors.class);
                    if (s != null) {
                        s.firebaseKey = child.getKey();
                        list.add(s);
                    }
                }
                liveData.setValue(list);
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });
        return liveData;
    }

    /** Returns LiveData of sensors filtered by status. */
    public LiveData<List<Sensors>> getSensorsByStatus(String status) {
        MutableLiveData<List<Sensors>> liveData = new MutableLiveData<>();
        sensorsRef.orderByChild("status").equalTo(status)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        List<Sensors> list = new ArrayList<>();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            Sensors s = child.getValue(Sensors.class);
                            if (s != null) {
                                s.firebaseKey = child.getKey();
                                list.add(s);
                            }
                        }
                        liveData.setValue(list);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {}
                });
        return liveData;
    }

    /** Inserts a new sensor node. */
    public void insertSensor(Sensors sensor) {
        sensorsRef.push().setValue(sensor);
    }

    /** Updates sensor readings by Firebase key. */
    public void updateSensor(String key, Sensors sensor) {
        sensorsRef.child(key).setValue(sensor);
    }

    /** Updates just the live readings on a sensor node. */
    public void updateSensorReadings(String key, String ph, String turbidity,
                                     String temperature, String flowRate,
                                     int hp, String status) {
        DatabaseReference ref = sensorsRef.child(key);
        ref.child("ph_level").setValue(ph);
        ref.child("turbidity").setValue(turbidity);
        ref.child("temperature").setValue(temperature);
        ref.child("water_flow_rate").setValue(flowRate);
        ref.child("HP").setValue(hp);
        ref.child("status").setValue(status);
        ref.child("CurrentTimeStamp").setValue(System.currentTimeMillis());
    }

    public void deleteSensor(String key) {
        sensorsRef.child(key).removeValue();
    }

    // =========================================================================
    // ALERTS
    // =========================================================================

    /** Returns LiveData of all alerts, newest first. */
    public LiveData<List<Alert>> getAllAlerts() {
        MutableLiveData<List<Alert>> liveData = new MutableLiveData<>();
        alertsRef.orderByChild("timestamp")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        List<Alert> list = new ArrayList<>();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            Alert a = child.getValue(Alert.class);
                            if (a != null) {
                                a.firebaseKey = child.getKey();
                                list.add(0, a); // prepend → newest first
                            }
                        }
                        liveData.setValue(list);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {}
                });
        return liveData;
    }

    /** Returns LiveData of alerts filtered by category. */
    public LiveData<List<Alert>> getAlertsByCategory(String category) {
        MutableLiveData<List<Alert>> liveData = new MutableLiveData<>();
        alertsRef.orderByChild("category").equalTo(category)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        List<Alert> list = new ArrayList<>();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            Alert a = child.getValue(Alert.class);
                            if (a != null) {
                                a.firebaseKey = child.getKey();
                                list.add(a);
                            }
                        }
                        liveData.setValue(list);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {}
                });
        return liveData;
    }

    public void insertAlert(Alert alert) {
        alertsRef.push().setValue(alert);
    }

    public void deleteAlert(String key) {
        alertsRef.child(key).removeValue();
    }

    // =========================================================================
    // REPORTS
    // =========================================================================

    /** Returns LiveData of all incident reports. */
    public LiveData<List<Reports>> getAllReports() {
        MutableLiveData<List<Reports>> liveData = new MutableLiveData<>();
        reportsRef.orderByChild("ReportedTimeStamps")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        List<Reports> list = new ArrayList<>();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            Reports r = child.getValue(Reports.class);
                            if (r != null) {
                                r.firebaseKey = child.getKey();
                                list.add(0, r); // newest first
                            }
                        }
                        liveData.setValue(list);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {}
                });
        return liveData;
    }

    /** Returns LiveData of reports by a specific user. */
    public LiveData<List<Reports>> getReportsByUser(String userId) {
        MutableLiveData<List<Reports>> liveData = new MutableLiveData<>();
        reportsRef.orderByChild("ReportedBy").equalTo(userId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        List<Reports> list = new ArrayList<>();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            Reports r = child.getValue(Reports.class);
                            if (r != null) {
                                r.firebaseKey = child.getKey();
                                list.add(r);
                            }
                        }
                        liveData.setValue(list);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {}
                });
        return liveData;
    }

    /** Returns LiveData of reports filtered by category. */
    public LiveData<List<Reports>> getReportsByCategory(String category) {
        MutableLiveData<List<Reports>> liveData = new MutableLiveData<>();
        reportsRef.orderByChild("category").equalTo(category)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        List<Reports> list = new ArrayList<>();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            Reports r = child.getValue(Reports.class);
                            if (r != null) {
                                r.firebaseKey = child.getKey();
                                list.add(r);
                            }
                        }
                        liveData.setValue(list);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {}
                });
        return liveData;
    }

    public void insertReport(Reports report) {
        reportsRef.push().setValue(report);
    }
    public void deleteReport(String key) {
        reportsRef.child(key).removeValue();
    }
    public LiveData<List<CommunityNotice>> getAllNotices() {
        MutableLiveData<List<CommunityNotice>> liveData = new MutableLiveData<>();
        noticesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<CommunityNotice> list = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    CommunityNotice n = child.getValue(CommunityNotice.class);
                    if (n != null) {
                        n.firebaseKey = child.getKey();
                        list.add(0, n);
                    }
                }
                liveData.setValue(list);
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });
        return liveData;
    }
    public void insertNotice(CommunityNotice notice) {
        noticesRef.push().setValue(notice);
    }
    public void updateNotice(String key, CommunityNotice notice) {
        noticesRef.child(key).setValue(notice);
    }
    public void deleteNotice(String key) {
        noticesRef.child(key).removeValue();
    }
    public LiveData<List<Comment>> getCommentsByNotice(String noticeKey) {
        MutableLiveData<List<Comment>> liveData = new MutableLiveData<>();
        commentsRef.orderByChild("noticeId").equalTo(noticeKey)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        List<Comment> list = new ArrayList<>();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            Comment c = child.getValue(Comment.class);
                            if (c != null) {
                                c.firebaseKey = child.getKey();
                                list.add(c);
                            }
                        }
                        liveData.setValue(list);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {}
                });
        return liveData;
    }
    public void insertComment(Comment comment) {
        commentsRef.push().setValue(comment);
    }
    public void deleteComment(String key) {
        commentsRef.child(key).removeValue();
    }
    public interface OnResult<T> {
        void onSuccess(T result);
        void onError(String message);
    }
}