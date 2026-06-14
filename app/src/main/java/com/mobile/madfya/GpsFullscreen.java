package com.mobile.madfya;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class GpsFullscreen extends AppCompatActivity implements OnMapReadyCallback {

    private double latitude, longitude;
    private String filterName, date, time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gps_fullscreen);

        // Get extras
        latitude   = getIntent().getDoubleExtra("lat", 0);
        longitude  = getIntent().getDoubleExtra("lng", 0);
        filterName = getIntent().getStringExtra("filter");
        date       = getIntent().getStringExtra("date");
        time       = getIntent().getStringExtra("time");

        Toolbar toolbar = findViewById(R.id.toolbarGps);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        ((TextView) findViewById(R.id.tvFullFilter)).setText("Filter: " + filterName);
        ((TextView) findViewById(R.id.tvFullCoords)).setText("GPS: " + latitude + ", " + longitude);
        ((TextView) findViewById(R.id.tvFullDate)).setText("Date: " + date);
        ((TextView) findViewById(R.id.tvFullTime)).setText("Time: " + time);
        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.mapFullscreen);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        LatLng coords = new LatLng(latitude, longitude);
        googleMap.addMarker(new MarkerOptions().position(coords).title(filterName));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coords, 15f));
    }
}