package com.mobile.madfya;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.mobile.madfya.data.User;

import java.util.ArrayList;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.VH> {

    public interface Listener {
        void onEdit(User user);

        void onDelete(User user);
    }

    private final List<User> items = new ArrayList<>();
    private final Listener listener;

    public UserAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submit(List<User> list) {
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
                .inflate(R.layout.item_user, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        User u = items.get(position);

        h.avatar.setText(Ui.initials(u.name));
        int avatarColor = ContextCompat.getColor(h.itemView.getContext(), Ui.avatarColorRes(u.active, u.id));
        h.avatar.setBackgroundTintList(ColorStateList.valueOf(avatarColor));

        h.name.setText(u.name);
        h.role.setText(u.role);

        int statusColor = ContextCompat.getColor(h.itemView.getContext(),
                u.active ? R.color.status_active : R.color.status_inactive);
        h.statusDot.setBackgroundTintList(ColorStateList.valueOf(statusColor));
        h.statusText.setText(u.active ? R.string.status_active : R.string.status_inactive);

        h.edit.setOnClickListener(v -> listener.onEdit(u));
        h.delete.setOnClickListener(v -> listener.onDelete(u));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView avatar;
        final TextView name;
        final TextView role;
        final TextView statusText;
        final View statusDot;
        final ImageButton edit;
        final ImageButton delete;

        VH(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.avatar);
            name = itemView.findViewById(R.id.name);
            role = itemView.findViewById(R.id.role);
            statusText = itemView.findViewById(R.id.status_text);
            statusDot = itemView.findViewById(R.id.status_dot);
            edit = itemView.findViewById(R.id.btn_edit);
            delete = itemView.findViewById(R.id.btn_delete);
        }
    }
}
