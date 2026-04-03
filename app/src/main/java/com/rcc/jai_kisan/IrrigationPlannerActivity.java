package com.rcc.jai_kisan;

import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.snackbar.Snackbar;

import java.util.Locale;

import android.util.Log;
import androidx.annotation.NonNull;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import io.noties.markwon.Markwon;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class IrrigationPlannerActivity extends AppCompatActivity {

    private View rootView;
    private AutoCompleteTextView cropDropdown, soilDropdown, methodDropdown;
    private View resultSection;
    private TextView tvInterval, tvCriticalStage, tvExpertTip;
    private CircularProgressIndicator loadingIndicator;
    private TextView tvAiInsight;
    private Markwon markwon;

    private static final String GEMINI_API_KEY = BuildConfig.GEMINI_API_KEY;
    private static final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemma-3-4b-it:generateContent?key=" + GEMINI_API_KEY;

    private static final String[] CROPS = {
            "Wheat / गेहूं", "Rice / धान", "Maize / मक्का", "Sugarcane / गन्ना",
            "Cotton / कपास", "Soybean / सोयाबीन", "Mustard / सरसों", "Potato / आलू",
            "Millets / बाजरा", "Pulses / दालें"
    };

    private static final String[] SOILS = {
            "Alluvial / जलोढ़", "Black / काली मिट्टी", "Red / लाल मिट्टी",
            "Laterite / लैटेराइट", "Sandy / रेतीली मिट्टी", "Clay / चिकनी मिट्टी"
    };

    private static final String[] METHODS = {
            "Flood / सतह सिंचाई", "Drip / ड्रिप (टपक)", "Sprinkler / फव्वारा",
            "Furrow / कूंड़ सिंचाई", "Basin / थाला सिंचाई"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_irrigation_planner);

        markwon = Markwon.create(this);

        rootView = findViewById(R.id.root_view);
        cropDropdown = findViewById(R.id.dropdown_crop);
        soilDropdown = findViewById(R.id.dropdown_soil);
        methodDropdown = findViewById(R.id.dropdown_method);
        resultSection = findViewById(R.id.result_section);

        tvInterval = findViewById(R.id.tv_interval);
        tvCriticalStage = findViewById(R.id.tv_critical_stage);
        tvExpertTip = findViewById(R.id.tv_expert_tip);
        
        loadingIndicator = findViewById(R.id.loadingIndicator);
        tvAiInsight = findViewById(R.id.tv_ai_insight);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        ImageButton btnClose = findViewById(R.id.btn_close);
        if (btnClose != null) btnClose.setOnClickListener(v -> finish());

        // Adapters
        cropDropdown.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, CROPS));
        soilDropdown.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, SOILS));
        methodDropdown.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, METHODS));

        // Autofill logic from Photo Expert
        String autofillCrop = getIntent().getStringExtra("autofill_crop");
        if (autofillCrop != null && !autofillCrop.isEmpty()) {
            cropDropdown.setText(autofillCrop, false);
            Snackbar.make(rootView, "Crop Auto-Selected: " + autofillCrop, Snackbar.LENGTH_SHORT).show();
        }

        findViewById(R.id.btn_calculate_irrigation).setOnClickListener(v -> calculateIrrigation());

        findViewById(R.id.btn_share).setOnClickListener(v -> shareSchedule());
    }

    private void calculateIrrigation() {
        String crop = cropDropdown.getText().toString();
        String soil = soilDropdown.getText().toString();
        String method = methodDropdown.getText().toString();

        if (crop.isEmpty() || soil.isEmpty() || method.isEmpty()) {
            Snackbar.make(rootView, "Please fill all fields / कृपया पूरी जानकारी दें", Snackbar.LENGTH_SHORT).show();
            return;
        }

        // 1. Calculate Base Interval (Days) based on Soil Type
        int interval = 10;
        if (soil.contains("Sandy")) interval = 7;      // Sandy dries fast
        else if (soil.contains("Clay")) interval = 18;  // Clay holds water
        else if (soil.contains("Black")) interval = 15; // Black soil holds moisture well

        // 2. Adjust based on Method
        if (method.contains("Drip")) interval -= 3; // Drip needs very frequent small amounts
        else if (method.contains("Sprinkler")) interval -= 1;

        // 3. Define Bilingual Critical Growth Stages and Tips
        String critical;
        String tip;

        if (crop.contains("Wheat")) {
            critical = "CRI Stage (21 days) & Flowering.\nसीआरआई चरण (21 दिन) और फूल आने का समय।";
            tip = "Avoid water stress during the CRI stage to ensure root growth.\nजड़ों के विकास के लिए सीआरआई चरण के दौरान पानी की कमी न होने दें।";
        } else if (crop.contains("Rice")) {
            interval = 4; // Paddy needs frequent water
            critical = "Tillering to Grain Filling.\nकल्ले फूटने से दाना भरने तक।";
            tip = "Keep 5cm water level for flood method.\nसतह सिंचाई के लिए 5 सेमी पानी का स्तर बनाए रखें।";
        } else if (crop.contains("Sugarcane")) {
            interval += 2;
            critical = "Formative Stage (60-130 Days).\nप्रारंभिक विकास चरण (60-130 दिन)।";
            tip = "Deep irrigation is required for long intervals.\nलंबे अंतराल के लिए गहरी सिंचाई की आवश्यकता होती है।";
        } else if (crop.contains("Potato")) {
            interval = Math.max(5, interval - 2);
            critical = "Stolon formation & Tuberization.\nस्टोलन बनना और कंद बनना।";
            tip = "Light but frequent irrigation is best for tubers.\nकंदों के लिए हल्की लेकिन बार-बार सिंचाई सबसे अच्छी होती है।";
        } else {
            critical = "Active Vegetative Growth.\nसक्रिय वनस्पति विकास का चरण।";
            tip = "Monitor soil moisture at 15cm depth.\n15 सेमी की गहराई पर मिट्टी की नमी की निगरानी करें।";
        }

        displayResults(interval, critical, tip);
        fetchAIAdvise("Irrigation Advice", "Crop: " + crop + ", Soil: " + soil + ", Method: " + method + ". Give 2 extremely short bullet tips on efficient irrigation in English and Hindi separately. Keep it beneath 50 words.");
    }

    private void displayResults(int interval, String critical, String tip) {
        tvInterval.setText(String.format(Locale.getDefault(),
                "Irrigation Interval: Every %d days\nसिंचाई का अंतराल: हर %d दिनों में", interval, interval));

        tvCriticalStage.setText("Critical Stage / महत्वपूर्ण चरण:\n" + critical);

        tvExpertTip.setText("Expert Tip / विशेषज्ञ सलाह:\n" + tip);

        resultSection.setVisibility(View.VISIBLE);
        resultSection.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
    }

    private void fetchAIAdvise(String contextTitle, String promptText) {
        runOnUiThread(() -> {
            loadingIndicator.setVisibility(View.VISIBLE);
            tvAiInsight.setVisibility(View.GONE);
        });

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        String prompt = "You are an agricultural expert. Provide a lightning-fast practical tip.\n\n" +
                "Context: " + contextTitle + "\n" + promptText + "\n\n" +
                "Format: 1 short practical sentence in English, an empty line, then the exact translation in Hindi. Do NOT use bullet points, asterisks, or any labels like 'English' or 'Hindi'.";

        JsonObject textPart = new JsonObject();
        textPart.addProperty("text", prompt);
        JsonArray parts = new JsonArray();
        parts.add(textPart);
        JsonObject content = new JsonObject();
        content.add("parts", parts);
        JsonArray contents = new JsonArray();
        contents.add(content);
        JsonObject requestJson = new JsonObject();
        requestJson.add("contents", contents);

        RequestBody body = RequestBody.create(requestJson.toString(), MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder().url(GEMINI_URL).post(body).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> {
                    loadingIndicator.setVisibility(View.GONE);
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String responseBody = response.body() != null ? response.body().string() : "";
                runOnUiThread(() -> {
                    loadingIndicator.setVisibility(View.GONE);
                    try {
                        if (response.isSuccessful()) {
                            JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
                            String resultText = json.getAsJsonArray("candidates").get(0).getAsJsonObject()
                                    .getAsJsonObject("content").getAsJsonArray("parts").get(0).getAsJsonObject().get("text").getAsString().trim();
                            tvExpertTip.append("\n\n✨ " + resultText);
                        }
                    } catch (Exception e) {
                        // ignore error quietly
                    }
                });
            }
        });
    }

    private void shareSchedule() {
        String interval = tvInterval.getText().toString();
        String stage = tvCriticalStage.getText().toString();
        String tip = tvExpertTip.getText().toString();

        // Prevent sharing if results aren't calculated yet (default is "--")
        if (interval.contains("--")) {
            Snackbar.make(rootView, "Please plan first / पहले योजना बनाएं", Snackbar.LENGTH_SHORT).show();
            return;
        }

        String shareMessage = "💧 *Jai Kisan - Irrigation Schedule*\n\n" +
                "📅 " + interval + "\n\n" +
                "🌱 " + stage + "\n\n" +
                "💡 " + tip + "\n\n" +
                "Calculated via Jai Kisan App";

        android.content.Intent shareIntent = new android.content.Intent(android.content.Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareMessage);
        startActivity(android.content.Intent.createChooser(shareIntent, "Share via"));
    }
}