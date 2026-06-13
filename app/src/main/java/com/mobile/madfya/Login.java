package com.mobile.madfya;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * Launcher screen. For now it acts as a simple prototype hub that opens the
 * three new screens (Admin, Alerts and Community); swap in the real login flow
 * when it is ready.
 */
public class Login extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        findViewById(R.id.btn_admin).setOnClickListener(v ->
                startActivity(new Intent(this, AdminMain.class)));
        findViewById(R.id.btn_alerts).setOnClickListener(v ->
                startActivity(new Intent(this, Alerts.class)));
        findViewById(R.id.btn_community).setOnClickListener(v ->
                startActivity(new Intent(this, Community.class)));
    }
}
