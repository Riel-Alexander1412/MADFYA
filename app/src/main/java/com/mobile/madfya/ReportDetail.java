package com.mobile.madfya;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.mobile.madfya.data.FirebaseRepository;
import com.mobile.madfya.data.Reports;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReportDetail extends AppCompatActivity {

    private MapView mapView;
    private FirebaseRepository firebaseRepo;
    private String reportFirebaseKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_detail);

        firebaseRepo = FirebaseRepository.get();

        Toolbar toolbar = findViewById(R.id.toolbarReportDetail);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }

        mapView = findViewById(R.id.mapDetailView);
        mapView.onCreate(savedInstanceState);

        // Get report ID (firebaseKey) from intent
        reportFirebaseKey = getIntent().getStringExtra("report_id");
        if (reportFirebaseKey == null || reportFirebaseKey.isEmpty()) {
            finish();
            return;
        }

        loadReportDetails();
    }

    private void loadReportDetails() {
        // Use findFirst() with stream to get the report without modifying a variable
        firebaseRepo.getAllReports().observe(this, reports -> {
            if (reports == null || reports.isEmpty()) {
                finish();
                return;
            }

            // Use stream to find the matching report (effectively final)
            reports.stream()
                    .filter(r -> reportFirebaseKey.equals(r.firebaseKey))
                    .findFirst()
                    .ifPresentOrElse(report -> {
                        displayReport(report);
                    }, () -> {
                        finish();
                    });
        });
    }

    private void displayReport(Reports report) {
        Toolbar toolbar = findViewById(R.id.toolbarReportDetail);
        if (toolbar != null) {
            toolbar.setTitle(report.title != null ? report.title : "Report");
        }

        setText(R.id.tvDetailTitle, report.title);
        setText(R.id.tvDetailCategory, report.category);
        setText(R.id.tvDetailPoster, "Reported by user #" + report.ReportedBy);
        setText(R.id.tvDetailDescription, report.description);

        if (report.ReportedTimeStamps > 0) {
            setText(R.id.tvDetailTimestamp,
                    new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                            .format(new Date(report.ReportedTimeStamps)));
        }

        // Handle location
        if (report.latitude != 0 || report.longitude != 0) {
            setText(R.id.tvDetailLocation, report.latitude + ", " + report.longitude);
            mapView.setVisibility(View.VISIBLE);
            mapView.getMapAsync(googleMap -> {
                LatLng point = new LatLng(report.latitude, report.longitude);
                googleMap.addMarker(new MarkerOptions()
                        .position(point)
                        .title(report.title != null ? report.title : "Report"));
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(point, 15f));
            });
        } else {
            findViewById(R.id.tvDetailLocationLabel).setVisibility(View.GONE);
            findViewById(R.id.tvDetailLocation).setVisibility(View.GONE);
            mapView.setVisibility(View.GONE);
        }

        // Handle image
        ImageView ivPhoto = findViewById(R.id.ivDetailPhoto);
        if (report.ImagePath != null && !report.ImagePath.isEmpty()) {
            ivPhoto.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(report.ImagePath) // Firebase Storage URL
                    .centerCrop()
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_report_image)
                    .into(ivPhoto);
        } else {
            ivPhoto.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mapView != null) mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mapView != null) mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mapView != null) mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mapView != null) mapView.onLowMemory();
    }

    private void setText(int viewId, String text) {
        TextView tv = findViewById(viewId);
        if (tv != null) tv.setText(text != null ? text : "—");
    }
}