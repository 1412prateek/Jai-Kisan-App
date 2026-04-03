package com.rcc.jai_kisan;

import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

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

public class FertilizerCalculatorActivity extends AppCompatActivity {

    private View rootView;
    private AutoCompleteTextView cropDropdown, stateDropdown, unitDropdown;
    private TextInputEditText areaInput;
    private View resultSection;
    private TextView tvUreaBags, tvDapBags, tvMopBags;
    private TextView tvNRow, tvPRow, tvKRow, tvNanoUrea;
    private CircularProgressIndicator loadingIndicator;
    private TextView tvAiInsight;
    private Markwon markwon;

    private String suggestedFertsExtra = "";

    private static final String GEMINI_API_KEY = BuildConfig.GEMINI_API_KEY;
    private static final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemma-3-4b-it:generateContent?key=" + GEMINI_API_KEY;

    private final Map<String, Map<String, Double>> regionUnitMap = new HashMap<>();

    // Nutrient data per acre: {N, P, K}
    private static final double[][] NPK_BASE_RATES = {
            {55.0, 30.0, 15.0}, {50.0, 25.0, 25.0}, {60.0, 30.0, 20.0}, {50.0, 25.0, 25.0},
            {80.0, 35.0, 40.0}, {20.0, 40.0, 20.0}, {45.0, 20.0, 15.0}, {75.0, 40.0, 75.0},
            {40.0, 20.0, 20.0}, {35.0, 25.0, 25.0}
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fertilizer_calculator);

        markwon = Markwon.create(this);
        initializeRegionalUnits();

        rootView = findViewById(R.id.root_view);
        cropDropdown = findViewById(R.id.dropdown_crop);
        stateDropdown = findViewById(R.id.dropdown_state);
        unitDropdown = findViewById(R.id.dropdown_unit);
        areaInput = findViewById(R.id.input_area);
        resultSection = findViewById(R.id.result_section);

        tvUreaBags = findViewById(R.id.tv_urea_bags);
        tvDapBags = findViewById(R.id.tv_dap_bags);
        tvMopBags = findViewById(R.id.tv_mop_bags);

        tvNRow = findViewById(R.id.tv_n_row);
        tvPRow = findViewById(R.id.tv_p_row);
        tvKRow = findViewById(R.id.tv_k_row);
        tvNanoUrea = findViewById(R.id.tv_nano_urea);
        
        loadingIndicator = findViewById(R.id.loadingIndicator);
        tvAiInsight = findViewById(R.id.tv_ai_insight);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        ImageButton btnClose = findViewById(R.id.btn_close);
        if (btnClose != null) btnClose.setOnClickListener(v -> finish());

