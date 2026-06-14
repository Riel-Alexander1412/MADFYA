package com.mobile.madfya;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

public class WaterTipDetailActivity extends AppCompatActivity {
    private TextView titleTextView, descriptionTextView;
    private ImageView detailTipImageView;
    private Button watchVideoButton;
    private String youtubeUrl;

    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_water_tip_detail);

        ImageView backArrow = findViewById(R.id.backArrowIcon);
        if (backArrow != null){
            backArrow.setOnClickListener(v -> finish());
        }

        titleTextView = findViewById(R.id.detailTitleTextView);
        descriptionTextView = findViewById(R.id.detailDescriptionTextView);
        detailTipImageView = findViewById(R.id.detailTipImageView);
        watchVideoButton = findViewById(R.id.watchVideoButton);
        String title = getIntent().getStringExtra("tip_title");
        String description = getIntent().getStringExtra("tip_description");
        youtubeUrl = getIntent().getStringExtra("tip_youtube_url");

        int imageResId = getIntent().getIntExtra("tip_image", R.drawable.ic_launcher_background);
        titleTextView.setText(title);
        descriptionTextView.setText(description);
        detailTipImageView.setImageResource(imageResId);

        watchVideoButton.setOnClickListener(v -> {
            Intent youtubeIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(youtubeUrl));
            startActivity(youtubeIntent);
        });

    }
}
