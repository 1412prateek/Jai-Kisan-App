package com.rcc.jai_kisan;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;

public class FarmingToolsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_farming_tools);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        MaterialCardView cardFertilizer = findViewById(R.id.card_fertilizer);
        MaterialCardView cardYield = findViewById(R.id.card_yield);
        MaterialCardView cardIrrigation = findViewById(R.id.card_irrigation);
        MaterialCardView cardCost = findViewById(R.id.card_cost);

        cardFertilizer.setOnClickListener(v -> startActivity(new Intent(this, FertilizerCalculatorActivity.class)));
        cardYield.setOnClickListener(v -> startActivity(new Intent(this, YieldEstimatorActivity.class)));

        // UNLINKED from ComingSoonActivity and linked to new specific activities
        cardIrrigation.setOnClickListener(v -> startActivity(new Intent(this, IrrigationPlannerActivity.class)));
        cardCost.setOnClickListener(v -> startActivity(new Intent(this, CostCalculatorActivity.class)));
    }
}