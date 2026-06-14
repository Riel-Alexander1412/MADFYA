package com.mobile.madfya;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.mobile.madfya.data.FirebaseRepository;
import com.mobile.madfya.data.Sensors;
import com.mobile.madfya.databinding.ActivityStatusBinding;

import java.util.ArrayList;
import java.util.List;

public class Status extends AppCompatActivity {

    private ActivityStatusBinding binding;
    private FirebaseRepository repo;

    private <T> List<T> takeLast(List<T> list, int n) {
        if (list.size() <= n) return list;
        return list.subList(list.size() - n, list.size());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityStatusBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.statusToolbar, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        repo = FirebaseRepository.get();

        binding.btnBack.setOnClickListener(v -> {
            finish(); // Or navigate to previous activity
        });

        binding.btnViewHistory.setOnClickListener(v -> {
            startActivity(new Intent(this, History.class));
        });

        loadFirebaseGraphs();

        setupBottomNav();
    }

    private void loadFirebaseGraphs() {
        repo.getAllSensors().observe(this, sensors -> {
            if (sensors == null) return;

            List<Entry> dataA    = new ArrayList<>();
            List<Entry> dataB    = new ArrayList<>();
            List<Entry> dataC    = new ArrayList<>();
            List<Entry> phData   = new ArrayList<>();
            List<Entry> turbData = new ArrayList<>();
            List<Entry> tempData = new ArrayList<>();
            List<Entry> flowData = new ArrayList<>();

            int iMain = 0, iA = 0, iB = 0, iC = 0;

            for (Sensors s : sensors) {
                float val = 0f;
                try { val = s.ph_level != null ? Float.parseFloat(s.ph_level) : 0f; } catch (Exception ignored) {}

                if (s.filterId == 1) {
                    updateStatusBadges(s);
                    updateAlertBanner(s);
                    updateSafetyScore(s);
                    try { phData.add(new Entry(iMain, Float.parseFloat(s.ph_level))); }        catch (Exception ignored) {}
                    try { turbData.add(new Entry(iMain, Float.parseFloat(s.turbidity))); }     catch (Exception ignored) {}
                    try { tempData.add(new Entry(iMain, Float.parseFloat(s.temperature))); }   catch (Exception ignored) {}
                    try { flowData.add(new Entry(iMain, Float.parseFloat(s.water_flow_rate))); } catch (Exception ignored) {}
                    iMain++;
                } else if (s.filterId == 2) {
                    updateSubFilterBadge(binding.subAStatText, s.status);
                    dataA.add(new Entry(iA++, val));
                } else if (s.filterId == 3) {
                    updateSubFilterBadge(binding.subBText, s.status);
                    dataB.add(new Entry(iB++, val));
                } else if (s.filterId == 4) {
                    updateSubFilterBadge(binding.subAText, s.status);
                    dataC.add(new Entry(iC++, val));
                }
            }

            int WINDOW = 10;
            if (!phData.isEmpty())   setupSparkline(binding.PhChart,      takeLast(phData,   WINDOW), Color.parseColor("#FFCC80"), Color.parseColor("#E65100"));
            if (!turbData.isEmpty()) setupSparkline(binding.TurbChart,    takeLast(turbData, WINDOW), Color.parseColor("#80DEEA"), Color.parseColor("#00838F"));
            if (!tempData.isEmpty()) setupSparkline(binding.TempChart,    takeLast(tempData, WINDOW), Color.parseColor("#EF9A9A"), Color.parseColor("#C62828"));
            if (!flowData.isEmpty()) setupSparkline(binding.WaterChart,   takeLast(flowData, WINDOW), Color.parseColor("#A5D6A7"), Color.parseColor("#2E7D32"));
            if (!dataA.isEmpty())    setupSparkline(binding.chartFilterA, takeLast(dataA,    WINDOW), -1, Color.parseColor("#005A9E"));
            if (!dataB.isEmpty())    setupSparkline(binding.chartFilterB, takeLast(dataB,    WINDOW), -1, Color.parseColor("#005A9E"));
            if (!dataC.isEmpty())    setupSparkline(binding.chartFilterC, takeLast(dataC,    WINDOW), -1, Color.parseColor("#005A9E"));
        });
    }

