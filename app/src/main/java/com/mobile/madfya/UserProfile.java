package com.mobile.madfya;

import android.Manifest;
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

import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mobile.madfya.data.FirebaseRepository;
import com.mobile.madfya.data.User;

import java.io.ByteArrayOutputStream;

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

    private FirebaseRepository repo;
    private StorageReference storageRef;
    private User currentUser;

    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Bundle extras = result.getData().getExtras();
                    capturedPhotoBitmap = (Bitmap) extras.get("data");

                    if (currentUser != null && capturedPhotoBitmap != null) {
                        profileImageView.setImageBitmap(capturedPhotoBitmap);
                        uploadProfileImageToFirebase(capturedPhotoBitmap);
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

        profileImageView       = findViewById(R.id.profileImageView);
        userNameTextView       = findViewById(R.id.userNameTextView);
        userNameEditText       = findViewById(R.id.userNameEditText);
        editNameIcon           = findViewById(R.id.editNameIcon);
        buttonRow              = findViewById(R.id.buttonRow);
        saveNameButton         = findViewById(R.id.saveNameButton);
        cancelNameButton       = findViewById(R.id.cancelNameButton);
        logoutButton           = findViewById(R.id.logoutButton);
        editProfilePictureText = findViewById(R.id.editProfilePictureText);
        ImageView backArrow    = findViewById(R.id.backArrowIcon);

        repo       = FirebaseRepository.get();
        storageRef = FirebaseStorage.getInstance().getReference("profile_images");

        loadUserData();

        backArrow.setOnClickListener(v -> finish());
        editProfilePictureText.setOnClickListener(v -> checkCameraPermission());
        editNameIcon.setOnClickListener(v -> enterEditMode());
        cancelNameButton.setOnClickListener(v -> cancelEdit());
        saveNameButton.setOnClickListener(v -> saveName());
        logoutButton.setOnClickListener(v -> logout());
    }

    // ── Load user from Firebase using the saved Firebase key ─────────────────

    private void loadUserData() {
        SharedPreferences prefs = getSharedPreferences(Login.PREFS_NAME, MODE_PRIVATE);
        String userId = prefs.getString(Login.KEY_USER_ID, null);

        if (userId == null) {
            loadDummyData();
            return;
        }

        repo.getAllUsers().observe(this, users -> {
            if (users == null || users.isEmpty()) {
                loadDummyData();
                return;
            }

            currentUser = null;
            for (User u : users) {
                if (userId.equals(u.firebaseKey)) {
                    currentUser = u;
                    break;
                }
            }

            if (currentUser != null) {
                userNameTextView.setText(currentUser.name);

                // Load profile image from Firebase Storage URL via Glide
                if (currentUser.profileImagePath != null && !currentUser.profileImagePath.isEmpty()) {
                    Glide.with(this)
                            .load(currentUser.profileImagePath)
                            .placeholder(R.drawable.ic_person)
                            .circleCrop()
                            .into(profileImageView);
                }
            } else {
                loadDummyData();
            }
        });
    }

    // ── Upload profile image to Firebase Storage, then save URL to RTDB ──────

    private void uploadProfileImageToFirebase(Bitmap bitmap) {
        if (currentUser == null) return;

        Toast.makeText(this, "Uploading...", Toast.LENGTH_SHORT).show();

        // Convert bitmap to bytes
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] imageData = baos.toByteArray();

        // Store under profile_images/{firebaseKey}.png
        StorageReference fileRef = storageRef.child(currentUser.firebaseKey + ".png");

        fileRef.putBytes(imageData)
                .addOnSuccessListener(taskSnapshot ->
                        // Get the public download URL
                        fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            String downloadUrl = uri.toString();

                            // Save URL into the user's profileImagePath field in RTDB
                            currentUser.profileImagePath = downloadUrl;
                            repo.updateUser(currentUser.firebaseKey, currentUser);

                            Toast.makeText(this, "Profile picture saved!", Toast.LENGTH_SHORT).show();
                        })
                )
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    // ── Save updated name to Firebase + SharedPreferences ────────────────────

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
        repo.updateUser(currentUser.firebaseKey, currentUser);

        getSharedPreferences(Login.PREFS_NAME, MODE_PRIVATE)
                .edit()
                .putString(Login.KEY_NAME, newName)
                .apply();

        userNameTextView.setText(newName);
        exitEditMode();
        Toast.makeText(this, "Name updated successfully!", Toast.LENGTH_SHORT).show();
    }

    // ── Camera permission ─────────────────────────────────────────────────────

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

    // ── Edit mode helpers ─────────────────────────────────────────────────────

    private void enterEditMode() {
        userNameEditText.setText(userNameTextView.getText().toString());
        userNameTextView.setVisibility(View.GONE);
        userNameEditText.setVisibility(View.VISIBLE);
        buttonRow.setVisibility(View.VISIBLE);
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
        userNameTextView.setText("John Ben");
        Toast.makeText(this, "Dummy data loaded", Toast.LENGTH_SHORT).show();
    }
}