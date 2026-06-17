package com.mobile.madfya;

import android.graphics.Color;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.material.chip.ChipGroup;
import com.mobile.madfya.data.FirebaseRepository;

import java.util.ArrayList;
import java.util.List;

public class History extends AppCompatActivity {

    private RecyclerView rvHistoricalLedger;
    private SensorLogAdapter adapter;
    private List<SensorLog> logs;
    private LineChart masterHistoricalChart;
    private ChipGroup chipGroupMetrics;
    private static final int EDIT_REQUEST = 100;

    private FirebaseRepository repo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.toolbarHistory), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        rvHistoricalLedger    = findViewById(R.id.rvHistoricalLedger);
        masterHistoricalChart = findViewById(R.id.masterHistoricalChart);
        chipGroupMetrics      = findViewById(R.id.chipGroupMetrics);
        String role = getSharedPreferences(Login.PREFS_NAME, MODE_PRIVATE)
                .getString(Login.KEY_ROLE, null);
        boolean isAdmin = Login.ROLE_ADMIN.equals(role) || Login.ROLE_MAINTENANCE.equals(role);

        logs    = new ArrayList<>();
        adapter = new SensorLogAdapter(logs, isAdmin);
        rvHistoricalLedger.setLayoutManager(new LinearLayoutManager(this));
        rvHistoricalLedger.setAdapter(adapter);

        repo = FirebaseRepository.get();

        // Observe sensors from Firebase
        repo.getAllSensors().observe(this, sensors -> {
            logs.clear();

            // take last 50 entries
            List<com.mobile.madfya.data.Sensors> limited = sensors.size() > 50
                    ? sensors.subList(sensors.size() - 50, sensors.size())
                    : sensors;

            for (com.mobile.madfya.data.Sensors s : limited) {
                String name = SensorUtil.getSensorName(s.filterId);
                String coords = s.latitude + ", " + s.longitude;
                String date = android.text.format.DateFormat.format("dd/MM/yyyy", s.CurrentTimeStamp).toString();
                String time = android.text.format.DateFormat.format("hh:mm a", s.CurrentTimeStamp).toString();
                double ph = s.ph_level != null ? Double.parseDouble(s.ph_level) : 0;
                double turbidity = s.turbidity != null ? Double.parseDouble(s.turbidity) : 0;
                double temperature = s.temperature != null ? Double.parseDouble(s.temperature) : 0;
                double usage = s.water_flow_rate != null ? Double.parseDouble(s.water_flow_rate) : 0;
                logs.add(new SensorLog(s.filterId, name, coords, date, time, ph, turbidity, temperature, usage, s.latitude, s.longitude));
            }
            adapter.notifyDataSetChanged();
            loadChart("ph", Color.parseColor("#E65100"), Color.parseColor("#FFCC80"));
        });

        // Chip listener
        chipGroupMetrics.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            if (id == R.id.chipPh)
                loadChart("ph",          Color.parseColor("#E65100"), Color.parseColor("#FFCC80"));
            else if (id == R.id.chipTurbidity)
                loadChart("turbidity",   Color.parseColor("#00838F"), Color.parseColor("#80DEEA"));
            else if (id == R.id.chipTemperature)
                loadChart("temperature", Color.parseColor("#C62828"), Color.parseColor("#EF9A9A"));
            else if (id == R.id.chipUsage)
                loadChart("usage",       Color.parseColor("#2E7D32"), Color.parseColor("#A5D6A7"));
        });
    }

    private void loadChart(String metric, int lineColor, int fillColor) {
        List<Float> newValues = new ArrayList<>();
        for (SensorLog log : logs) {
            float val;
            switch (metric) {
                case "turbidity":   val = (float) log.turbidity;   break;
                case "temperature": val = (float) log.temperature; break;
                case "usage":       val = (float) log.usage;       break;
                default:            val = (float) log.ph;          break;
            }
            newValues.add(val);
        }

        List<Float> oldValues = new ArrayList<>();
        if (masterHistoricalChart.getData() != null &&
                masterHistoricalChart.getData().getDataSetCount() > 0) {
            LineDataSet existing = (LineDataSet) masterHistoricalChart.getData().getDataSetByIndex(0);
            for (int i = 0; i < existing.getEntryCount(); i++) {
                oldValues.add(existing.getEntryForIndex(i).getY());
            }
        }
        while (oldValues.size() < newValues.size()) oldValues.add(0f);

        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < newValues.size(); i++)
            entries.add(new Entry(i, i < oldValues.size() ? oldValues.get(i) : 0f));

        LineDataSet dataSet = new LineDataSet(entries, metric.toUpperCase());
        dataSet.setColor(lineColor);
        dataSet.setFillColor(fillColor);
        dataSet.setFillAlpha(80);
        dataSet.setDrawFilled(true);
        dataSet.setDrawCircles(false);
        dataSet.setLineWidth(2f);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setDrawValues(false);

        float maxVal = 0f;
        for (float v : newValues) if (v > maxVal) maxVal = v;
        int duration = (int) Math.min(Math.max(maxVal * 10, 400), 2000);

        masterHistoricalChart.setData(new LineData(dataSet));
        masterHistoricalChart.getDescription().setEnabled(false);
        masterHistoricalChart.getLegend().setEnabled(false);
        masterHistoricalChart.getXAxis().setDrawGridLines(false);
        masterHistoricalChart.getAxisRight().setEnabled(false);
        masterHistoricalChart.fitScreen();

        switch (metric) {
            case "turbidity":
                masterHistoricalChart.getAxisLeft().setAxisMinimum(0f);
                masterHistoricalChart.getAxisLeft().setAxisMaximum(10f);
                break;
            case "temperature":
                masterHistoricalChart.getAxisLeft().setAxisMinimum(15f);
                masterHistoricalChart.getAxisLeft().setAxisMaximum(40f);
                break;
            case "usage":
                masterHistoricalChart.getAxisLeft().setAxisMinimum(0f);
                masterHistoricalChart.getAxisLeft().setAxisMaximum(150f);
                break;
            default: // ph
                masterHistoricalChart.getAxisLeft().setAxisMinimum(0f);
                masterHistoricalChart.getAxisLeft().setAxisMaximum(14f);
                break;
        }

        List<Float> finalOld = new ArrayList<>(oldValues);
        android.animation.ValueAnimator animator = android.animation.ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(duration);
        animator.setInterpolator(new android.view.animation.DecelerateInterpolator());
        animator.addUpdateListener(anim -> {
            float progress = (float) anim.getAnimatedValue();
            for (int i = 0; i < newValues.size(); i++) {
                float from = i < finalOld.size() ? finalOld.get(i) : 0f;
                float to   = newValues.get(i);
                dataSet.getEntryForIndex(i).setY(from + (to - from) * progress);
            }
            masterHistoricalChart.getData().notifyDataChanged();
            masterHistoricalChart.notifyDataSetChanged();
            masterHistoricalChart.invalidate();
        });
        animator.start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EDIT_REQUEST && resultCode == RESULT_OK && data != null) {
            int pos = data.getIntExtra("position", -1);
            if (pos < 0 || pos >= logs.size()) return;

            SensorLog log = logs.get(pos);
            log.filterId    = data.getIntExtra("filterId", log.filterId);
            log.filterName  = data.getStringExtra("filterName");
            log.ph          = data.getDoubleExtra("ph", log.ph);
            log.turbidity   = data.getDoubleExtra("turbidity", log.turbidity);
            log.temperature = data.getDoubleExtra("temperature", log.temperature);
            log.usage       = data.getDoubleExtra("usage", log.usage);
            log.latitude    = data.getDoubleExtra("lat", log.latitude);
            log.longitude   = data.getDoubleExtra("lng", log.longitude);
            log.gpsCoords   = data.getStringExtra("gpsCoords");

            adapter.notifyItemChanged(pos);
            loadChart("ph", Color.parseColor("#E65100"), Color.parseColor("#FFCC80"));
        }
    }
}