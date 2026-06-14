package com.mobile.madfya;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import java.util.ArrayList;
import java.util.List;

public class Safety extends AppCompatActivity implements WaterTipPagerAdapter.OnTipClickListener {

    private ViewPager2 tipsViewPager;
    private TabLayout tabLayout;
    private WaterTipPagerAdapter adapter;
    private List<WaterTipModel> tipList;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_safety);

        // Click listener for the draggable back arrow on the Safety page
        ImageView backArrow = findViewById(R.id.backArrowIcon);
        if (backArrow != null) {
            backArrow.setOnClickListener(v -> finish());
        }

        tipsViewPager = findViewById(R.id.tipsViewPager);
        tabLayout = findViewById(R.id.tabLayout);

        loadSampleTips();

        adapter = new WaterTipPagerAdapter(tipList, this);
        tipsViewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, tipsViewPager, (tab, position) -> {}).attach();
    }

    private void loadSampleTips(){
        tipList = new ArrayList<>();

        // Pass your drawable resource ID (e.g., R.drawable.your_image) into the third parameter
        tipList.add(new WaterTipModel(
                "How to Test Water Quality at Home",
                "An introductory guide on testing your tap water for safety, clarity, and impurities using quick household methods.",
                R.drawable.checkwater,
                "https://www.youtube.com/watch?v=QO9i3dlXaOw"
        ));

        tipList.add(new WaterTipModel(
                "How and When to Change Your Water Filter",
                "A quick overview of household filter maintenance, optimal replacement schedules, and signs that your filter needs changing",
                R.drawable.waterfilter,
                "https://www.youtube.com/watch?v=UQKeMoBmL34&t=41s"
        ));
    }

    @Override
    public void onLearnMoreClick(WaterTipModel tip){
        Intent intent = new Intent(Safety.this, WaterTipDetailActivity.class);
        intent.putExtra("tip_title", tip.getTitle());
        intent.putExtra("tip_description", tip.getDescription());
        intent.putExtra("tip_image", tip.getImageResource()); // Added to pass the image resource ID
        intent.putExtra("tip_youtube_url", tip.getYoutubeUrl());
        startActivity(intent);
    }
}