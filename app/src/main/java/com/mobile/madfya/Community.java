package com.mobile.madfya;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.mobile.madfya.data.Comment;
import com.mobile.madfya.data.CommunityNotice;
import com.mobile.madfya.data.FirebaseRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/** Community page — powered by Firebase Realtime Database. */
public class Community extends AppCompatActivity implements NoticeAdapter.Listener {

    private FirebaseRepository repo;
    private NoticeAdapter adapter;
    private final List<CommunityNotice> all = new ArrayList<>();
    private String query  = "";
    private String filter = "All";

    private FusedLocationProviderClient fusedLocationClient;
    private ActivityResultLauncher<String> locationPermissionLauncher;
    private TextView tvLocation;
    private String  currentLocationName = null;
    private boolean hasLocation         = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_community);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // ── Firebase replaces MadfyaRepository ───────────────────────────────
        repo = FirebaseRepository.get();

        RecyclerView recycler = findViewById(R.id.recycler);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NoticeAdapter(this);
        recycler.setAdapter(adapter);

        // ── Observe notices from Firebase in real-time ────────────────────────
        repo.getAllNotices().observe(this, notices -> {
            all.clear();
            all.addAll(notices);
            render();
        });

        // ── Search ────────────────────────────────────────────────────────────
        EditText search = findViewById(R.id.et_search);
        search.addTextChangedListener(new AdminMain.SimpleWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                query = s.toString().trim().toLowerCase(Locale.getDefault());
                render();
            }
        });

        // ── Filter chips ──────────────────────────────────────────────────────
        ChipGroup chipGroup = findViewById(R.id.chip_group);
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            filter = filterFor(checkedIds.isEmpty() ? R.id.chip_all : checkedIds.get(0));
            render();
        });

        findViewById(R.id.avatar).setOnClickListener(v ->
                startActivity(new Intent(this, UserProfile.class)));
        ((FloatingActionButton) findViewById(R.id.fab)).setOnClickListener(v -> showPostDialog());

        setupBottomNav();

        // ── Location ──────────────────────────────────────────────────────────
        tvLocation = findViewById(R.id.tv_location);
        tvLocation.setOnClickListener(v -> requestLocation());
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                granted -> {
                    if (granted) fetchLocation();
                    else tvLocation.setText("Location off · tap to enable");
                });
        requestLocation();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Filter + render
    // ─────────────────────────────────────────────────────────────────────────

    private void render() {
        List<CommunityNotice> result = new ArrayList<>();
        for (CommunityNotice n : all) {
            if (matchesFilter(n) && matchesQuery(n)) result.add(n);
        }
        if ("Nearby".equals(filter)) {
            Collections.sort(result, (a, b) -> Double.compare(a.distanceKm, b.distanceKm));
        }
        adapter.submit(result);
    }

    private boolean matchesFilter(CommunityNotice n) {
        switch (filter) {
            case "Nearby":      return n.locationName != null && !n.locationName.trim().isEmpty();
            case "Alerts":      return n.pinned || "Alert".equals(n.tag);
            case "Maintenance": return "Maintenance".equals(n.tag);
            case "General":     return "General".equals(n.tag);
            default:            return true;
        }
    }

    private boolean matchesQuery(CommunityNotice n) {
        if (query.isEmpty()) return true;
        return contains(n.title) || contains(n.body)
                || contains(n.authorName) || contains(n.tag);
    }

    private boolean contains(String value) {
        return value != null && value.toLowerCase(Locale.getDefault()).contains(query);
    }

    private String filterFor(int chipId) {
        if (chipId == R.id.chip_nearby)      return "Nearby";
        if (chipId == R.id.chip_alerts)      return "Alerts";
        if (chipId == R.id.chip_maintenance) return "Maintenance";
        if (chipId == R.id.chip_general)     return "General";
        return "All";
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Location (GPS)
    // ─────────────────────────────────────────────────────────────────────────

    private void requestLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fetchLocation();
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    @SuppressLint("MissingPermission")
    private void fetchLocation() {
        tvLocation.setText("Locating…");
        try {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
                    .addOnSuccessListener(this, location -> {
                        if (location == null) { tvLocation.setText("Location unavailable"); return; }
                        resolveAreaName(location.getLatitude(), location.getLongitude());
                    })
                    .addOnFailureListener(this, e -> tvLocation.setText("Location unavailable"));
        } catch (SecurityException e) {
            tvLocation.setText("Location off · tap to enable");
        }
    }

    private void resolveAreaName(double lat, double lng) {
        final String coords = String.format(Locale.getDefault(), "%.4f, %.4f", lat, lng);
        hasLocation         = true;
        currentLocationName = coords;
        tvLocation.setText(coords);

        new Thread(() -> {
            String area = coords;
            try {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                List<Address> result = geocoder.getFromLocation(lat, lng, 1);
                if (result != null && !result.isEmpty()) {
                    Address a = result.get(0);
                    if      (a.getSubLocality() != null) area = a.getSubLocality();
                    else if (a.getLocality()    != null) area = a.getLocality();
                    else if (a.getAdminArea()   != null) area = a.getAdminArea();
                }
            } catch (IOException | IllegalArgumentException ignored) {}
            final String shown = area;
            runOnUiThread(() -> { currentLocationName = shown; tvLocation.setText(shown); });
        }).start();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Post notice dialog
    // ─────────────────────────────────────────────────────────────────────────

    private void showPostDialog() {
        View form = LayoutInflater.from(this).inflate(R.layout.dialog_post_form, null, false);
        final TextInputLayout   tilTitle    = form.findViewById(R.id.til_title);
        final TextInputEditText etTitle     = form.findViewById(R.id.et_title);
        final TextInputEditText etBody      = form.findViewById(R.id.et_body);
        final RadioGroup        rgTag       = form.findViewById(R.id.rg_tag);
        final MaterialSwitch    swImportant = form.findViewById(R.id.sw_important);

        final AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle("Share a notice")
                .setView(form)
                .setPositiveButton(R.string.action_post, null)
                .setNegativeButton(R.string.action_cancel, null)
                .create();

        dialog.setOnShowListener(d -> {
            Button post = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            post.setOnClickListener(v -> {
                String title = etTitle.getText() == null
                        ? "" : etTitle.getText().toString().trim();
                if (title.isEmpty()) { tilTitle.setError("Title is required"); return; }

                String  body      = etBody.getText() == null ? "" : etBody.getText().toString().trim();
                boolean important = swImportant.isChecked();
                String  tag       = important ? "Alert" : tagFor(rgTag.getCheckedRadioButtonId());
                String  location  = hasLocation ? currentLocationName : null;

                // Get logged-in user name from session
                String authorName = getSharedPreferences(Login.PREFS_NAME, MODE_PRIVATE)
                        .getString(Login.KEY_NAME, "User");

                CommunityNotice notice = new CommunityNotice(
                        title, body, tag, authorName, false,
                        System.currentTimeMillis(), location, 0, 0, 0,
                        important, important);

                // ── INSERT notice into Firebase ───────────────────────────────
                repo.insertNotice(notice);
                dialog.dismiss();
            });
        });
        dialog.show();
    }

    private String tagFor(int checkedId) {
        if (checkedId == R.id.rb_event)       return "Event";
        if (checkedId == R.id.rb_maintenance) return "Maintenance";
        return "General";
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Like / comment (NoticeAdapter.Listener)
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public void onLike(CommunityNotice notice) {
        if (notice.firebaseKey == null) return;
        notice.likes = (notice.likes == 0 ? 0 : notice.likes) + 1;
        FirebaseRepository.get().updateNotice(notice.firebaseKey, notice);
    }

    @Override
    public void onComment(final CommunityNotice notice) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_comments, null, false);
        RecyclerView rv       = view.findViewById(R.id.rv_comments);
        final View   empty    = view.findViewById(R.id.empty);
        EditText     etComment= view.findViewById(R.id.et_comment);
        MaterialButton btnSend = view.findViewById(R.id.btn_send);

        rv.setLayoutManager(new LinearLayoutManager(this));
        final CommentAdapter commentAdapter = new CommentAdapter();
        rv.setAdapter(commentAdapter);

        // ── Observe comments for this notice from Firebase ────────────────────
        final LiveData<List<Comment>> live = repo.getCommentsByNotice(notice.firebaseKey);
        final Observer<List<Comment>> observer = comments -> {
            commentAdapter.submit(comments);
            boolean isEmpty = comments == null || comments.isEmpty();
            empty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            rv.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        };
        live.observe(this, observer);

        String authorName = getSharedPreferences(Login.PREFS_NAME, MODE_PRIVATE)
                .getString(Login.KEY_NAME, "User");

        btnSend.setOnClickListener(v -> {
            String text = etComment.getText().toString().trim();
            if (text.isEmpty()) return;
            // ── INSERT comment into Firebase ──────────────────────────────────
            repo.insertComment(new Comment(notice.firebaseKey, authorName, text,
                    System.currentTimeMillis()));
            etComment.setText("");
        });

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(view)
                .setNegativeButton("Close", null)
                .create();
        dialog.setOnDismissListener(d -> live.removeObserver(observer));
        dialog.show();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Bottom navigation
    // ─────────────────────────────────────────────────────────────────────────

    private void setupBottomNav() {
        BottomNavigationView nav = findViewById(R.id.bottom_nav);
        nav.setSelectedItemId(R.id.menu_community_community);
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_community_community) return true;
            if (id == R.id.menu_community_dashboard) { startActivity(new Intent(this, Dashboard.class)); return true; }
            if (id == R.id.menu_community_status)    { startActivity(new Intent(this, Status.class));    return true; }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        BottomNavigationView nav = findViewById(R.id.bottom_nav);
        if (nav != null) nav.setSelectedItemId(R.id.menu_community_community);
    }
}