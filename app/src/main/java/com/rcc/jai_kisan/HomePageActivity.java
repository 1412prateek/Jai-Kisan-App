package com.rcc.jai_kisan;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class HomePageActivity extends AppCompatActivity {

    CardView cardWeather; // Existing Weather card
    CardView cardMandi;   // Existing Mandi Prices card
    CardView cardSchemes; // ✅ Government Schemes card

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        // Initialize existing Weather card
        cardWeather = findViewById(R.id.cardWeather);
        cardWeather.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePageActivity.this, WeatherActivity.class);
                startActivity(intent);
            }
        });

        // ✅ Initialize Mandi Prices card
        cardMandi = findViewById(R.id.cardMandi);
        cardMandi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePageActivity.this, MandiPricesActivity.class);
                startActivity(intent);
            }
        });

        // ✅ Initialize Government Schemes card
        cardSchemes = findViewById(R.id.cardSchemes);
        cardSchemes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePageActivity.this, GovernmentSchemesActivity.class);
                startActivity(intent);
            }
        });

        // ✅ Initialize Detect Disease card
        CardView cardDetectDisease = findViewById(R.id.cardDetectDisease);
        cardDetectDisease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePageActivity.this, DetectDiseaseActivity.class);
                startActivity(intent);
            }
        });
    }
}
