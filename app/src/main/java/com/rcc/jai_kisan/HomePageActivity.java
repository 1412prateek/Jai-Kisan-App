package com.rcc.jai_kisan;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.card.MaterialCardView;

public class HomePageActivity extends AppCompatActivity {

    CardView cardWeather; // Existing Weather card
    CardView cardMandi;   // Existing Mandi Prices card
    CardView cardFarmingTools; // ✅ Farming Tools hub
    CardView cardHowToUse;
    CardView cardVoiceAssistant; // ✅ IMPROVEMENT: Added the Voice Assistant card

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

        // Initialize Mandi Prices card
        cardMandi = findViewById(R.id.cardMandi);
        cardMandi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePageActivity.this, MandiPricesActivity.class);
                startActivity(intent);
            }
        });

        // ✅ Farming Tools card — launches FarmingToolsActivity hub
        cardFarmingTools = findViewById(R.id.cardFarmingTools);
        cardFarmingTools.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomePageActivity.this, FarmingToolsActivity.class));
            }
        });

        // Initialize Detect Disease card
        CardView cardDetectDisease = findViewById(R.id.cardDetectDisease);
        cardDetectDisease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePageActivity.this, DetectDiseaseActivity.class);
                startActivity(intent);
            }
        });

        // Initialize the How to Use card
        cardHowToUse = findViewById(R.id.cardHowToUse);
        cardHowToUse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePageActivity.this, HowToUseActivity.class);
                startActivity(intent);
            }
        });

        // ✅ IMPROVEMENT: Initialize the Voice Assistant card and set its click listener
        cardVoiceAssistant = findViewById(R.id.cardVoiceAssistant);
        cardVoiceAssistant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePageActivity.this, VoiceAssistantActivity.class);
                startActivity(intent);
            }
        });

        // About
        MaterialCardView cardAbout = findViewById(R.id.cardAboutDeveloper);
        cardAbout.setOnClickListener(v -> {
            Intent intent = new Intent(HomePageActivity.this, AboutDeveloperActivity.class);
            startActivity(intent);
        });




    }
}