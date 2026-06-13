package com.mobile.madfya;

import android.content.res.ColorStateList;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.mobile.madfya.data.Alert;

import java.util.ArrayList;
import java.util.List;

/** Renders the alert cards with a coloured accent bar and type icon. */
public class AlertAdapter extends RecyclerView.Adapter<AlertAdapter.VH> {

    private final List<Alert> items = new ArrayList<>();

    public void submit(List<Alert> list) {
        items.clear();
        if (list != null) {
            items.addAll(list);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_alert, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Alert a = items.get(position);
        int color = ContextCompat.getColor(h.itemView.getContext(), Ui.alertColorRes(a.type));

        h.accent.setBackgroundColor(color);
        h.icon.setImageResource(Ui.alertIcon(a.type));
        h.icon.setImageTintList(ColorStateList.valueOf(color));
        h.title.setText(a.title);
        h.time.setText(Ui.timeAgo(a.timestamp));

        if (TextUtils.isEmpty(a.message)) {
            h.message.setVisibility(View.GONE);
        } else {
            h.message.setVisibility(View.VISIBLE);
            h.message.setText(a.message);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final View accent;
        final ImageView icon;
        final TextView title;
        final TextView time;
        final TextView message;

        VH(@NonNull View itemView) {
            super(itemView);
            accent = itemView.findViewById(R.id.accent);
            icon = itemView.findViewById(R.id.icon);
            title = itemView.findViewById(R.id.title);
            time = itemView.findViewById(R.id.time);
            message = itemView.findViewById(R.id.message);
        }
    }
}
