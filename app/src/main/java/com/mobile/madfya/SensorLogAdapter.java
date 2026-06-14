package com.mobile.madfya;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.List;

public class SensorLogAdapter extends RecyclerView.Adapter<SensorLogAdapter.ViewHolder> {

    private final List<SensorLog> logs;

    public SensorLogAdapter(List<SensorLog> logs) {
        this.logs = logs;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SensorLog log = logs.get(position);

        holder.tvItemTurbidity.setText(String.valueOf(log.turbidity));
        holder.tvItemTemp.setText(log.temperature + "°C");
        holder.tvItemUsage.setText(log.usage + "L");

        holder.btnEditLog.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), EditLog.class);
            intent.putExtra("position",    position);
            intent.putExtra("lat",         log.latitude);
            intent.putExtra("lng",         log.longitude);
            intent.putExtra("filterName",  log.filterName);
            intent.putExtra("ph",          String.valueOf(log.ph));
            intent.putExtra("turbidity",   String.valueOf(log.turbidity));
            intent.putExtra("temperature", String.valueOf(log.temperature));
            intent.putExtra("usage",       String.valueOf(log.usage));
            v.getContext().startActivity(intent);
        });

        holder.btnDeleteLog.setOnClickListener(v -> {
            int currentPos = holder.getAdapterPosition();
            if (currentPos != RecyclerView.NO_POSITION) {
                logs.remove(currentPos);
                notifyItemRemoved(currentPos);
                notifyItemRangeChanged(currentPos, logs.size());
            }
        });

        String staticMapUrl = "https://maps.geoapify.com/v1/staticmap"
                + "?style=osm-bright&width=500&height=500"
                + "&center=lonlat%3A" + log.longitude + "%2C" + log.latitude
                + "&zoom=15"
                + "&apiKey=59d2db334aab4632929ede3ace4b0df8";
        android.util.Log.d("MAP_URL", staticMapUrl);

        // Clean Glide v5 call structure
        Glide.with(holder.itemView.getContext().getApplicationContext())
                .load(staticMapUrl)
                .placeholder(R.drawable.ic_map)
                .timeout(10000)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        Log.e("GLIDE", "Load failed: " + (e != null ? e.getMessage() : "Unknown exception"));
                        return false;
                    }
                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        return false;
                    }
                })
                .into(holder.mapThumbnail);
        holder.mapThumbnail.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), GpsFullscreen.class);
            intent.putExtra("lat", log.latitude);
            intent.putExtra("lng", log.longitude);
            intent.putExtra("filter", log.filterName);
            intent.putExtra("date", log.date);
            intent.putExtra("time", log.time);
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return logs.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView mapThumbnail;
        TextView tvItemPh, tvItemTurbidity, tvItemTemp, tvItemUsage;
        ImageButton btnEditLog, btnDeleteLog;

        ViewHolder(View itemView) {
            super(itemView);
            mapThumbnail = itemView.findViewById(R.id.mapThumbnail);
            tvItemPh = itemView.findViewById(R.id.tvItemPh);
            tvItemTurbidity = itemView.findViewById(R.id.tvItemTurbidity);
            tvItemTemp = itemView.findViewById(R.id.tvItemTemp);
            tvItemUsage = itemView.findViewById(R.id.tvItemUsage);
            btnEditLog = itemView.findViewById(R.id.btnEditLog);
            btnDeleteLog = itemView.findViewById(R.id.btnDeleteLog);
        }
    }
}