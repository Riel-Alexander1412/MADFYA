package com.mobile.madfya;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.mobile.madfya.data.AppDatabase;
import com.mobile.madfya.databinding.ActivityStatusBinding;

import java.util.ArrayList;
import java.util.List;

public class Status extends AppCompatActivity {

    private ActivityStatusBinding binding;
    private <T> List<T> takeLast(List<T> list, int n) {
        if (list.size() <= n) return list;
        return list.subList(list.size() - n, list.size());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStatusBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 1. Toolbar and History button setup
        if (binding.statusToolbar != null) {
            binding.statusToolbar.setNavigationOnClickListener(v -> onBackPressed());
        }
        binding.btnViewHistory.setOnClickListener(v -> {
            startActivity(new Intent(this, History.class));
        });
        seedTestData();

        // 3. Setup Bottom Navigation
        if (binding.statusBottomNav != null) {
            // Set the active item to Status silently
            binding.statusBottomNav.getMenu().findItem(R.id.menu_alerts_status).setChecked(true);

        }
    }

    private void loadRoomGraphs() {
        com.mobile.madfya.data.AppDatabase db = com.mobile.madfya.data.AppDatabase.get(this);
        db.sensorsDao().getAll().observe(this, sensors -> {
            List<Entry> dataA = new ArrayList<>();
            List<Entry> dataB = new ArrayList<>();
            List<Entry> dataC = new ArrayList<>();
            List<Entry> phData = new ArrayList<>();
            List<Entry> turbData = new ArrayList<>();
            List<Entry> tempData = new ArrayList<>();
            List<Entry> flowData = new ArrayList<>();
            int iMain = 0;
            int iA = 0, iB = 0, iC = 0;

            for (com.mobile.madfya.data.Sensors s : sensors) {
                float val = 0f;
                try { val = s.ph_level != null ? Float.parseFloat(s.ph_level) : 0f; } catch (Exception ignored) {}

                if (s.filterId == 1) {
                    updateStatusBadges(s);
                    try { phData.add(new Entry(iMain, Float.parseFloat(s.ph_level))); } catch (Exception ignored) {}
                    try { turbData.add(new Entry(iMain, Float.parseFloat(s.turbidity))); } catch (Exception ignored) {}
                    try { tempData.add(new Entry(iMain, Float.parseFloat(s.temperature))); } catch (Exception ignored) {}
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
            if (!phData.isEmpty())    setupSparkline(binding.PhChart,      takeLast(phData, WINDOW));
            if (!turbData.isEmpty())  setupSparkline(binding.TurbChart,    takeLast(turbData, WINDOW));
            if (!tempData.isEmpty())  setupSparkline(binding.TempChart,    takeLast(tempData, WINDOW));
            if (!flowData.isEmpty())  setupSparkline(binding.WaterChart,   takeLast(flowData, WINDOW));
            if (!dataA.isEmpty())     setupSparkline(binding.chartFilterA, takeLast(dataA, WINDOW));
            if (!dataB.isEmpty())     setupSparkline(binding.chartFilterB, takeLast(dataB, WINDOW));
            if (!dataC.isEmpty())     setupSparkline(binding.chartFilterC, takeLast(dataC, WINDOW));
        });
    }

    private void setupSparkline(LineChart lineChart, List<Entry> dataPoints) {
        if (lineChart == null || dataPoints == null || dataPoints.isEmpty()) return;

        // Create line collection dataset
        LineDataSet dataSet = new LineDataSet(dataPoints, "Water Usage");

        // CUBIC_BEZIER creates the smooth curved wave geometry matching Figma
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setColor(Color.parseColor("#005A9E")); // Set line stroke to theme primary blue
        dataSet.setLineWidth(2f);
        dataSet.setDrawCircles(false); // Hide individual node dots
        dataSet.setDrawValues(false);  // Hide value metric strings on top of line

        // Apply smooth fading area layout under the line curve
        dataSet.setDrawFilled(true);
        Drawable fadeGradient = ContextCompat.getDrawable(this, R.drawable.bg_graph_gradient);
        if (fadeGradient != null) {
            dataSet.setFillDrawable(fadeGradient);
        } else {
            dataSet.setFillColor(Color.parseColor("#20005A9E")); // Backup semi-transparent tint block
        }

        lineChart.setData(new LineData(dataSet));

        // Strip default chart styling elements (Grid meshes, numbers, interactive lines)
        lineChart.getDescription().setEnabled(false);
        lineChart.getLegend().setEnabled(false);
        lineChart.setDrawGridBackground(false);
        lineChart.setTouchEnabled(false); // Keeps view flat and non-draggable

        // Hide X-Axis elements
        lineChart.getXAxis().setDrawGridLines(false);
        lineChart.getXAxis().setDrawAxisLine(false);
        lineChart.getXAxis().setDrawLabels(false);

        // Hide Left Y-Axis elements
        lineChart.getAxisLeft().setDrawGridLines(false);
        lineChart.getAxisLeft().setDrawAxisLine(false);
        lineChart.getAxisLeft().setDrawLabels(false);

        // Hide Right Y-Axis elements
        lineChart.getAxisRight().setDrawGridLines(false);
        lineChart.getAxisRight().setDrawAxisLine(false);
        lineChart.getAxisRight().setDrawLabels(false);
        lineChart.setVisibility(android.view.View.VISIBLE);
        // Force viewport drawing pass
        lineChart.invalidate();
    }

    private void updateStatusBadges(com.mobile.madfya.data.Sensors s) {
        if (s == null) return;

        // pH (normal: 6.5–8.5)
        try {
            double ph = s.ph_level != null ? Double.parseDouble(s.ph_level) : 0;
            if (ph < 6.5 || ph > 8.5) {
                applyBadgeStyle(binding.phStatusText, binding.phStatusBadge,
                        "High", Color.parseColor("#F57C00"), R.drawable.ic_warning);
            } else {
                applyBadgeStyle(binding.phStatusText, binding.phStatusBadge,
                        "Normal", Color.parseColor("#388E3C"), R.drawable.ic_check_circle);
            }
        } catch (Exception ignored) {}

        // Turbidity (normal: <4 NTU)
        try {
            double turb = s.turbidity != null ? Double.parseDouble(s.turbidity) : 0;
            if (turb > 4) {
                applyBadgeStyle(binding.TurbStatusText, binding.TurbStatusBadge,
                        "High", Color.parseColor("#F57C00"), R.drawable.ic_warning);
            } else {
                applyBadgeStyle(binding.TurbStatusText, binding.TurbStatusBadge,
                        "Normal", Color.parseColor("#66BB6A"), R.drawable.ic_check_circle);
            }
        } catch (Exception ignored) {}

        // Temperature (normal: 20–30°C)
        try {
            double temp = s.temperature != null ? Double.parseDouble(s.temperature) : 0;
            if (temp < 20 || temp > 30) {
                applyBadgeStyle(binding.TempStatusText, binding.TempStatusBagde,
                        "Abnormal", Color.parseColor("#EF5350"), R.drawable.ic_warning);
            } else {
                applyBadgeStyle(binding.TempStatusText, binding.TempStatusBagde,
                        "Normal", Color.parseColor("#66BB6A"), R.drawable.ic_check_circle);
            }
        } catch (Exception ignored) {}

        // Water flow (normal: >50L)
        try {
            double flow = s.water_flow_rate != null ? Double.parseDouble(s.water_flow_rate) : 0;
            if (flow < 50) {
                applyBadgeStyle(binding.waterStatusText, binding.waterStatusBadge,
                        "Low", Color.parseColor("#EF5350"), R.drawable.ic_warning);
            } else {
                applyBadgeStyle(binding.waterStatusText, binding.waterStatusBadge,
                        "Normal", Color.parseColor("#66BB6A"), R.drawable.ic_check_circle);
            }
        } catch (Exception ignored) {}
    }
    private void updateSubFilterBadge(android.widget.TextView textView, String status) {
        if (status == null) return;
        android.graphics.drawable.GradientDrawable bg = new android.graphics.drawable.GradientDrawable();
        bg.setCornerRadii(new float[]{0, 0, 16f, 16f, 16f, 16f, 0, 0});

        android.graphics.drawable.Drawable warning = ContextCompat.getDrawable(this, R.drawable.ic_warning);
        android.graphics.drawable.Drawable tick = ContextCompat.getDrawable(this, R.drawable.ic_tick_sub);

        int size = (int) (14 * getResources().getDisplayMetrics().density); // 14dp, adjust as needed

        if (warning != null) warning.setBounds(0, 0, size, size);
        if (tick != null) tick.setBounds(0, 0, size, size);

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
    private void seedTestData() {
        com.mobile.madfya.data.AppDatabase db = com.mobile.madfya.data.AppDatabase.get(this);
        AppDatabase.dbExecutor.execute(() -> {
            // Clear existing data first if you want a clean slate every boot
            // db.sensorsDao().deleteAll();

            long now = System.currentTimeMillis();
            long tenMinutes = 10 * 60 * 1000;

            // --- FILTER 1: MAIN FILTER HISTORICAL DATA ---
            String[] mainPh = {"6.8", "7.2", "7.5", "8.1", "8.7"};
            String[] mainTurb = {"1.5", "2.0", "2.5", "2.9", "3.1"};
            String[] mainTemp = {"24.0", "25.5", "26.0", "27.2", "28.5"};
            String[] mainFlow = {"90", "100", "110", "105", "120"};

            for (int i = 0; i < 5; i++) {
                com.mobile.madfya.data.Sensors s = new com.mobile.madfya.data.Sensors();
                s.filterId = 1;
                s.ph_level = mainPh[i];
                s.turbidity = mainTurb[i];
                s.temperature = mainTemp[i];
                s.water_flow_rate = mainFlow[i];
                s.status = "OPERATIONAL";
                s.CurrentTimeStamp = now - ((4 - i) * tenMinutes);
                db.sensorsDao().insert(s);
            }

            // --- FILTER 2: SUB-FILTER A HISTORICAL DATA ---
            String[] subAPh = {"6.9", "7.0", "7.2", "7.1", "7.1"};
            for (int i = 0; i < 5; i++) {
                com.mobile.madfya.data.Sensors s = new com.mobile.madfya.data.Sensors();
                s.filterId = 2;
                s.ph_level = subAPh[i];
                s.turbidity = "2.0";
                s.temperature = "27.0";
                s.water_flow_rate = "80";
                s.status = "OPERATIONAL";
                s.CurrentTimeStamp = now - ((4 - i) * tenMinutes);
                db.sensorsDao().insert(s);
            }

            // --- FILTER 3: SUB-FILTER B HISTORICAL DATA ---
            String[] subBPh = {"7.4", "7.2", "7.0", "6.8", "6.9"};
            for (int i = 0; i < 5; i++) {
                com.mobile.madfya.data.Sensors s = new com.mobile.madfya.data.Sensors();
                s.filterId = 3;
                s.ph_level = subBPh[i];
                s.turbidity = "5.5";
                s.temperature = "31.0";
                s.water_flow_rate = "40";
                s.status = "WARNING";
                s.CurrentTimeStamp = now - ((4 - i) * tenMinutes);
                db.sensorsDao().insert(s);
            }

            // --- FILTER 4: SUB-FILTER C HISTORICAL DATA ---
            String[] subCPh = {"7.0", "7.1", "7.3", "7.5", "7.4"};
            for (int i = 0; i < 5; i++) {
                com.mobile.madfya.data.Sensors s = new com.mobile.madfya.data.Sensors();
                s.filterId = 4;
                s.ph_level = subCPh[i];
                s.turbidity = "1.8";
                s.temperature = "26.0";
                s.water_flow_rate = "95";
                s.status = "OPERATIONAL";
                s.CurrentTimeStamp = now - ((4 - i) * tenMinutes);
                db.sensorsDao().insert(s);
            }
            runOnUiThread(this::loadRoomGraphs);
        });
    }
}