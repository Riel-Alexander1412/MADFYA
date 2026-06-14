package com.mobile.madfya;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class EditLog extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST = 1001;
    private static final String[] FILTERS = {"Main Filter", "Sub Filter A", "Sub Filter B", "Sub Filter C"};

    private GoogleMap googleMap;
    private Marker currentMarker;
    private FusedLocationProviderClient fusedLocationClient;

    private double latitude, longitude;
    private int logPosition;

    private AutoCompleteTextView etFilterName;
    private TextInputEditText etPh, etTurbidity, etTemperature, etUsage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_log);

        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.toolbarEditLog), (v, insets) -> {
                    androidx.core.graphics.Insets bars = insets.getInsets(
                            androidx.core.view.WindowInsetsCompat.Type.systemBars());
                    v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
                    return insets;
                });

        latitude    = getIntent().getDoubleExtra("lat", 0);
        longitude   = getIntent().getDoubleExtra("lng", 0);
        logPosition = getIntent().getIntExtra("position", -1);
        int filterId = getIntent().getIntExtra("filterId", 1);

        String ph          = getIntent().getStringExtra("ph");
        String turbidity   = getIntent().getStringExtra("turbidity");
        String temperature = getIntent().getStringExtra("temperature");
        String usage       = getIntent().getStringExtra("usage");

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        etFilterName  = findViewById(R.id.etFilterName);
        etPh          = findViewById(R.id.etPh);
        etTurbidity   = findViewById(R.id.etTurbidity);
        etTemperature = findViewById(R.id.etTemperature);
        etUsage       = findViewById(R.id.etUsage);

        // setup dropdown
        android.widget.ArrayAdapter<String> filterAdapter = new android.widget.ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, FILTERS);
        etFilterName.setAdapter(filterAdapter);
        etFilterName.setText(FILTERS[filterId - 1], false);

        etPh.setText(ph);
        etTurbidity.setText(turbidity);
        etTemperature.setText(temperature);
        etUsage.setText(usage);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.mapEditLog);
        if (mapFragment != null) mapFragment.getMapAsync(this);

        findViewById(R.id.btnCurrentLocation).setOnClickListener(v -> fetchCurrentLocation());
        findViewById(R.id.btnSaveLog).setOnClickListener(v -> saveLog());
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        LatLng initial = new LatLng(latitude, longitude);
        currentMarker = googleMap.addMarker(new MarkerOptions().position(initial).draggable(true));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initial, 15f));

        googleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override public void onMarkerDragStart(@NonNull Marker marker) {}
            @Override public void onMarkerDrag(@NonNull Marker marker) {}
            @Override
            public void onMarkerDragEnd(@NonNull Marker marker) {
                latitude  = marker.getPosition().latitude;
                longitude = marker.getPosition().longitude;
            }
        });

        googleMap.setOnMapClickListener(latLng -> {
            latitude  = latLng.latitude;
            longitude = latLng.longitude;
            if (currentMarker != null) currentMarker.setPosition(latLng);
            else currentMarker = googleMap.addMarker(
                    new MarkerOptions().position(latLng).draggable(true));
        });
    }

    private void fetchCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST);
            return;
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                latitude  = location.getLatitude();
                longitude = location.getLongitude();
                LatLng latLng = new LatLng(latitude, longitude);
                if (currentMarker != null) currentMarker.setPosition(latLng);
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f));
            } else {
                Toast.makeText(this, "Could not get current location", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            fetchCurrentLocation();
        }
    }

    private void saveLog() {
        String selectedFilter = etFilterName.getText() != null ? etFilterName.getText().toString().trim() : "";
        String phStr          = etPh.getText() != null ? etPh.getText().toString().trim() : "0";
        String turbStr        = etTurbidity.getText() != null ? etTurbidity.getText().toString().trim() : "0";
        String tempStr        = etTemperature.getText() != null ? etTemperature.getText().toString().trim() : "0";
        String usageStr       = etUsage.getText() != null ? etUsage.getText().toString().trim() : "0";

        if (selectedFilter.isEmpty()) {
            etFilterName.setError("Please select a filter");
            return;
        }

        int selectedFilterId = java.util.Arrays.asList(FILTERS).indexOf(selectedFilter) + 1;

        android.content.Intent result = new android.content.Intent();
        result.putExtra("position",    logPosition);
        result.putExtra("filterId",    selectedFilterId);
        result.putExtra("filterName",  selectedFilter);
        result.putExtra("ph",          Double.parseDouble(phStr));
        result.putExtra("turbidity",   Double.parseDouble(turbStr));
        result.putExtra("temperature", Double.parseDouble(tempStr));
        result.putExtra("usage",       Double.parseDouble(usageStr));
        result.putExtra("lat",         latitude);
        result.putExtra("lng",         longitude);
        result.putExtra("gpsCoords",   latitude + ", " + longitude);
        setResult(RESULT_OK, result);
        finish();
    }
}