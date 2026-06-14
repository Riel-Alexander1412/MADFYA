package com.mobile.madfya;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class WaterTipPagerAdapter extends RecyclerView.Adapter<WaterTipPagerAdapter.ViewHolder> {

    private List<WaterTipModel> tipList;
    private OnTipClickListener listener;

    public interface OnTipClickListener{
        void onLearnMoreClick(WaterTipModel tip);
    }

    public WaterTipPagerAdapter(List<WaterTipModel> tipList, OnTipClickListener listener){
        this.tipList = tipList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_water_tip, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position){
        WaterTipModel tip = tipList.get(position);
        holder.titleTextView.setText(tip.getTitle());

        // FIXED: Dynamically load the image from your model object instead of the default placeholder
        holder.imageView.setImageResource(tip.getImageResource());

        holder.learnMoreButton.setOnClickListener(v -> {
            listener.onLearnMoreClick(tip);
        });
    }

    @Override
    public int getItemCount(){
        return tipList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        ImageView imageView;
        TextView titleTextView;
        Button learnMoreButton;

        public ViewHolder(@NonNull View itemView){
            super(itemView);
            imageView = itemView.findViewById(R.id.tipImageView);
            titleTextView = itemView.findViewById(R.id.tipTitleTextView);
            learnMoreButton = itemView.findViewById(R.id.learnMoreButton);
        }
    }
}