package com.mobile.madfya;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.mobile.madfya.data.FirebaseRepository;
import com.mobile.madfya.data.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** User management screen: list, search, add, edit and remove users. */
public class AdminMain extends AppCompatActivity implements UserAdapter.Listener {

    private FirebaseRepository repo;
    private UserAdapter adapter;
    private final List<User> all = new ArrayList<>();
    private String query = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // ── Firebase replaces MadfyaRepository ───────────────────────────────
        repo = FirebaseRepository.get();

        RecyclerView recycler = findViewById(R.id.recycler);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UserAdapter(this);
        recycler.setAdapter(adapter);

        // ── Observe users from Firebase in real-time ──────────────────────────
        repo.getAllUsers().observe(this, users -> {
            all.clear();
            all.addAll(users);
            render();
        });

        // ── Search bar ────────────────────────────────────────────────────────
        EditText search = findViewById(R.id.et_search);
        search.addTextChangedListener(new SimpleWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                query = s.toString().trim().toLowerCase(Locale.getDefault());
                render();
            }
        });

        ImageButton headerSearch = findViewById(R.id.btn_search);
        headerSearch.setOnClickListener(v -> {
            search.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null) imm.showSoftInput(search, InputMethodManager.SHOW_IMPLICIT);
        });

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        ((FloatingActionButton) findViewById(R.id.fab)).setOnClickListener(v -> showUserDialog(null));

        setupBottomNav();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Filter + render
    // ─────────────────────────────────────────────────────────────────────────

    private void render() {
        if (query.isEmpty()) {
            adapter.submit(all);
            return;
        }
        List<User> filtered = new ArrayList<>();
        for (User u : all) {
            if (u.name != null && u.name.toLowerCase(Locale.getDefault()).contains(query)
                    || u.role != null && u.role.toLowerCase(Locale.getDefault()).contains(query)) {
                filtered.add(u);
            }
        }
        adapter.submit(filtered);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Add / Edit dialog
    // ─────────────────────────────────────────────────────────────────────────

    private void showUserDialog(final User existing) {
        View form = LayoutInflater.from(this).inflate(R.layout.dialog_user_form, null, false);
        final TextInputLayout    tilName    = form.findViewById(R.id.til_name);
        final TextInputEditText  etName     = form.findViewById(R.id.et_name);
        final TextInputEditText  etPassword = form.findViewById(R.id.et_password);
        final RadioGroup         rgRole     = form.findViewById(R.id.rg_role);
        final MaterialSwitch     swActive   = form.findViewById(R.id.sw_active);

        // Pre-fill when editing an existing user
        if (existing != null) {
            etName.setText(existing.name);
            etPassword.setText(existing.password);
            swActive.setChecked(existing.active);
            switch (existing.role != null ? existing.role : "") {
                case "Admin":       rgRole.check(R.id.rb_admin);       break;
                case "Resident":    rgRole.check(R.id.rb_resident);    break;
                default:            rgRole.check(R.id.rb_maintenance); break;
            }
        } else {
            rgRole.check(R.id.rb_maintenance);
        }

        final AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle(existing == null ? "Add user" : "Edit user")
                .setView(form)
                .setPositiveButton(R.string.action_save, null)
                .setNegativeButton(R.string.action_cancel, null)
                .create();

        dialog.setOnShowListener(d -> {
            Button save = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            save.setOnClickListener(v -> {
                String name = etName.getText() == null
                        ? "" : etName.getText().toString().trim();
                if (name.isEmpty()) {
                    tilName.setError("Name is required");
                    return;
                }

                String role     = roleFor(rgRole.getCheckedRadioButtonId());
                boolean active  = swActive.isChecked();
                String password = etPassword.getText() == null
                        ? "" : etPassword.getText().toString().trim();

                if (existing == null) {
                    // ── INSERT new user into Firebase ─────────────────────────
                    User newUser = new User(name, role, active, System.currentTimeMillis());
                    newUser.password = password.isEmpty() ? "123456" : password;
                    repo.insertUser(newUser);

                } else {
                    // ── UPDATE existing user in Firebase ──────────────────────
                    existing.name   = name;
                    existing.role   = role;
                    existing.active = active;
                    if (!password.isEmpty()) existing.password = password;
                    repo.updateUser(existing.firebaseKey, existing);
                }

                dialog.dismiss();
            });
        });

        dialog.show();
    }

    private String roleFor(int checkedId) {
        if (checkedId == R.id.rb_admin)    return "Admin";
        if (checkedId == R.id.rb_resident) return "Resident";
        return "Maintenance";
    }

    // ─────────────────────────────────────────────────────────────────────────
    // UserAdapter.Listener callbacks
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public void onEdit(User user) {
        showUserDialog(user);
    }

    @Override
    public void onDelete(final User user) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Remove user")
                .setMessage("Remove " + user.name + " from the network?")
                .setPositiveButton(R.string.action_delete, (d, w) -> {
                    // ── DELETE from Firebase using the push key ────────────────
                    repo.deleteUser(user.firebaseKey);
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Bottom navigation
    // ─────────────────────────────────────────────────────────────────────────

    private void setupBottomNav() {
        BottomNavigationView nav = findViewById(R.id.bottom_nav);
        nav.setSelectedItemId(R.id.menu_admin_admin);
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_admin_admin)    return true;
            if (id == R.id.menu_admin_userview) { startActivity(new Intent(this, Dashboard.class));   return true; }
            if (id == R.id.menu_admin_reports)  { startActivity(new Intent(this, ViewReport.class));  return true; }
            if (id == R.id.menu_admin_settings) { startActivity(new Intent(this, UserProfile.class)); return true; }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        BottomNavigationView nav = findViewById(R.id.bottom_nav);
        if (nav != null) nav.setSelectedItemId(R.id.menu_admin_admin);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    abstract static class SimpleWatcher implements TextWatcher {
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
    }
}