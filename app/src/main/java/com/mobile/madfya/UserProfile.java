package com.mobile.madfya;

import android.Manifest;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;

import com.mobile.madfya.data.AppDatabase;
import com.mobile.madfya.data.User;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class UserProfile extends AppCompatActivity {

    private ImageView profileImageView;
    private TextView userNameTextView;
    private EditText userNameEditText;
    private ImageView editNameIcon;
    private LinearLayout buttonRow;
    private Button saveNameButton;
    private Button cancelNameButton;
    private Button logoutButton;
    private TextView editProfilePictureText;

    private Bitmap capturedPhotoBitmap;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;

    private AppDatabase db;
    private User currentUser;

    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Bundle extras = result.getData().getExtras();
                    capturedPhotoBitmap = (Bitmap) extras.get("data");

                    if (currentUser != null && capturedPhotoBitmap != null) {
                        profileImageView.setImageBitmap(capturedPhotoBitmap);
                        saveProfileImageToDatabase(capturedPhotoBitmap);
                    }
                } else {
                    Toast.makeText(this, "Camera Cancelled", Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        profileImageView = findViewById(R.id.profileImageView);
        userNameTextView = findViewById(R.id.userNameTextView);
        userNameEditText = findViewById(R.id.userNameEditText);
        editNameIcon = findViewById(R.id.editNameIcon);
        buttonRow = findViewById(R.id.buttonRow);
        saveNameButton = findViewById(R.id.saveNameButton);
        cancelNameButton = findViewById(R.id.cancelNameButton);
        logoutButton = findViewById(R.id.logoutButton);
        editProfilePictureText = findViewById(R.id.editProfilePictureText);
        ImageView backArrow = findViewById(R.id.backArrowIcon);

        db = AppDatabase.get(this);
        loadUserData();

        backArrow.setOnClickListener(v -> finish());
        editProfilePictureText.setOnClickListener(v -> checkCameraPermission());
        editNameIcon.setOnClickListener(v -> enterEditMode());
        cancelNameButton.setOnClickListener(v -> cancelEdit());
        saveNameButton.setOnClickListener(v -> saveName());
        logoutButton.setOnClickListener(v -> logout());
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST_CODE);
        } else {
            openCamera();
        }
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraLauncher.launch(cameraIntent);
    }

    private void enterEditMode() {
        String currentName = userNameTextView.getText().toString();
        userNameEditText.setText(currentName);
        userNameTextView.setVisibility(View.GONE);
        userNameEditText.setVisibility(View.VISIBLE);
        buttonRow.setVisibility(View.VISIBLE);
    }

    private void loadUserData() {
        SharedPreferences prefs = getSharedPreferences(Login.PREFS_NAME, MODE_PRIVATE);
        int loggedInId = prefs.getInt(Login.KEY_USER_ID, -1);

        if (loggedInId == -1) {
            loadDummyData();
            return;
        }

        AppDatabase.dbExecutor.execute(() -> {
            List<User> users = db.userDao().getAllSync();
            if (users != null && !users.isEmpty()) {
                for (User user : users) {
                    if (user.id == loggedInId) {
                        currentUser = user;
                        break;
                    }
                }

                runOnUiThread(() -> {
                    if (currentUser != null) {
                        userNameTextView.setText(currentUser.name);

                        // Load image path from database if it exists
                        if (currentUser.profileImagePath != null) {
                            File imgFile = new File(currentUser.profileImagePath);
                            if (imgFile.exists()) {
                                profileImageView.setImageURI(Uri.fromFile(imgFile));
                            }
                        }
                    } else {
                        loadDummyData();
                    }
                });
            } else {
                runOnUiThread(this::loadDummyData);
            }
        });
    }

    private void saveProfileImageToDatabase(Bitmap bitmap) {
        AppDatabase.dbExecutor.execute(() -> {
            // 1. Save Bitmap image to local file directory path
            ContextWrapper cw = new ContextWrapper(getApplicationContext());
            File directory = cw.getDir("profile_images", MODE_PRIVATE);
            File file = new File(directory, "user_" + currentUser.id + ".png");

            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);

                // 2. Assign file path back to local model entity object
                currentUser.profileImagePath = file.getAbsolutePath();

                // 3. Write data update parameters straight to database tables
                db.userDao().update(currentUser);

                runOnUiThread(() -> Toast.makeText(UserProfile.this, "Profile picture saved!", Toast.LENGTH_SHORT).show());

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(UserProfile.this, "Failed to save picture", Toast.LENGTH_SHORT).show());
            } finally {
                try {
                    if (fos != null) fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void saveName() {
        String newName = userNameEditText.getText().toString().trim();

        if (newName.isEmpty()) {
            userNameEditText.setError("Name cannot be empty");
            return;
        }

        if (currentUser == null) {
            userNameTextView.setText(newName);
            exitEditMode();
            return;
        }

        currentUser.name = newName;

        AppDatabase.dbExecutor.execute(() -> {
            db.userDao().update(currentUser);

            getSharedPreferences(Login.PREFS_NAME, MODE_PRIVATE)
                    .edit()
                    .putString(Login.KEY_NAME, newName)
                    .apply();

            runOnUiThread(() -> {
                userNameTextView.setText(newName);
                exitEditMode();
                Toast.makeText(UserProfile.this, "Name Updated successfully!", Toast.LENGTH_SHORT).show();
            });
        });
    }

    private void cancelEdit() {
        exitEditMode();
        Toast.makeText(this, "Edit Cancelled", Toast.LENGTH_SHORT).show();
    }

    private void exitEditMode() {
        userNameTextView.setVisibility(View.VISIBLE);
        userNameEditText.setVisibility(View.GONE);
        buttonRow.setVisibility(View.GONE);
    }

    private void logout() {
        Login.logout(this);
    }

    private void loadDummyData() {
        String dummyName = "John Ben";
        userNameTextView.setText(dummyName);
        Toast.makeText(this, "Dummy data loaded", Toast.LENGTH_SHORT).show();
    }
}