        // 1. Populate Crops
        String[] crops = {"Wheat / गेहूं", "Rice / धान", "Maize / मक्का", "Cotton / कपास", "Sugarcane / गन्ना", "Soybean / सोयाबीन", "Mustard / सरसों", "Potato / आलू", "Onion / प्याज", "Chili / मिर्च"};
        cropDropdown.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, crops));

        // 2. Populate States
        String[] allStates = {
                "Andhra Pradesh / आंध्र प्रदेश", "Arunachal Pradesh / अरुणाचल प्रदेश", "Assam / असम", "Bihar / बिहार", "Chhattisgarh / छत्तीसगढ़", "Goa / गोवा", "Gujarat / गुजरात", "Haryana / हरियाणा", "Himachal Pradesh / हिमाचल प्रदेश", "Jharkhand / झारखंड", "Karnataka / कर्नाटक", "Kerala / केरल", "Madhya Pradesh / मध्य प्रदेश", "Maharashtra / महाराष्ट्र", "Manipur / मणिपुर", "Meghalaya / मेघालय", "Mizoram / मिजोरम", "Nagaland / नागालैंड", "Odisha / ओडिशा", "Punjab / पंजाब", "Rajasthan / राजस्थान", "Sikkim / सिक्किम", "Tamil Nadu / तमिलनाडु", "Telangana / तेलंगाना", "Tripura / त्रिपुरा", "Uttar Pradesh / उत्तर प्रदेश", "Uttarakhand / उत्तराखंड", "West Bengal / पश्चिम बंगाल", "Delhi / दिल्ली", "Jammu & Kashmir / जम्मू और कश्मीर", "Ladakh / लद्दाख"
        };
        stateDropdown.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, allStates));

        stateDropdown.setOnItemClickListener((parent, view, position, id) -> loadUnitsForState(allStates[position]));

        // Autofill logic from Photo Expert
        String autofillCrop = getIntent().getStringExtra("autofill_crop");
        if (autofillCrop != null && !autofillCrop.isEmpty()) {
            cropDropdown.setText(autofillCrop, false);
            Snackbar.make(rootView, "Crop Auto-Selected: " + autofillCrop, Snackbar.LENGTH_SHORT).show();
        }

        if (getIntent().hasExtra("suggested_fertilizers")) {
            suggestedFertsExtra = getIntent().getStringExtra("suggested_fertilizers");
        }

        findViewById(R.id.btn_calculate).setOnClickListener(v -> calculateProFertilizer());
        findViewById(R.id.btn_share).setOnClickListener(v -> shareResult());
    }

    private void initializeRegionalUnits() {
        Map<String, Double> universal = new HashMap<>();
        universal.put("Acre / एकड़", 1.0);
        universal.put("Hectare / हेक्टेयर", 2.47);

        Map<String, Double> north = new HashMap<>(universal);
        north.put("Bigha / बीघा", 0.625); north.put("Kanal / कनाल", 0.125);

        Map<String, Double> south = new HashMap<>(universal);
        south.put("Cent / सेंट", 0.01); south.put("Guntha / गुंठा", 0.025);

        Map<String, Double> east = new HashMap<>(universal);
        east.put("Katha / कट्ठा", 0.031); east.put("Decimal / डेसीमल", 0.01);

        regionUnitMap.put("NORTH", north); regionUnitMap.put("SOUTH", south);
        regionUnitMap.put("EAST", east); regionUnitMap.put("WEST", south);
    }

    private void loadUnitsForState(String state) {
        String region = "NORTH";
        if (state.contains("Tamil") || state.contains("Karnataka") || state.contains("Kerala") || state.contains("Andhra")) region = "SOUTH";
        else if (state.contains("Bengal") || state.contains("Bihar") || state.contains("Assam")) region = "EAST";

        Map<String, Double> units = regionUnitMap.get(region);
        if (units != null) {
            String[] names = units.keySet().toArray(new String[0]);
            unitDropdown.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, names));
            unitDropdown.setText(names[0], false);
        }
    }

    private void calculateProFertilizer() {
        String crop = cropDropdown.getText().toString();
        String state = stateDropdown.getText().toString();
        String unit = unitDropdown.getText().toString();
        String areaVal = areaInput.getText().toString();

        if (crop.isEmpty() || state.isEmpty() || unit.isEmpty() || areaVal.isEmpty()) {
            Snackbar.make(rootView, "Please fill all info / कृपया जानकारी भरें", Snackbar.LENGTH_SHORT).show();
            return;
        }

        // Get conversion factor
        String region = "NORTH";
        if (state.contains("Tamil") || state.contains("Karnataka") || state.contains("Kerala") || state.contains("Andhra")) region = "SOUTH";
        else if (state.contains("Bengal") || state.contains("Bihar") || state.contains("Assam")) region = "EAST";

        double acres = Double.parseDouble(areaVal) * regionUnitMap.get(region).get(unit);

        // Find crop index
        int idx = 0;
        String[] crops = {"Wheat / गेहूं", "Rice / धान", "Maize / मक्का", "Cotton / कपास", "Sugarcane / गन्ना", "Soybean / सोयाबीन", "Mustard / सरसों", "Potato / आलू", "Onion / प्याज", "Chili / मिर्च"};
        for(int i=0; i<crops.length; i++) { if(crops[i].equals(crop)) idx = i; }

        double[] rates = NPK_BASE_RATES[idx];
        double netN = rates[0] * acres;
        double netP = rates[1] * acres;
        double netK = rates[2] * acres;

        // Bag Conversion
        double dapKg = netP / 0.46;
        double ureaKg = Math.max(0, (netN - (dapKg * 0.18)) / 0.46);
        double mopKg = netK / 0.60;

        // Nano Urea: 1 bottle (500ml) replaces 1 bag of Urea (approx 45kg N-content equivalent)
        int nanoBottles = (int) Math.ceil(ureaKg / 45.0);

        displayResults(netN, netP, netK, ureaKg, dapKg, mopKg, nanoBottles, rates);
        
        String promptContext = "Crop: " + crop + ", Area: " + areaVal + " " + unit + ". ";
        if (!suggestedFertsExtra.isEmpty()) {
            promptContext += "IMPORTANT: Farmer is treating a disease, you must strongly mention/incorporate using these cures: " + suggestedFertsExtra + " ";
        }
        promptContext += "Give a short tip on nutrient application.";
        
        fetchAIAdvise("Fertilizer Advice", promptContext);
    }

    private void displayResults(double n, double p, double k, double u, double d, double m, int nano, double[] base) {
        // 1. NPK Summary Row Style
        tvNRow.setText(String.format(Locale.getDefault(), "Nitrogen (N) / नाइट्रोजन:   %.0f kg/acre → %.1f kg", base[0], n));
        tvPRow.setText(String.format(Locale.getDefault(), "Phosphorus (P) / फॉस्फोरस:   %.0f kg/acre → %.1f kg", base[1], p));
        tvKRow.setText(String.format(Locale.getDefault(), "Potassium (K) / पोटेशियम:   %.0f kg/acre → %.1f kg", base[2], k));

        // 2. Shopping List (Bags + Loose)
        tvUreaBags.setText(formatBagString("Urea / यूरिया", u));
        tvDapBags.setText(formatBagString("DAP / डीएपी", d));
        tvMopBags.setText(formatBagString("MOP / एमओपी", m));

        // 3. Nano Urea
        tvNanoUrea.setText(String.format(Locale.getDefault(), "%d Bottle(s) of 500ml\n(यूरिया की जगह %d बोतल नैनो यूरिया का प्रयोग करें)", nano, nano));

        if (!suggestedFertsExtra.isEmpty()) {
            tvNanoUrea.append("\n\n🩺 Special Disease Cures / विशेष रोग उपचार:\n" + suggestedFertsExtra.replace(". ", "\n"));
        }

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
                            tvAiInsight.setVisibility(View.VISIBLE);
                            tvAiInsight.setText("✨ Expert Advice / विशेषज्ञ सलाह:\n" + resultText);
                        }
                    } catch (Exception e) {
                        // ignore error quietly
                    }
                });
            }
        });
    }

    private String formatBagString(String label, double totalKg) {
        int bags = (int) (totalKg / 50);
        int loose = (int) (totalKg % 50);
        return String.format(Locale.getDefault(), "%s: %d Bags + %d kg loose\n(%d बैग और %d किलो खुला)", label, bags, loose, bags, loose);
    }

    private void shareResult() {
        String res = "🌾 Fertilizer Advice\n" + tvUreaBags.getText() + "\n" + tvDapBags.getText() + "\n— Jai Kisan";
        android.content.Intent i = new android.content.Intent(android.content.Intent.ACTION_SEND);
        i.setType("text/plain"); i.putExtra(android.content.Intent.EXTRA_TEXT, res);
        startActivity(android.content.Intent.createChooser(i, "Share"));
    }
}