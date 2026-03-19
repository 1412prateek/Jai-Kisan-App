package com.rcc.jai_kisan;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import io.noties.markwon.Markwon;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MandiPricesActivity extends AppCompatActivity {

    AutoCompleteTextView spinnerState, spinnerCity, spinnerCrop, spinnerVariety, spinnerSortField, spinnerSortOrder;
    MaterialButton btnDateFrom, btnDateTo, btnReset, btnGetPrices;
    TextView tvPriceResults;
    private Markwon markwon;
    private CircularProgressIndicator loadingIndicator;
    private MaterialCardView cardResult;

    private String selectedSortField;
    private String selectedSortOrder;

    private Map<String, String[]> cropVarietiesMap;

    // API Key handled via BuildConfig for security
    private static final String GEMINI_API_KEY = BuildConfig.GEMINI_API_KEY;

    // Using Gemma 3 4B for high-performance generation
    private static final String GEMINI_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemma-3-4b-it:generateContent?key="
                    + GEMINI_API_KEY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mandi_prices);

        markwon = Markwon.create(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Mandi Prices / मंडी भाव");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        spinnerState = findViewById(R.id.spinnerState);
        spinnerCity = findViewById(R.id.spinnerCity);
        spinnerCrop = findViewById(R.id.spinnerCrop);
        spinnerVariety = findViewById(R.id.spinnerVariety);
        spinnerSortField = findViewById(R.id.spinnerSortField);
        spinnerSortOrder = findViewById(R.id.spinnerSortOrder);
        btnDateFrom = findViewById(R.id.btnDateFrom);
        btnDateTo = findViewById(R.id.btnDateTo);
        btnReset = findViewById(R.id.btnReset);
        btnGetPrices = findViewById(R.id.btnGetPrices);
        tvPriceResults = findViewById(R.id.tvPriceResults);
        loadingIndicator = findViewById(R.id.loadingIndicator);
        cardResult = findViewById(R.id.cardResult);

        initCropVarieties();

        // Setup States
        ArrayAdapter<CharSequence> stateAdapter = ArrayAdapter.createFromResource(this, R.array.states_array, android.R.layout.simple_dropdown_item_1line);
        spinnerState.setAdapter(stateAdapter);

        // Setup Crops
        String[] crops = {
                "Wheat / गेहूं", "Rice / चावल", "Onion / प्याज", "Potato / आलू",
                "Tomato / टमाटर", "Maize / मक्का", "Paddy / धान", "Mustard / सरसों",
                "Soyabean / सोयाबीन", "Cotton / कपास"
        };
        ArrayAdapter<String> cropAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, crops);
        spinnerCrop.setAdapter(cropAdapter);

        // Sort Config
        final String[] sortFieldDisplay = {"Date / तारीख", "Price / कीमत", "Min Price / न्यूनतम", "Max Price / अधिकतम"};
        final String[] sortFieldApi = {"arrival_date", "modal_price", "min_price", "max_price"};
        selectedSortField = sortFieldApi[0];

        final String[] sortOrderDisplay = {"Des / अवरोही", "Asc / आरोही"};
        final String[] sortOrderApi = {"desc", "asc"};
        selectedSortOrder = sortOrderApi[0];

        ArrayAdapter<String> sortFieldAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, sortFieldDisplay);
        spinnerSortField.setAdapter(sortFieldAdapter);
        spinnerSortField.setText(sortFieldDisplay[0], false);

        ArrayAdapter<String> sortOrderAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, sortOrderDisplay);
        spinnerSortOrder.setAdapter(sortOrderAdapter);
        spinnerSortOrder.setText(sortOrderDisplay[0], false);

        spinnerSortField.setOnItemClickListener((parent, view, position, id) -> selectedSortField = sortFieldApi[position]);
        spinnerSortOrder.setOnItemClickListener((parent, view, position, id) -> selectedSortOrder = sortOrderApi[position]);

        spinnerState.setOnItemClickListener((parent, view, position, id) -> {
            String selectedState = (String) parent.getItemAtPosition(position);
            spinnerCity.setText("", false);
            String englishStateName = selectedState.split("/")[0].trim();
            String arrayName = englishStateName.toLowerCase().replace(" ", "_").replace("-", "_") + "_cities";
            int citiesArrayId = getResources().getIdentifier(arrayName, "array", getPackageName());
            if (citiesArrayId != 0) {
                ArrayAdapter<CharSequence> cityAdapter = ArrayAdapter.createFromResource(this, citiesArrayId, android.R.layout.simple_dropdown_item_1line);
                spinnerCity.setAdapter(cityAdapter);
            }
        });

        spinnerCrop.setOnItemClickListener((parent, view, position, id) -> {
            String selectedCropFull = (String) parent.getItemAtPosition(position);
            String englishCropName = selectedCropFull.split("/")[0].trim();
            spinnerVariety.setText("", false);
            if (cropVarietiesMap.containsKey(englishCropName)) {
                ArrayAdapter<String> varietyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, cropVarietiesMap.get(englishCropName));
                spinnerVariety.setAdapter(varietyAdapter);
            }
        });

        btnDateFrom.setOnClickListener(v -> showDatePicker(btnDateFrom));
        btnDateTo.setOnClickListener(v -> showDatePicker(btnDateTo));

        btnReset.setOnClickListener(v -> resetFields(sortFieldDisplay));

        btnGetPrices.setOnClickListener(v -> {
            String state = spinnerState.getText().toString();
            String city = spinnerCity.getText().toString();
            String crop = spinnerCrop.getText().toString();
            String variety = spinnerVariety.getText().toString();

            if (state.isEmpty() || city.isEmpty() || crop.isEmpty()) {
                Toast.makeText(this, "Please select all required fields.\nकृपया सभी आवश्यक फ़ील्ड चुनें।", Toast.LENGTH_LONG).show();
                return;
            }
            fetchPrices(state, city, crop, variety);
        });
    }

    private void resetFields(String[] sortFieldDisplay) {
        spinnerState.setText("", false);
        spinnerCity.setAdapter(null);
        spinnerCity.setText("", false);
        spinnerCrop.setText("", false);
        spinnerVariety.setText("", false);
        spinnerSortField.setText(sortFieldDisplay[0], false);
        btnDateFrom.setText("From / से");
        btnDateTo.setText("To / तक");
        btnDateFrom.setTag(null);
        btnDateTo.setTag(null);
        tvPriceResults.setText("Market data will be shown here / बाज़ार का डेटा यहाँ दिखाया जाएगा।");
        cardResult.setVisibility(View.VISIBLE);
        loadingIndicator.setVisibility(View.GONE);
    }

    private void initCropVarieties() {
        cropVarietiesMap = new HashMap<>();
        cropVarietiesMap.put("Wheat", new String[]{"Lokwan / लोकवन", "Sharbati / शरबती", "Durum / डुरम", "Deshi / देसी", "Other / अन्य"});
        cropVarietiesMap.put("Rice", new String[]{"Basmati / बासमती", "Sona Masoori / सोना मसूरी", "Kolam / कोलम", "Other / अन्य"});
        cropVarietiesMap.put("Onion", new String[]{"Red / लाल", "White / सफेद", "Pink / गुलाबी", "Other / अन्य"});
        cropVarietiesMap.put("Potato", new String[]{"Jyoti / ज्योति", "Chandramukhi / चंद्रमुखी", "Sugar Free / शुगर फ्री", "Other / अन्य"});
        cropVarietiesMap.put("Tomato", new String[]{"Hybrid / हाइब्रिड", "Deshi / देसी", "Other / अन्य"});
        cropVarietiesMap.put("Maize", new String[]{"Yellow / पीला", "White / सफेद", "Other / अन्य"});
        cropVarietiesMap.put("Paddy", new String[]{"Common / सामान्य", "Basmati / बासमती", "Other / अन्य"});
        cropVarietiesMap.put("Mustard", new String[]{"Black / काली", "Yellow / पीली", "Other / अन्य"});
        cropVarietiesMap.put("Soyabean", new String[]{"Yellow / पीला", "Black / काला", "Other / अन्य"});
        cropVarietiesMap.put("Cotton", new String[]{"American / अमेरिकन", "Deshi / देसी", "Bt Cotton / बीटी कपास", "Other / अन्य"});
    }

    private void showDatePicker(MaterialButton button) {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Date / तारीख चुनें")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            SimpleDateFormat apiSdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            apiSdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            button.setTag(apiSdf.format(new Date(selection)));

            SimpleDateFormat displaySdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            button.setText(displaySdf.format(new Date(selection)));
        });
        datePicker.show(getSupportFragmentManager(), "date_picker");
    }

    private void fetchPrices(String state, String city, String crop, String variety) {
        runOnUiThread(() -> {
            loadingIndicator.setVisibility(View.VISIBLE);
            cardResult.setVisibility(View.GONE);
            btnGetPrices.setEnabled(false);
            // Neutral text - no mention of govt or expert
            tvPriceResults.setText("Generating Market Insights...\nबाज़ार की जानकारी प्राप्त की जा रही है...");
        });

        // Directly call AI API
        fetchFromGeminiApi(state, city, crop, variety);
    }

    private void fetchFromGeminiApi(String state, String city, String crop, String variety) {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(180, TimeUnit.SECONDS)
                .readTimeout(180, TimeUnit.SECONDS)
                .build();

        String fromDateStr = btnDateFrom.getTag() instanceof String ? (String) btnDateFrom.getTag() : "today";
        String toDateStr = btnDateTo.getTag() instanceof String ? (String) btnDateTo.getTag() : "";
        String dateInfo = fromDateStr + (toDateStr.isEmpty() ? "" : " to " + toDateStr);

        String prompt =
                "Generate a short bilingual mandi market insight for farmers.\n\n" +

                        "Crop: " + crop +
                        "\nVariety: " + (variety.isEmpty() ? "General" : variety) +
                        "\nLocation: " + city + ", " + state +
                        "\nDate: " + dateInfo + "\n\n" +

                        "Rules:\n" +
                        "- Keep response very short.\n" +
                        "- Use bullet points.\n" +
                        "- English section first, Hindi section after.\n" +
                        "- Do NOT write words like English or Hindi.\n" +
                        "- Do NOT mix languages in same paragraph.\n\n" +

                        "Format exactly like this:\n\n" +

                        "## Market Insight\n" +
                        "- Price Range: example min–max\n" +
                        "- Modal Price: short explanation\n" +
                        "- Trend: rising / stable / falling\n" +
                        "- Weather Impact: short line\n" +
                        "- Farmer Advice: short practical tip\n\n" +

                        "## बाजार जानकारी\n" +
                        "- मूल्य सीमा: उदाहरण न्यूनतम–अधिकतम\n" +
                        "- सामान्य मूल्य: छोटा अर्थ\n" +
                        "- बाजार प्रवृत्ति: बढ़त / स्थिर / गिरावट\n" +
                        "- मौसम प्रभाव: छोटा वाक्य\n" +
                        "- किसान सलाह: व्यावहारिक सुझाव\n\n" +

                        "Keep it concise and farmer friendly.";

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
                runOnUiThread(() -> handleError("Network error. Please try again.\nनेटवर्क त्रुटि। कृपया पुनः प्रयास करें।"));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String responseBody = response.body() != null ? response.body().string() : "";
                runOnUiThread(() -> {
                    try {
                        if (response.isSuccessful()) {
                            JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
                            String resultText = json.getAsJsonArray("candidates").get(0).getAsJsonObject()
                                    .getAsJsonObject("content").getAsJsonArray("parts").get(0).getAsJsonObject().get("text").getAsString();
                            markwon.setMarkdown(tvPriceResults, resultText);
                        } else {
                            handleError("Service temporarily unavailable.\nसेवा अस्थायी रूप से अनुपलब्ध है।");
                        }
                    } catch (Exception e) {
                        handleError("Error processing data.\nडेटा संसाधित करने में त्रुटि।");
                    }
                    loadingIndicator.setVisibility(View.GONE);
                    cardResult.setVisibility(View.VISIBLE);
                    btnGetPrices.setEnabled(true);
                });
            }
        });
    }

    private void handleError(String message) {
        tvPriceResults.setText(message);
        loadingIndicator.setVisibility(View.GONE);
        cardResult.setVisibility(View.VISIBLE);
        btnGetPrices.setEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}