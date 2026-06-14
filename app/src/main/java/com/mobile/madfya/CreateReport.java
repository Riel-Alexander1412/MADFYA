package com.mobile.madfya;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.mobile.madfya.data.FirebaseRepository;
import com.mobile.madfya.data.Reports;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CreateReport extends AppCompatActivity implements OnMapReadyCallback {
    private static final int REQUEST_LOCATION_PERMISSION = 101;
    private static final int REQUEST_CAMERA_PERMISSION   = 102;
    private AutoCompleteTextView  spinnerCategory;
    private TextInputEditText     etName, etLocation, etDate, etTime, etDetails;
    private TextInputLayout       tilLocation;
    private CardView              cardImagePreview;
    private ImageView             ivPreview;
    private MaterialButton        btnUploadImage, btnSubmit;
    private double   pickedLat    = 0.0;
    private double   pickedLng    = 0.0;
    private String   savedImagePath = null;
    private Uri      cameraImageUri = null;
    private GoogleMap googleMap;
    private final Calendar calendar = Calendar.getInstance();

    private FusedLocationProviderClient fusedLocation;


    /** Pick image from gallery */
    private final ActivityResultLauncher<String> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) handleImageUri(uri);
            });

    /** Capture photo with camera */
    private final ActivityResultLauncher<Uri> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
                if (Boolean.TRUE.equals(success) && cameraImageUri != null) {
                    handleImageUri(cameraImageUri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_report);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        fusedLocation = LocationServices.getFusedLocationProviderClient(this);

        bindViews();
        setupCategoryDropdown();
        prefillUserName();
        prefillDateTime();
        setupDateTimePickers();
        setupMapFragment();
        fetchGpsLocation();

        findViewById(R.id.btn_back).setOnClickListener(v -> {
            startActivity(new Intent(this, ViewReport.class));
            finish();
        });

        tilLocation.setEndIconOnClickListener(v -> fetchGpsLocation());
        btnUploadImage.setOnClickListener(v -> showImageSourceDialog());
        btnSubmit.setOnClickListener(v -> submitReport());

        findViewById(R.id.btn_remove_image).setOnClickListener(v -> clearImage());
    }
    private void bindViews() {
        spinnerCategory  = findViewById(R.id.spinner_category);
        etName           = findViewById(R.id.et_name);
        etLocation       = findViewById(R.id.et_location);
        tilLocation      = findViewById(R.id.til_location);
        etDate           = findViewById(R.id.et_date);
        etTime           = findViewById(R.id.et_time);
        etDetails        = findViewById(R.id.et_details);
        cardImagePreview = findViewById(R.id.card_image_preview);
        ivPreview        = findViewById(R.id.iv_preview);
        btnUploadImage   = findViewById(R.id.btn_upload_image);
        btnSubmit        = findViewById(R.id.btn_submit);
    }


    private void setupCategoryDropdown() {
        String[] categories = {"Damaged Pipes", "Unusual Behavior", "Miscellaneous"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, categories);
        spinnerCategory.setAdapter(adapter);
    }


    private void prefillUserName() {
        String name = getSharedPreferences(Login.PREFS_NAME, MODE_PRIVATE)
                .getString(Login.KEY_NAME, "");
        etName.setText(name);
    }

    private void prefillDateTime() {
        updateDateField();
        updateTimeField();
    }

    private void setupDateTimePickers() {
        etDate.setOnClickListener(v -> showDatePicker());
        findViewById(R.id.til_date).setOnClickListener(v -> showDatePicker());

        etTime.setOnClickListener(v -> showTimePicker());
        findViewById(R.id.til_time).setOnClickListener(v -> showTimePicker());
    }

    private void showDatePicker() {
        new DatePickerDialog(this,
                (view, year, month, day) -> {
                    calendar.set(Calendar.YEAR,  year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, day);
                    updateDateField();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    private void showTimePicker() {
        new TimePickerDialog(this,
                (view, hour, minute) -> {
                    calendar.set(Calendar.HOUR_OF_DAY, hour);
                    calendar.set(Calendar.MINUTE, minute);
                    updateTimeField();
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false)
                .show();
    }

    private void updateDateField() {
        etDate.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                .format(calendar.getTime()));
    }

    private void updateTimeField() {
        etTime.setText(new SimpleDateFormat("hh:mm a", Locale.getDefault())
                .format(calendar.getTime()));
    }

    private void setupMapFragment() {
        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        if (mapFragment != null) mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        googleMap.getUiSettings().setAllGesturesEnabled(false); // preview only
        googleMap.getUiSettings().setZoomControlsEnabled(false);
    }

    private void updateMapPin(double lat, double lng) {
        if (googleMap == null) return;
        LatLng point = new LatLng(lat, lng);
        googleMap.clear();
        googleMap.addMarker(new MarkerOptions().position(point).title("Incident location"));
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(point, 16f));
    }

    private void fetchGpsLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
            return;
        }

        etLocation.setHint("Fetching location…");

        fusedLocation.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                pickedLat = location.getLatitude();
                pickedLng = location.getLongitude();
                reverseGeocode(pickedLat, pickedLng);
                updateMapPin(pickedLat, pickedLng);
            } else {
                etLocation.setHint("Could not get location. Tap 📍 to retry.");
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void reverseGeocode(double lat, double lng) {
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address addr = addresses.get(0);
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i <= addr.getMaxAddressLineIndex(); i++) {
                    if (i > 0) sb.append(", ");
                    sb.append(addr.getAddressLine(i));
                }
                etLocation.setText(sb.toString());
            } else {
                etLocation.setText(lat + ", " + lng);
            }
        } catch (IOException e) {
            etLocation.setText(lat + ", " + lng);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            fetchGpsLocation();
        }
        if (requestCode == REQUEST_CAMERA_PERMISSION
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            launchCamera();
        }
    }

    private void showImageSourceDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Add photo")
                .setItems(new String[]{"Take a photo", "Choose from gallery"}, (dialog, which) -> {
                    if (which == 0) checkCameraAndLaunch();
                    else            galleryLauncher.launch("image/*");
                })
                .show();
    }

    private void checkCameraAndLaunch() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSION);
        } else {
            launchCamera();
        }
    }

    private void launchCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME,
                "report_" + System.currentTimeMillis() + ".jpg");
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.Images.Media.RELATIVE_PATH,
                Environment.DIRECTORY_PICTURES + "/MADFYA");

        cameraImageUri = getContentResolver().insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        if (cameraImageUri != null) cameraLauncher.launch(cameraImageUri);
    }

    private void handleImageUri(Uri uri) {
        savedImagePath = uri.toString();
        Glide.with(this).load(uri).centerCrop().into(ivPreview);
        cardImagePreview.setVisibility(View.VISIBLE);
        btnUploadImage.setText("Change image");
    }

    private void clearImage() {
        savedImagePath = null;
        cameraImageUri = null;
        ivPreview.setImageDrawable(null);
        cardImagePreview.setVisibility(View.GONE);
        btnUploadImage.setText("Upload image");
    }


    private void submitReport() {
        // Validate
        String category = spinnerCategory.getText().toString().trim();
        String details  = etDetails.getText() != null
                ? etDetails.getText().toString().trim() : "";

        if (category.isEmpty()) {
            Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show();
            return;
        }
        if (details.isEmpty()) {
            etDetails.setError("Please describe what happened");
            etDetails.requestFocus();
            return;
        }

        // Get logged-in user's Firebase key from SharedPreferences
        String userId = getSharedPreferences(Login.PREFS_NAME, MODE_PRIVATE)
                .getString(Login.KEY_USER_ID, "");

        Reports report = new Reports(
                category,
                details,
                category,
                userId,
                pickedLat,
                pickedLng,
                savedImagePath
        );

        report.ReportedTimeStamps = calendar.getTimeInMillis();

        btnSubmit.setEnabled(false);
        btnSubmit.setText("Submitting…");

        // ── INSERT into Firebase — no background thread needed ────────────────
        FirebaseRepository.get().insertReport(report);

        Toast.makeText(this, "Report submitted", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, ViewReport.class));
        finish();
    }
}