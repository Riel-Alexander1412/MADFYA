package com.mobile.madfya;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.mobile.madfya.data.AppDatabase;
import com.mobile.madfya.data.Sensors;
import com.mobile.madfya.ui.GaugeView;

import java.util.List;

/**
 * Resident / user dashboard.
 * Reads the latest sensor row and displays PH, turbidity, temperature,
 * water flow and system HP. Also shows the most recent announcement from
 * the alerts table.
 */
public class Dashboard extends AppCompatActivity {

    // Sensor value TextViews
    private TextView tvWelcome;
    private TextView tvPhValue;
    private TextView tvTurbidityValue;
    private TextView tvTemperatureValue;
    private TextView tvFlowValue;
    private TextView tvHpValue;
    private TextView tvHealthSubtitle;

    // Badge TextViews (Normal / Warning)
    private TextView badgePh;
    private TextView badgeTurbidity;
    private TextView badgeTemperature;
    private TextView badgeFlow;

    // Announcement
    private TextView tvAnnouncementTitle;
    private TextView tvAnnouncementBody;

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
        setWelcomeName();
        observeSensors();
        observeAnnouncements();
        setupBottomNav();

        findViewById(R.id.btn_menu).setOnClickListener(v -> {
            // TODO: open navigation drawer if you add one
        });

        findViewById(R.id.btn_notifications).setOnClickListener(v ->
                startActivity(new Intent(this, Alerts.class)));

        findViewById(R.id.btn_profile).setOnClickListener(v ->
                startActivity(new Intent(this, UserProfile.class)));
    }

    // -------------------------------------------------------------------------
    // View binding
    // -------------------------------------------------------------------------

    private void bindViews() {
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
        tvAnnouncementTitle = findViewById(R.id.tv_announcement_title);
        tvAnnouncementBody  = findViewById(R.id.tv_announcement_body);

        GaugeView gauge = findViewById(R.id.health_gauge);

        AppDatabase.get(this).sensorsDao().getAll().observe(this, sensors -> {
            if (sensors == null || sensors.isEmpty()) return;
            Sensors latest = sensors.get(0);
            gauge.setPercent(latest.HP);       // drives the arc
            tvHpValue.setText(latest.HP + "%");
            updateHealthCard(latest.HP);
        });
    }

    // -------------------------------------------------------------------------
    // Welcome name from SharedPreferences
    // -------------------------------------------------------------------------

    private void setWelcomeName() {
        String name = getSharedPreferences(Login.PREFS_NAME, MODE_PRIVATE)
                .getString(Login.KEY_NAME, "User");
        tvWelcome.setText("Welcome, " + name);
    }

    // -------------------------------------------------------------------------
    // Sensor LiveData observer
    // -------------------------------------------------------------------------

    private void observeSensors() {
        AppDatabase.get(this).sensorsDao().getAll().observe(this, sensors -> {
            if (sensors == null || sensors.isEmpty()) return;

            // Use the most recently updated sensor row.
            Sensors latest = sensors.get(0);

            // Populate values — fall back to "–" if null.
            tvPhValue.setText(latest.ph_level != null ? latest.ph_level : "–");
            tvTurbidityValue.setText(latest.turbidity != null ? latest.turbidity : "–");
            tvTemperatureValue.setText(latest.temperature != null ? latest.temperature : "–");
            tvFlowValue.setText(latest.water_flow_rate != null ? latest.water_flow_rate : "–");

            // HP gauge
            int hp = latest.HP;
            tvHpValue.setText(hp + "%");
            updateHealthCard(hp);

            // Status badges
            String status = latest.status != null ? latest.status : "OPERATIONAL";
            updateBadge(badgePh, status);
            updateBadge(badgeTurbidity, status);
            updateBadge(badgeTemperature, status);
            updateBadge(badgeFlow, status);
        });
    }

    /**
     * Sets badge text and colour depending on sensor status.
     * Extend the switch for DISABLED, CRITICAL, etc.
     */
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
            default: // OPERATIONAL
                badge.setText("Normal");
                badge.setTextColor(getColor(R.color.badge_normal_text));
                badge.setBackgroundResource(R.drawable.bg_badge_normal);
                break;
        }
    }

    /** Updates the health subtitle and gauge colour based on HP percentage. */
    private void updateHealthCard(int hp) {
        if (hp >= 80) {
            tvHealthSubtitle.setText("System is currently healthy. Replacement is unnecessary");
        } else if (hp >= 50) {
            tvHealthSubtitle.setText("System is operating normally. Schedule maintenance soon.");
        } else {
            tvHealthSubtitle.setText("System health is low. Maintenance is recommended.");
        }
    }

    // -------------------------------------------------------------------------
    // Announcements observer — show the latest critical/community alert
    // -------------------------------------------------------------------------

    private void observeAnnouncements() {
        AppDatabase.get(this).alertDao().getAll().observe(this, alerts -> {
            if (alerts == null || alerts.isEmpty()) {
                tvAnnouncementTitle.setText("No announcements");
                tvAnnouncementBody.setText("");
                return;
            }
            // Pick the first (most recent) alert as the featured announcement.
            com.mobile.madfya.data.Alert a = alerts.get(0);
            tvAnnouncementTitle.setText(a.title);
            tvAnnouncementBody.setText(a.message != null ? a.message : "");
        });
    }

    // -------------------------------------------------------------------------
    // Bottom navigation
    // -------------------------------------------------------------------------

    private void setupBottomNav() {
        BottomNavigationView nav = findViewById(R.id.bottom_nav);
        nav.setSelectedItemId(R.id.menu_dashboard);
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_dashboard) {
                return true; // already here
            }
            if (id == R.id.menu_community) {
                startActivity(new Intent(this, Community.class));
                return true;
            }
            if (id == R.id.menu_status) {
                startActivity(new Intent(this, Status.class));
                return true;
            }
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