    private void updateSafetyScore(Sensors s) {
        int score = 0;
        try { double ph = Double.parseDouble(s.ph_level);
            if (ph >= 6.5 && ph <= 8.5) score += 25; } catch (Exception ignored) {}
        try { double turb = Double.parseDouble(s.turbidity);
            if (turb <= 4) score += 25; } catch (Exception ignored) {}
        try { double temp = Double.parseDouble(s.temperature);
            if (temp >= 20 && temp <= 30) score += 25; } catch (Exception ignored) {}
        try { double flow = Double.parseDouble(s.water_flow_rate);
            if (flow >= 50) score += 25; } catch (Exception ignored) {}

        binding.tvWaterSafetyScore.setText(score + "%");

        if (score >= 75) {
            binding.tvSafetyLabel.setText("Safe For Consumption ✓");
            binding.tvSafetyLabel.setTextColor(Color.parseColor("#66BB6A"));
        } else if (score >= 50) {
            binding.tvSafetyLabel.setText("Use With Caution ⚠");
            binding.tvSafetyLabel.setTextColor(Color.parseColor("#F57C00"));
        } else {
            binding.tvSafetyLabel.setText("Not Safe For Consumption ✗");
            binding.tvSafetyLabel.setTextColor(Color.parseColor("#EF5350"));
        }
    }

    private void updateAlertBanner(Sensors s) {
        List<String> issues = new ArrayList<>();

        try {
            double ph = Double.parseDouble(s.ph_level);
            if (ph < 6.5)      issues.add("pH is too low (" + s.ph_level + " pH)");
            else if (ph > 8.5) issues.add("pH is above safe threshold (" + s.ph_level + " pH)");
        } catch (Exception ignored) {}

        try {
            double turb = Double.parseDouble(s.turbidity);
            if (turb > 4) issues.add("Turbidity is high (" + s.turbidity + " NTU)");
        } catch (Exception ignored) {}

        try {
            double temp = Double.parseDouble(s.temperature);
            if (temp < 20)      issues.add("Temperature is too low (" + s.temperature + "°C)");
            else if (temp > 30) issues.add("Temperature is too high (" + s.temperature + "°C)");
        } catch (Exception ignored) {}

        try {
            double flow = Double.parseDouble(s.water_flow_rate);
            if (flow < 50) issues.add("Water flow is low (" + s.water_flow_rate + "L)");
        } catch (Exception ignored) {}

        if (issues.isEmpty()) {
            binding.tvAlertBanner.setVisibility(android.view.View.GONE);
        } else {
            binding.tvAlertBanner.setVisibility(android.view.View.VISIBLE);
            binding.tvAlertBanner.setText("⚠ " + android.text.TextUtils.join(" • ", issues) + ". Filter inspection recommended.");
        }
    }

    private void setupBottomNav() {
        if (binding.bottomNav != null) {
            // Set current selected item to Status
            binding.bottomNav.setSelectedItemId(R.id.menu_status);

            binding.bottomNav.setOnItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == R.id.menu_status) {
                    return true;  // Already on Status
                }
                if (id == R.id.menu_dashboard) {
                    startActivity(new Intent(this, Dashboard.class));
                    finish(); // Optional: close Status when navigating
                    return true;
                }
                if (id == R.id.menu_community) {
                    startActivity(new Intent(this, Community.class));
                    finish(); // Optional: close Status when navigating
                    return true;
                }
                return false;
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (binding.bottomNav != null) {
            binding.bottomNav.setSelectedItemId(R.id.menu_status);
        }
    }

    private void setupSparkline(LineChart lineChart, List<Entry> dataPoints, int fillColor, int lineColor) {
        if (lineChart == null || dataPoints == null || dataPoints.isEmpty()) return;

        LineDataSet dataSet = new LineDataSet(dataPoints, "Water Usage");
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setColor(lineColor);
        dataSet.setLineWidth(2f);
        dataSet.setDrawCircles(false);
        dataSet.setDrawValues(false);
        dataSet.setDrawFilled(true);

        if (fillColor == -1) {
            Drawable fadeGradient = ContextCompat.getDrawable(this, R.drawable.bg_graph_gradient);
            if (fadeGradient != null) {
                dataSet.setFillDrawable(fadeGradient);
            } else {
                dataSet.setFillColor(Color.parseColor("#20005A9E"));
            }
        } else {
            dataSet.setFillColor(fillColor);
            dataSet.setFillAlpha(255);
        }

        lineChart.setData(new LineData(dataSet));
        lineChart.getDescription().setEnabled(false);
        lineChart.getLegend().setEnabled(false);
        lineChart.setDrawGridBackground(false);
        lineChart.setTouchEnabled(false);
        lineChart.getXAxis().setDrawGridLines(false);
        lineChart.getXAxis().setDrawAxisLine(false);
        lineChart.getXAxis().setDrawLabels(false);
        lineChart.getAxisLeft().setDrawGridLines(false);
        lineChart.getAxisLeft().setDrawAxisLine(false);
        lineChart.getAxisLeft().setDrawLabels(false);
        lineChart.getAxisRight().setDrawGridLines(false);
        lineChart.getAxisRight().setDrawAxisLine(false);
        lineChart.getAxisRight().setDrawLabels(false);
        lineChart.setVisibility(android.view.View.VISIBLE);
        lineChart.invalidate();
    }

