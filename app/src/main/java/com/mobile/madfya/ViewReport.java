package com.mobile.madfya;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.mobile.madfya.data.FirebaseRepository;
import com.mobile.madfya.data.Reports;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ViewReport extends AppCompatActivity {

    private RecyclerView rvReports;
    private TextView tvEmpty;
    private ReportAdapter adapter;
    private FirebaseRepository firebaseRepo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_report);

        firebaseRepo = FirebaseRepository.get();

        Toolbar toolbar = findViewById(R.id.toolbarViewReport);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }

        rvReports = findViewById(R.id.rvReports);
        tvEmpty = findViewById(R.id.tvEmpty);

        MaterialButton btnCreate = findViewById(R.id.btnCreateReport);
        btnCreate.setOnClickListener(v ->
                startActivity(new Intent(this, CreateReport.class)));

        adapter = new ReportAdapter();
        rvReports.setLayoutManager(new LinearLayoutManager(this));
        rvReports.setAdapter(adapter);

        loadReports();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadReports();
    }

    private void loadReports() {
        // Use LiveData from FirebaseRepository
        firebaseRepo.getAllReports().observe(this, reports -> {
            if (reports == null || reports.isEmpty()) {
                tvEmpty.setVisibility(View.VISIBLE);
                rvReports.setVisibility(View.GONE);
            } else {
                tvEmpty.setVisibility(View.GONE);
                rvReports.setVisibility(View.VISIBLE);
                adapter.setReports(reports);
            }
        });
    }

    // ── Adapter ──────────────────────────────────────────────────────────────

    private class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.VH> {

        private List<Reports> data = new ArrayList<>();

        void setReports(List<Reports> list) {
            data = list;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_report, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int position) {
            Reports r = data.get(position);

            h.tvTitle.setText(r.title != null ? r.title : "Untitled");
            h.tvCategory.setText(r.category != null ? r.category : "—");
            h.tvPoster.setText("Reported by user #" + r.ReportedBy);

            if (r.ReportedTimeStamps > 0) {
                h.tvTimestamp.setText(new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                        .format(new Date(r.ReportedTimeStamps)));
            } else {
                h.tvTimestamp.setText("—");
            }

            // Load image from Firebase Storage URL
            if (r.ImagePath != null && !r.ImagePath.isEmpty()) {
                h.ivThumbnail.setVisibility(View.VISIBLE);
                Glide.with(h.ivThumbnail.getContext())
                        .load(r.ImagePath)  // Firebase Storage URL
                        .centerCrop()
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.ic_menu_report_image)
                        .into(h.ivThumbnail);
            } else {
                h.ivThumbnail.setVisibility(View.GONE);
            }

            h.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(ViewReport.this, ReportDetail.class);
                intent.putExtra("report_id", r.firebaseKey); // Use firebaseKey as ID
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        class VH extends RecyclerView.ViewHolder {
            TextView  tvTitle, tvCategory, tvPoster, tvTimestamp;
            ImageView ivThumbnail;

            VH(View v) {
                super(v);
                tvTitle     = v.findViewById(R.id.tvReportTitle);
                tvCategory  = v.findViewById(R.id.tvReportCategory);
                tvPoster    = v.findViewById(R.id.tvReportPoster);
                tvTimestamp = v.findViewById(R.id.tvReportTimestamp);
                ivThumbnail = v.findViewById(R.id.ivReportThumbnail);
            }
        }
    }
}