package com.mobile.madfya;

import android.Manifest;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mobile.madfya.data.FirebaseRepository;
import com.mobile.madfya.data.User;

public class UserProfile extends AppCompatActivity {

    private ImageView profileImageView;
    private TextView userNameTextView;
    private EditText userNameEditText;
    private ImageView editNameIcon, userProfileview;
    private LinearLayout buttonRow;
    private Button saveNameButton;
    private Button cancelNameButton;
    private Button logoutButton;
    private TextView editProfilePictureText;

    private String   savedImagePath = null;
    private Uri      cameraImageUri = null;

    private Uri selectedImageUri = null;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;

    private FirebaseRepository repo;
    private StorageReference storageRef;
    private User currentUser;

    private final ActivityResultLauncher<String> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    profileImageView.setImageURI(selectedImageUri);
                    uploadProfileImageToFirebase(selectedImageUri);
                }
            });

    private final ActivityResultLauncher<Uri> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
                if (Boolean.TRUE.equals(success) && selectedImageUri != null) {
                    handleImageUri(selectedImageUri);
                    profileImageView.setImageURI(selectedImageUri);
                    uploadProfileImageToFirebase(selectedImageUri);
                } else {
                    Toast.makeText(this, "Camera cancelled", Toast.LENGTH_SHORT).show();
                }
            });

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
        editProfilePictureText.setOnClickListener(v -> showImageSourceDialog());
        profileImageView.setOnClickListener(v -> showImageSourceDialog());
        editNameIcon.setOnClickListener(v -> enterEditMode());
        cancelNameButton.setOnClickListener(v -> cancelEdit());
        saveNameButton.setOnClickListener(v -> saveName());
        logoutButton.setOnClickListener(v -> logout());
    }

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

    private void showImageSourceDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Profile Picture")
                .setItems(new String[]{"Take a photo", "Choose from gallery"}, (dialog, which) -> {
                    if (which == 0) checkCameraAndLaunch();
                    else galleryLauncher.launch("image/*");
                })
                .show();
    }

    private void checkCameraAndLaunch() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST_CODE);
        } else {
            launchCamera();
        }
    }

    private void launchCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME,
                "profile_" + System.currentTimeMillis() + ".jpg");
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.Images.Media.RELATIVE_PATH,
                Environment.DIRECTORY_PICTURES + "/MADFYA");

        selectedImageUri = getContentResolver().insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        if (selectedImageUri != null) cameraLauncher.launch(selectedImageUri);
    }

    private void handleImageUri(Uri uri) {
        savedImagePath = uri.toString();
    }

    private void clearImage() {
        savedImagePath = null;
        cameraImageUri = null;
    }
    private void uploadProfileImageToFirebase(Uri imageUri) {
        if (currentUser == null) return;

        Toast.makeText(this, "Uploading...", Toast.LENGTH_SHORT).show();

        StorageReference fileRef = storageRef.child(currentUser.firebaseKey + ".jpg");

        currentUser.setProfileImagePath(savedImagePath);
        FirebaseRepository.get().updateUser(currentUser.id, currentUser);
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
        repo.updateUser(currentUser.firebaseKey, currentUser);

        getSharedPreferences(Login.PREFS_NAME, MODE_PRIVATE)
                .edit()
                .putString(Login.KEY_NAME, newName)
                .apply();

        userNameTextView.setText(newName);
        exitEditMode();
        Toast.makeText(this, "Name updated successfully!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            launchCamera();
        }
    }

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
        String dummyName = "John Ben";
        userNameTextView.setText(dummyName);
        Toast.makeText(this, "Dummy data loaded", Toast.LENGTH_SHORT).show();
    }
}