package com.mobile.madfya;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.mobile.madfya.data.AppDatabase;
import com.mobile.madfya.data.Sensors;
import com.mobile.madfya.data.User;
import com.mobile.madfya.ui.GaugeView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class Dashboard extends AppCompatActivity {

    private DrawerLayout    drawerLayout;

    private TextView tvWelcome, tvPhValue, tvTurbidityValue,
            tvTemperatureValue, tvFlowValue,
            tvHpValue, tvHealthSubtitle;
    private TextView badgePh, badgeTurbidity, badgeTemperature, badgeFlow;
    private GaugeView gauge;

    private RecyclerView rvAnnouncements;
    private AnnouncementAdapter announcementAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        bindViews();
        setupDrawer();
        setWelcomeName();
        observeSensors();
        observeAnnouncements();
        setupBottomNav();

        findViewById(R.id.btn_menu).setOnClickListener(v ->
                drawerLayout.openDrawer(GravityCompat.START));

        findViewById(R.id.btn_notifications).setOnClickListener(v ->
                startActivity(new Intent(this, Alerts.class)));

        findViewById(R.id.btn_profile).setOnClickListener(v ->
                startActivity(new Intent(this, UserProfile.class)));

        AppDatabase.dbExecutor.execute(() -> {
            List<User> users = AppDatabase.get(this).userDao().getAllSync();
            if (users.isEmpty()) {
                Log.d("DEBUG_USERS", "No users in database!");
            } else {
                for (User u : users) {
                    Log.d("DEBUG_USERS", "ID: " + u.id
                            + " | Name: " + u.name
                            + " | Password: " + u.password
                            + " | Role: " + u.role
                            + " | Active: " + u.active);
                }
            }
        });
    }

    private void bindViews() {
        drawerLayout        = findViewById(R.id.drawer_layout);
        tvWelcome           = findViewById(R.id.tv_welcome);
        tvPhValue           = findViewById(R.id.tv_ph_value);
        tvTurbidityValue    = findViewById(R.id.tv_turbidity_value);
        tvTemperatureValue  = findViewById(R.id.tv_temperature_value);
        tvFlowValue         = findViewById(R.id.tv_flow_value);
        tvHpValue           = findViewById(R.id.tv_hp_value);
        tvHealthSubtitle    = findViewById(R.id.tv_health_subtitle);
        badgePh             = findViewById(R.id.badge_ph);
        badgeTurbidity      = findViewById(R.id.badge_turbidity);
        badgeTemperature    = findViewById(R.id.badge_temperature);
        badgeFlow           = findViewById(R.id.badge_flow);
        gauge               = findViewById(R.id.health_gauge);
        rvAnnouncements = findViewById(R.id.rv_announcements);

        announcementAdapter = new AnnouncementAdapter();
        rvAnnouncements.setLayoutManager(
                new LinearLayoutManager(this));

        rvAnnouncements.setAdapter(announcementAdapter);
    }

    private void setupDrawer() {
        // Populate header
        TextView drawerName = findViewById(R.id.drawer_user_name);
        TextView drawerRole = findViewById(R.id.drawer_user_role);

        String name = getSharedPreferences(Login.PREFS_NAME, MODE_PRIVATE)
                .getString(Login.KEY_NAME, "User");
        String role = getSharedPreferences(Login.PREFS_NAME, MODE_PRIVATE)
                .getString(Login.KEY_ROLE, "Resident");

        drawerName.setText(name);
        drawerRole.setText(role);

        // Top nav items
        findViewById(R.id.nav_reports).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(this, ViewReport.class));
        });

        findViewById(R.id.nav_alerts).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(this, Alerts.class));
        });

        findViewById(R.id.nav_water_safety).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(this, Safety.class));
        });

        // Bottom items
        findViewById(R.id.nav_profile).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(this, UserProfile.class));
        });

        findViewById(R.id.nav_logout).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            confirmLogout();
        });
    }

    private void confirmLogout() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Log out")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Log out", (dialog, which) -> {
                    Login.logout(this); // clears SharedPreferences + redirects to Login
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void setWelcomeName() {
        String name = getSharedPreferences(Login.PREFS_NAME, MODE_PRIVATE)
                .getString(Login.KEY_NAME, "User");
        tvWelcome.setText("Welcome, " + name);
    }

    private void observeSensors() {
        AppDatabase.get(this).sensorsDao().getAll().observe(this, sensors -> {
            if (sensors == null || sensors.isEmpty()) return;
            Sensors latest = null;
            for (int i = sensors.size() - 1; i >= 0; i--) {
                if (sensors.get(i).filterId == 1) {
                    latest = sensors.get(i);
                    break;
                }
            }
            if (latest == null) return;


            tvPhValue.setText(latest.ph_level != null ? latest.ph_level : "–");
            tvTurbidityValue.setText(latest.turbidity != null ? latest.turbidity : "–");
            tvTemperatureValue.setText(latest.temperature != null ? latest.temperature : "–");
            tvFlowValue.setText(latest.water_flow_rate != null ? latest.water_flow_rate : "–");

            int hp = latest.HP;
            tvHpValue.setText(hp + "%");
            gauge.setPercent(hp);
            updateHealthCard(hp);

            try {
                double ph = Double.parseDouble(latest.ph_level);
                updateBadge(badgePh, (ph < 6.5 || ph > 8.5) ? "WARNING" : "OPERATIONAL");
            } catch (Exception ignored) {}

            try {
                double turb = Double.parseDouble(latest.turbidity);
                updateBadge(badgeTurbidity, turb > 4 ? "WARNING" : "OPERATIONAL");
            } catch (Exception ignored) {}

            try {
                double temp = Double.parseDouble(latest.temperature);
                updateBadge(badgeTemperature, (temp < 20 || temp > 30) ? "WARNING" : "OPERATIONAL");
            } catch (Exception ignored) {}

            try {
                double flow = Double.parseDouble(latest.water_flow_rate);
                updateBadge(badgeFlow, flow < 50 ? "WARNING" : "OPERATIONAL");
            } catch (Exception ignored) {}
        });
    }

    private void updateBadge(TextView badge, String status) {
        switch (status) {
            case "WARNING":
                badge.setText("Warning");
                badge.setTextColor(getColor(R.color.badge_warning_text));
                badge.setBackgroundResource(R.drawable.bg_badge_warning);
                break;
            case "DISABLED":
                badge.setText("Disabled");
                badge.setTextColor(getColor(R.color.badge_disabled_text));
                badge.setBackgroundResource(R.drawable.bg_badge_disabled);
                break;
            default:
                badge.setText("Normal");
                badge.setTextColor(getColor(R.color.badge_normal_text));
                badge.setBackgroundResource(R.drawable.bg_badge_normal);
                break;
        }
    }

    private void updateHealthCard(int hp) {
        if (hp >= 80) {
            tvHealthSubtitle.setText("System is currently healthy. Replacement is unnecessary.");
        } else if (hp >= 50) {
            tvHealthSubtitle.setText("System is operating normally. Schedule maintenance soon.");
        } else {
            tvHealthSubtitle.setText("System health is low. Maintenance is recommended.");
        }
    }


    private void observeAnnouncements() {
        AppDatabase.get(this).alertDao().getAll().observe(this, alerts -> {
            if (alerts == null) return;
            announcementAdapter.setData(alerts);
        });
    }

    private void setupBottomNav() {
        BottomNavigationView nav = findViewById(R.id.bottom_nav);
        nav.setSelectedItemId(R.id.menu_dashboard);
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_dashboard)  return true;
            if (id == R.id.menu_community)  { startActivity(new Intent(this, Community.class)); return true; }
            if (id == R.id.menu_status)     { startActivity(new Intent(this, Status.class));    return true; }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        BottomNavigationView nav = findViewById(R.id.bottom_nav);
        if (nav != null) nav.setSelectedItemId(R.id.menu_dashboard);
    }
}