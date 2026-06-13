package com.mobile.madfya;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mobile.madfya.data.CommunityNotice;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Renders community notices: a red pinned/urgent card and the standard notice card. */
public class NoticeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_PINNED = 0;
    private static final int TYPE_NOTICE = 1;

    public interface Listener {
        void onLike(CommunityNotice notice);

        void onComment(CommunityNotice notice);
    }

    private final List<CommunityNotice> items = new ArrayList<>();
    private final Listener listener;

    public NoticeAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submit(List<CommunityNotice> list) {
        items.clear();
        if (list != null) {
            items.addAll(list);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).pinned ? TYPE_PINNED : TYPE_NOTICE;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inf = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_PINNED) {
            return new PinnedVH(inf.inflate(R.layout.item_notice_alert, parent, false));
        }
        return new NoticeVH(inf.inflate(R.layout.item_notice, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        CommunityNotice n = items.get(position);
        if (holder instanceof PinnedVH) {
            PinnedVH h = (PinnedVH) holder;
            h.title.setText(n.title);
            h.body.setText(n.body);
        } else {
            NoticeVH h = (NoticeVH) holder;
            h.title.setText(n.title);
            h.tag.setText(n.tag);
            h.meta.setText(n.authorName + " • " + Ui.timeAgo(n.timestamp));
            if (n.locationName == null || n.locationName.trim().isEmpty()) {
                h.locationRow.setVisibility(View.GONE);
            } else {
                h.locationRow.setVisibility(View.VISIBLE);
                h.location.setText(String.format(Locale.getDefault(),
                        "%s (%.1f km away)", n.locationName, n.distanceKm));
            }
            h.body.setText(n.body);
            h.likes.setText(String.valueOf(n.likes));
            h.comments.setText(String.valueOf(n.commentsCount));
            h.likeGroup.setOnClickListener(v -> listener.onLike(n));
            h.commentGroup.setOnClickListener(v -> listener.onComment(n));
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class NoticeVH extends RecyclerView.ViewHolder {
        final TextView title;
        final TextView tag;
        final TextView meta;
        final View locationRow;
        final TextView location;
        final TextView body;
        final TextView likes;
        final TextView comments;
        final View likeGroup;
        final View commentGroup;

        NoticeVH(@NonNull View v) {
            super(v);
            title = v.findViewById(R.id.title);
            tag = v.findViewById(R.id.tag);
            meta = v.findViewById(R.id.meta);
            locationRow = v.findViewById(R.id.location_row);
            location = v.findViewById(R.id.location);
            body = v.findViewById(R.id.body);
            likes = v.findViewById(R.id.likes);
            comments = v.findViewById(R.id.comments);
            likeGroup = v.findViewById(R.id.like_group);
            commentGroup = v.findViewById(R.id.comment_group);
        }
    }

    static class PinnedVH extends RecyclerView.ViewHolder {
        final TextView title;
        final TextView body;

        PinnedVH(@NonNull View v) {
            super(v);
            title = v.findViewById(R.id.title);
            body = v.findViewById(R.id.body);
        }
    }
}
