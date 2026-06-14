package com.mobile.madfya;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.mobile.madfya.data.FirebaseRepository;
import com.mobile.madfya.data.FirebaseSeed;
import com.mobile.madfya.data.User;

public class Login extends AppCompatActivity {

    public static final String PREFS_NAME  = "madfya_prefs";
    public static final String KEY_USER_ID = "logged_in_user_id";
    public static final String KEY_ROLE    = "logged_in_role";
    public static final String KEY_NAME    = "logged_in_name";

    public static final String ROLE_ADMIN       = "Admin";
    public static final String ROLE_MAINTENANCE = "Maintenance";
    public static final String ROLE_RESIDENT    = "Resident";
    public static final String KEY_PROFILE_IMAGE = "profile_image";

    private TextInputLayout   tilUsername, tilPassword;
    private TextInputEditText etUsername,  etPassword;
    private MaterialButton    btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseSeed.populate();

        if (isLoggedIn()) {
            redirectByRole(getSavedRole());
            return;
        }

        setContentView(R.layout.activity_login);

        tilUsername = findViewById(R.id.tilUsername);
        tilPassword = findViewById(R.id.tilPassword);
        etUsername  = findViewById(R.id.etUsername);
        etPassword  = findViewById(R.id.etPassword);
        btnLogin    = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(v -> attemptLogin());
    }

    private void attemptLogin() {
        tilUsername.setError(null);
        tilPassword.setError(null);

        String username = etUsername.getText() != null
                ? etUsername.getText().toString().trim() : "";
        String password = etPassword.getText() != null
                ? etPassword.getText().toString() : "";

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

        // ── Firebase login (replaces Room userDao().login()) ──────────────────
        FirebaseRepository.get().login(username, password, new FirebaseRepository.OnResult<User>() {
            @Override
            public void onSuccess(User user) {
                btnLogin.setEnabled(true);
                btnLogin.setText("Login");

                if (user == null) {
                    tilPassword.setError("Invalid username or password");
                } else {
                    saveSession(user);
                    redirectByRole(user.role);
                }
            }

            @Override
            public void onError(String message) {
                btnLogin.setEnabled(true);
                btnLogin.setText("Login");
                tilPassword.setError("Error: " + message);
            }
        });
    }

    private void redirectByRole(String role) {
        Intent intent;
        if (role == null) {
            intent = new Intent(this, Dashboard.class);
        } else {
            switch (role) {
                case ROLE_ADMIN:
                case ROLE_MAINTENANCE:
                    intent = new Intent(this, AdminMain.class);
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
        // Use user.id if firebaseKey is not set (backwards compatibility in repo)
        String userId = user.firebaseKey != null ? user.firebaseKey : user.id;
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .edit()
                .putString(KEY_USER_ID, userId)
                .putString(KEY_ROLE,    user.role)
                .putString(KEY_NAME,    user.name)
                .apply();
    }

    private boolean isLoggedIn() {
        try {
            return getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                    .getString(KEY_USER_ID, null) != null;
        } catch (ClassCastException e) {
            getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().clear().apply();
            return false;
        }
    }

    private String getSavedRole() {
        try {
            return getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                    .getString(KEY_ROLE, null);
        } catch (ClassCastException e) {
            return null;
        }
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
