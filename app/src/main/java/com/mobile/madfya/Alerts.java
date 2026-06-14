package com.mobile.madfya;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.mobile.madfya.data.Alert;
import com.mobile.madfya.data.MadfyaRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Alerts &amp; Notifications Center — the activity feed, filterable by category.
 */
public class Alerts extends AppCompatActivity {

    private MadfyaRepository repo;
    private AlertAdapter adapter;
    private final List<Alert> all = new ArrayList<>();
    private String category = "All";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_alerts);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        repo = new MadfyaRepository(this);

        RecyclerView recycler = findViewById(R.id.recycler);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AlertAdapter();
        recycler.setAdapter(adapter);

        repo.alerts().observe(this, alerts -> {
            all.clear();
            all.addAll(alerts);
            render();
        });

        ChipGroup chipGroup = findViewById(R.id.chip_group);
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            category = categoryFor(checkedIds.isEmpty() ? R.id.chip_all : checkedIds.get(0));
            render();
        });

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        ((FloatingActionButton) findViewById(R.id.fab)).setOnClickListener(v -> showAddAlertDialog());

        setupBottomNav();
    }

    private void render() {
        if ("All".equals(category)) {
            adapter.submit(all);
            return;
        }
        List<Alert> filtered = new ArrayList<>();
        for (Alert a : all) {
            if (category.equals(a.category)) {
                filtered.add(a);
            }
        }
        adapter.submit(filtered);
    }

    private String categoryFor(int chipId) {
        if (chipId == R.id.chip_system) {
            return "System";
        }
        if (chipId == R.id.chip_personal) {
            return "Personal";
        }
        if (chipId == R.id.chip_community) {
            return "Community";
        }
        return "All";
    }

    private void showAddAlertDialog() {
        View form = LayoutInflater.from(this).inflate(R.layout.dialog_alert_form, null, false);
        final TextInputLayout tilTitle = form.findViewById(R.id.til_title);
        final TextInputEditText etTitle = form.findViewById(R.id.et_title);
        final TextInputEditText etMessage = form.findViewById(R.id.et_message);
        final RadioGroup rgCategory = form.findViewById(R.id.rg_category);
        final MaterialSwitch swCritical = form.findViewById(R.id.sw_critical);

        final AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle("New alert")
                .setView(form)
                .setPositiveButton(R.string.action_save, null)
                .setNegativeButton(R.string.action_cancel, null)
                .create();

        dialog.setOnShowListener(d -> {
            Button save = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            save.setOnClickListener(v -> {
                String title = etTitle.getText() == null ? "" : etTitle.getText().toString().trim();
                if (title.isEmpty()) {
                    tilTitle.setError("Title is required");
                    return;
                }
                String message = etMessage.getText() == null ? "" : etMessage.getText().toString().trim();
                String cat = categoryForRadio(rgCategory.getCheckedRadioButtonId());
                String type = swCritical.isChecked() ? "critical" : typeForCategory(cat);
                repo.addAlert(new Alert(title, message.isEmpty() ? null : message,
                        cat, type, System.currentTimeMillis(), false));
                dialog.dismiss();
            });
        });
        dialog.show();
    }

    private String categoryForRadio(int checkedId) {
        if (checkedId == R.id.rb_personal) {
            return "Personal";
        }
        if (checkedId == R.id.rb_community) {
            return "Community";
        }
        return "System";
    }

    private String typeForCategory(String cat) {
        switch (cat) {
            case "Personal":
                return "personal";
            case "Community":
                return "community";
            default:
                return "announce";
        }
    }

    private void setupBottomNav() {
        BottomNavigationView nav = findViewById(R.id.bottom_nav);
        nav.setSelectedItemId(R.id.menu_alerts_alerts);
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_alerts_alerts) {
                return true;
            }
            if (id == R.id.menu_alerts_status) {
                startActivity(new Intent(this, Status.class));
                return true;
            }
            if (id == R.id.menu_alerts_profile) {
                startActivity(new Intent(this, UserProfile.class));
                return true;
            }
            if (id == R.id.menu_alerts_map) {
                Toast.makeText(this, "Map coming soon", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });
    }

}
