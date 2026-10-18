package com.rcc.jai_kisan;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.cardview.widget.CardView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textview.MaterialTextView;

public class HomePageActivity extends BaseActivity {

    CardView cardWeather;
    CardView cardMandi;
    CardView cardFarmingTools;
    CardView cardHowToUse;
    CardView cardVoiceAssistant;
    View btnHomeSettings;
    MaterialTextView homeTitle;

    TextView tvCardDetectDisease;
    TextView tvCardVoiceAssistant;
    TextView tvCardMandi;
    TextView tvCardHowToUse;
    TextView tvCardWeather;
    TextView tvCardFarmingTools;
    TextView tvCardAbout;

    private InAppUpdateHelper inAppUpdateHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        inAppUpdateHelper = new InAppUpdateHelper(this);
        inAppUpdateHelper.checkForAppUpdate();

        homeTitle = findViewById(R.id.homeTitle);
        btnHomeSettings = findViewById(R.id.btnHomeSettings);
        if (btnHomeSettings != null) {
            btnHomeSettings.setOnClickListener(v -> {
                startActivity(new Intent(HomePageActivity.this, SettingsActivity.class));
            });
        }

        tvCardDetectDisease = findViewById(R.id.tvCardDetectDisease);
        tvCardVoiceAssistant = findViewById(R.id.tvCardVoiceAssistant);
        tvCardMandi = findViewById(R.id.tvCardMandi);
        tvCardHowToUse = findViewById(R.id.tvCardHowToUse);
        tvCardWeather = findViewById(R.id.tvCardWeather);
        tvCardFarmingTools = findViewById(R.id.tvCardFarmingTools);
        tvCardAbout = findViewById(R.id.tvCardAbout);

        // Initialize existing Weather card
        cardWeather = findViewById(R.id.cardWeather);
        cardWeather.setOnClickListener(v -> startActivity(new Intent(HomePageActivity.this, WeatherActivity.class)));

        // Initialize Mandi Prices card
        cardMandi = findViewById(R.id.cardMandi);
        cardMandi.setOnClickListener(v -> startActivity(new Intent(HomePageActivity.this, MandiPricesActivity.class)));

        // Farming Tools card — launches FarmingToolsActivity hub
        cardFarmingTools = findViewById(R.id.cardFarmingTools);
        cardFarmingTools.setOnClickListener(v -> startActivity(new Intent(HomePageActivity.this, FarmingToolsActivity.class)));

        // Initialize Detect Disease card
        CardView cardDetectDisease = findViewById(R.id.cardDetectDisease);
        cardDetectDisease.setOnClickListener(v -> startActivity(new Intent(HomePageActivity.this, DetectDiseaseActivity.class)));

        // Initialize the How to Use card
        cardHowToUse = findViewById(R.id.cardHowToUse);
        cardHowToUse.setOnClickListener(v -> startActivity(new Intent(HomePageActivity.this, HowToUseActivity.class)));

        // Initialize the Voice Assistant card
        cardVoiceAssistant = findViewById(R.id.cardVoiceAssistant);
        cardVoiceAssistant.setOnClickListener(v -> startActivity(new Intent(HomePageActivity.this, VoiceAssistantActivity.class)));

        // About
        MaterialCardView cardAbout = findViewById(R.id.cardAboutDeveloper);
        cardAbout.setOnClickListener(v -> startActivity(new Intent(HomePageActivity.this, AboutDeveloperActivity.class)));

        updateLocalizedTexts();
    }

    private String formatCardText(String english, String key) {
        if (LanguageManager.isEnglishOnly(this)) {
            return english;
        }
        String regional = LanguageManager.getRegional(this, key, "");
        if (regional == null || regional.trim().isEmpty() || regional.equalsIgnoreCase(english)) {
            return english;
        }
        return english + "\n(" + regional + ")";
    }

    private void updateLocalizedTexts() {
        if (homeTitle != null) {
            homeTitle.setText(LanguageManager.getRegional(this, "app_motto", "मेरी मिट्टी, मेरे किसान"));
        }
        if (tvCardDetectDisease != null) tvCardDetectDisease.setText(formatCardText("Detect Disease", "home_detect_disease"));
        if (tvCardVoiceAssistant != null) tvCardVoiceAssistant.setText(formatCardText("Voice Assistant", "home_voice_assistant"));
        if (tvCardMandi != null) tvCardMandi.setText(formatCardText("Mandi Prices", "home_mandi_prices"));
        if (tvCardHowToUse != null) tvCardHowToUse.setText(formatCardText("How to Use", "home_how_to_use"));
        if (tvCardWeather != null) tvCardWeather.setText(formatCardText("Weather Info", "home_weather_info"));
        if (tvCardFarmingTools != null) tvCardFarmingTools.setText(formatCardText("Farming Tools", "home_farming_tools"));
        if (tvCardAbout != null) tvCardAbout.setText(formatCardText("About", "home_about"));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (inAppUpdateHelper != null) {
            inAppUpdateHelper.onResume();
        }
        updateLocalizedTexts();
    }
}