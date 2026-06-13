package com.mobile.madfya;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mobile.madfya.data.Comment;

import java.util.ArrayList;
import java.util.List;

/** Renders the comment thread shown in the comments dialog. */
public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.VH> {

    private final List<Comment> items = new ArrayList<>();

    public void submit(List<Comment> list) {
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
                .inflate(R.layout.item_comment, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Comment c = items.get(position);
        h.author.setText(c.author);
        h.time.setText(Ui.timeAgo(c.timestamp));
        h.text.setText(c.text);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView author;
        final TextView time;
        final TextView text;

        VH(@NonNull View v) {
            super(v);
            author = v.findViewById(R.id.author);
            time = v.findViewById(R.id.time);
            text = v.findViewById(R.id.text);
        }
    }
}
