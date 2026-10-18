package com.rcc.jai_kisan;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;

public class FarmingToolsActivity extends BaseActivity {

    private MaterialToolbar toolbar;
    private TextView tvSubFertilizer;
    private TextView tvSubYield;
    private TextView tvSubIrrigation;
    private TextView tvSubCost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_farming_tools);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        tvSubFertilizer = findViewById(R.id.tv_sub_fertilizer);
        tvSubYield = findViewById(R.id.tv_sub_yield);
        tvSubIrrigation = findViewById(R.id.tv_sub_irrigation);
        tvSubCost = findViewById(R.id.tv_sub_cost);

        MaterialCardView cardFertilizer = findViewById(R.id.card_fertilizer);
        MaterialCardView cardYield = findViewById(R.id.card_yield);
        MaterialCardView cardIrrigation = findViewById(R.id.card_irrigation);
        MaterialCardView cardCost = findViewById(R.id.card_cost);

        cardFertilizer.setOnClickListener(v -> startActivity(new Intent(this, FertilizerCalculatorActivity.class)));
        cardYield.setOnClickListener(v -> startActivity(new Intent(this, YieldEstimatorActivity.class)));
        cardIrrigation.setOnClickListener(v -> startActivity(new Intent(this, IrrigationPlannerActivity.class)));
        cardCost.setOnClickListener(v -> startActivity(new Intent(this, CostCalculatorActivity.class)));

        AdManager.getInstance().loadAdaptiveBanner(this, findViewById(R.id.adBannerContainer));

        updateLocalizedTexts();
    }

    private void updateLocalizedTexts() {
        if (toolbar != null) {
            toolbar.setTitle(LanguageManager.getDual(this, "Farming Tools", "home_farming_tools"));
        }

        boolean isEn = LanguageManager.isEnglishOnly(this);
        if (isEn) {
            if (tvSubFertilizer != null) tvSubFertilizer.setVisibility(View.GONE);
            if (tvSubYield != null) tvSubYield.setVisibility(View.GONE);
            if (tvSubIrrigation != null) tvSubIrrigation.setVisibility(View.GONE);
            if (tvSubCost != null) tvSubCost.setVisibility(View.GONE);
        } else {
            if (tvSubFertilizer != null) {
                tvSubFertilizer.setVisibility(View.VISIBLE);
                tvSubFertilizer.setText(LanguageManager.getRegional(this, "fertilizer_calculator", "उर्वरक कैलकुलेटर"));
            }
            if (tvSubYield != null) {
                tvSubYield.setVisibility(View.VISIBLE);
                tvSubYield.setText(LanguageManager.getRegional(this, "yield_estimator", "पैदावार अनुमान"));
            }
            if (tvSubIrrigation != null) {
                tvSubIrrigation.setVisibility(View.VISIBLE);
                tvSubIrrigation.setText(LanguageManager.getRegional(this, "irrigation_planner", "सिंचाई योजना"));
            }
            if (tvSubCost != null) {
                tvSubCost.setVisibility(View.VISIBLE);
                tvSubCost.setText(LanguageManager.getRegional(this, "cost_calculator", "लागत कैलकुलेटर"));
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateLocalizedTexts();
    }
}