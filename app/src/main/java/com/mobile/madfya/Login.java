package com.mobile.madfya;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.mobile.madfya.data.AppDatabase;
import com.mobile.madfya.data.User;

import java.util.List;

public class Login extends AppCompatActivity {

    public static final String PREFS_NAME  = "madfya_prefs";
    public static final String KEY_USER_ID = "logged_in_user_id";
    public static final String KEY_ROLE    = "logged_in_role";
    public static final String KEY_NAME    = "logged_in_name";

    public static final String ROLE_ADMIN       = "Admin";
    public static final String ROLE_MAINTENANCE = "Maintenance";
    public static final String ROLE_RESIDENT    = "Resident";

    private TextInputLayout   tilUsername, tilPassword;
    private TextInputEditText etUsername,  etPassword;
    private MaterialButton    btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (isLoggedIn()) {
            redirectByRole(getSavedRole());
            return;
        }

        setContentView(R.layout.activity_login);
        bindViews();

        btnLogin.setOnClickListener(v -> attemptLogin());

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
        tilUsername = findViewById(R.id.tilUsername);
        tilPassword = findViewById(R.id.tilPassword);
        etUsername  = findViewById(R.id.etUsername);
        etPassword  = findViewById(R.id.etPassword);
        btnLogin    = findViewById(R.id.btnLogin);
    }

    private void attemptLogin() {
        tilUsername.setError(null);
        tilPassword.setError(null);

        String username = etUsername.getText() != null
                ? etUsername.getText().toString().trim() : "";
        String password = etPassword.getText() != null
                ? etPassword.getText().toString() : "";

        // --- Validation ---
        if (TextUtils.isEmpty(username)) {
            tilUsername.setError("Username is required");
            etUsername.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            tilPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }
        if (password.length() < 4) {
            tilPassword.setError("Password must be at least 4 characters");
            etPassword.requestFocus();
            return;
        }

        btnLogin.setEnabled(false);
        btnLogin.setText("Logging in...");


        AppDatabase.dbExecutor.execute(() -> {
            User user = AppDatabase.get(this).userDao().login(username, password);

            runOnUiThread(() -> {
                btnLogin.setEnabled(true);
                btnLogin.setText("Login");

                if (user == null) {
                    tilPassword.setError("Invalid username or password");
                } else if (!user.active) {
                    tilUsername.setError("This account has been deactivated. Contact your admin.");
                } else {
                    saveSession(user);
                    redirectByRole(user.role);
                }
            });
        });
    }

    private void redirectByRole(String role) {
        Intent intent;

        if (role == null) {
            intent = new Intent(this, Dashboard.class);
        } else {
            switch (role) {
                case ROLE_ADMIN:
                    intent = new Intent(this, AdminMain.class);
                    break;
                case ROLE_MAINTENANCE:
                    intent = new Intent(this, AdminMain.class);
                    break;
                case ROLE_RESIDENT:
                    intent = new Intent(this, Dashboard.class);
                    break;
                default:
                    intent = new Intent(this, Dashboard.class);
                    break;
            }
        }

        startActivity(intent);
        finish();
    }

    private void saveSession(User user) {
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .edit()
                .putInt(KEY_USER_ID, user.id)
                .putString(KEY_ROLE, user.role)
                .putString(KEY_NAME, user.name)
                .apply();
    }

    private boolean isLoggedIn() {
        return getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .getInt(KEY_USER_ID, -1) != -1;
    }

    private String getSavedRole() {
        return getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .getString(KEY_ROLE, null);
    }

    public static void logout(android.content.Context context) {
        context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .edit()
                .clear()
                .apply();

        Intent intent = new Intent(context, Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }
}