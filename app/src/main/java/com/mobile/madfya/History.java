package com.mobile.madfya;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.material.chip.ChipGroup;
import com.mobile.madfya.data.AppDatabase;

import java.util.ArrayList;
import java.util.List;

public class History extends AppCompatActivity {

    private RecyclerView rvHistoricalLedger;
    private SensorLogAdapter adapter;
    private List<SensorLog> logs;
    private LineChart masterHistoricalChart;
    private ChipGroup chipGroupMetrics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        // Toolbar
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbarHistory);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Views
        rvHistoricalLedger = findViewById(R.id.rvHistoricalLedger);
        masterHistoricalChart = findViewById(R.id.masterHistoricalChart);
        chipGroupMetrics = findViewById(R.id.chipGroupMetrics);
        logs = new ArrayList<>();
        adapter = new SensorLogAdapter(logs);
        rvHistoricalLedger.setLayoutManager(new LinearLayoutManager(this));
        rvHistoricalLedger.setAdapter(adapter);
        AppDatabase.get(this).sensorsDao().getAll().observe(this, sensors -> {
            logs.clear();
            for (com.mobile.madfya.data.Sensors s : sensors) {
                String name = SensorUtil.getSensorName(s.filterId);
                String coords = s.latitude + ", " + s.longitude;
                String date = android.text.format.DateFormat.format("dd/MM/yyyy", s.CurrentTimeStamp).toString();
                String time = android.text.format.DateFormat.format("hh:mm a", s.CurrentTimeStamp).toString();
                double ph = s.ph_level != null ? Double.parseDouble(s.ph_level) : 0;
                double turbidity = s.turbidity != null ? Double.parseDouble(s.turbidity) : 0;
                double temperature = s.temperature != null ? Double.parseDouble(s.temperature) : 0;
                double usage = s.water_flow_rate != null ? Double.parseDouble(s.water_flow_rate) : 0;
                logs.add(new SensorLog(name, coords, date, time, ph, turbidity, temperature, usage, s.latitude, s.longitude));
            }
            adapter.notifyDataSetChanged();
            loadChart("ph");
        });


        // Chip listener
        chipGroupMetrics.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            if (id == R.id.chipPh) loadChart("ph");
            else if (id == R.id.chipTurbidity) loadChart("turbidity");
            else if (id == R.id.chipTemperature) loadChart("temperature");
            else if (id == R.id.chipUsage) loadChart("usage");
        });
    }

    private void loadChart(String metric) {
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < logs.size(); i++) {
            SensorLog log = logs.get(i);
            float val;
            switch (metric) {
                case "turbidity":   val = (float) log.turbidity;   break;
                case "temperature": val = (float) log.temperature; break;
                case "usage":       val = (float) log.usage;       break;
                default:            val = (float) log.ph;          break;
            }
            entries.add(new Entry(i, val));
        }

        LineDataSet dataSet = new LineDataSet(entries, metric.toUpperCase());
        dataSet.setColor(0xFF005A9E);
        dataSet.setFillColor(0xFFB3D1F0);
        dataSet.setFillAlpha(80);
        dataSet.setDrawFilled(true);
        dataSet.setDrawCircles(false);
        dataSet.setLineWidth(2f);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setDrawValues(false);

        masterHistoricalChart.setData(new LineData(dataSet));
        masterHistoricalChart.getDescription().setEnabled(false);
        masterHistoricalChart.getLegend().setEnabled(false);
        masterHistoricalChart.getXAxis().setDrawGridLines(false);
        masterHistoricalChart.getAxisRight().setEnabled(false);
        masterHistoricalChart.animateX(500);
        masterHistoricalChart.invalidate();
    }
}