    private void updateStatusBadges(Sensors s) {
        if (s == null) return;

        try {
            double ph = s.ph_level != null ? Double.parseDouble(s.ph_level) : 0;
            if (ph < 6.5 || ph > 8.5) {
                applyBadgeStyle(binding.phStatusText, binding.phStatusBadge, "High", Color.parseColor("#F57C00"), R.drawable.ic_warning);
            } else {
                applyBadgeStyle(binding.phStatusText, binding.phStatusBadge, "Normal", Color.parseColor("#388E3C"), R.drawable.ic_check_circle);
            }
        } catch (Exception ignored) {}

        try {
            double turb = s.turbidity != null ? Double.parseDouble(s.turbidity) : 0;
            if (turb > 4) {
                applyBadgeStyle(binding.TurbStatusText, binding.TurbStatusBadge, "High", Color.parseColor("#F57C00"), R.drawable.ic_warning);
            } else {
                applyBadgeStyle(binding.TurbStatusText, binding.TurbStatusBadge, "Normal", Color.parseColor("#66BB6A"), R.drawable.ic_check_circle);
            }
        } catch (Exception ignored) {}

        try {
            double temp = s.temperature != null ? Double.parseDouble(s.temperature) : 0;
            if (temp < 20 || temp > 30) {
                applyBadgeStyle(binding.TempStatusText, binding.TempStatusBagde, "Abnormal", Color.parseColor("#EF5350"), R.drawable.ic_warning);
            } else {
                applyBadgeStyle(binding.TempStatusText, binding.TempStatusBagde, "Normal", Color.parseColor("#66BB6A"), R.drawable.ic_check_circle);
            }
        } catch (Exception ignored) {}

        try {
            double flow = s.water_flow_rate != null ? Double.parseDouble(s.water_flow_rate) : 0;
            if (flow < 50) {
                applyBadgeStyle(binding.waterStatusText, binding.waterStatusBadge, "Low", Color.parseColor("#EF5350"), R.drawable.ic_warning);
            } else {
                applyBadgeStyle(binding.waterStatusText, binding.waterStatusBadge, "Normal", Color.parseColor("#66BB6A"), R.drawable.ic_check_circle);
            }
        } catch (Exception ignored) {}
    }

    private void updateSubFilterBadge(android.widget.TextView textView, String status) {
        if (status == null) return;
        android.graphics.drawable.GradientDrawable bg = new android.graphics.drawable.GradientDrawable();
        bg.setCornerRadii(new float[]{0, 0, 16f, 16f, 16f, 16f, 0, 0});

        android.graphics.drawable.Drawable warning = ContextCompat.getDrawable(this, R.drawable.ic_warning);
        android.graphics.drawable.Drawable tick    = ContextCompat.getDrawable(this, R.drawable.ic_tick_sub);

        int size = (int) (14 * getResources().getDisplayMetrics().density);
        if (warning != null) warning.setBounds(0, 0, size, size);
        if (tick    != null) tick.setBounds(0, 0, size, size);

        switch (status.toUpperCase()) {
            case "WARNING":
                textView.setText("Warning");
                textView.setTextColor(Color.BLACK);
                textView.setCompoundDrawables(warning, null, null, null);
                textView.setCompoundDrawableTintList(android.content.res.ColorStateList.valueOf(Color.BLACK));
                bg.setColor(Color.parseColor("#FFE0B2"));
                break;
            case "DISABLED":
                textView.setText("Offline");
                textView.setTextColor(Color.BLACK);
                textView.setCompoundDrawables(warning, null, null, null);
                textView.setCompoundDrawableTintList(android.content.res.ColorStateList.valueOf(Color.BLACK));
                bg.setColor(Color.parseColor("#FFCDD2"));
                break;
            default:
                textView.setText("Good");
                textView.setTextColor(Color.BLACK);
                textView.setCompoundDrawables(tick, null, null, null);
                textView.setCompoundDrawableTintList(android.content.res.ColorStateList.valueOf(Color.BLACK));
                bg.setColor(Color.parseColor("#C8E6C9"));
        }
        textView.setBackground(bg);
    }

    private void applyBadgeStyle(android.widget.TextView textView, android.widget.ImageView icon,
                                 String label, int textColor, int iconRes) {
        textView.setText(label);
        textView.setTextColor(textColor);
        icon.setImageResource(iconRes);
        icon.setColorFilter(textColor);
    }
}