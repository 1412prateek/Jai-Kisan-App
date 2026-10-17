package com.rcc.jai_kisan;

import android.content.Intent;
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

public class MandiPricesActivity extends BaseActivity {

    AutoCompleteTextView spinnerState, spinnerCity, spinnerCrop, spinnerVariety, spinnerSortField, spinnerSortOrder;
    MaterialButton btnDateFrom, btnDateTo, btnReset, btnGetPrices;
    TextView tvPriceResults;
    private Markwon markwon;
    private View loadingIndicator;
    private View cardResult;
    // Container view captured for PDF rendering
    private View resultPdfContainer;

    private TextView tvUiModalPrice, tvUiTrend, tvUiPriceRange, tvUiWeatherImpact, tvUiFarmerAdvice;
    private TextView tvUiModalPriceSuffix, tvUiPriceRangeSuffix, tvUiDescriptionEn, tvUiDescriptionHi;
    private TextView tvUiWeatherImpactEn, tvUiWeatherImpactHi, tvUiFarmerAdviceEn, tvUiFarmerAdviceHi;
    private TextView tvUiModalPriceKg, tvUiModalPriceKgSuffix;
    private PriceTrendChartView priceTrendChartView;
    private TextView tvUiTrendDescEn, tvUiTrendDescHi;

    private String selectedSortField;
    private String selectedSortOrder;

    private Map<String, String[]> cropVarietiesMap;

    private static final String[] MODELS = {
            "gemini-2.5-flash",
            "gemini-2.0-flash",
            "gemini-1.5-flash"
    };

    private String buildUrl(String modelName) {
        return "https://generativelanguage.googleapis.com/v1beta/models/" + modelName + ":generateContent?key="
                + GeminiKeyManager.getNextKey();
    }

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
        resultPdfContainer = cardResult;

        tvUiModalPrice = findViewById(R.id.tvUiModalPrice);
        tvUiTrend = findViewById(R.id.tvUiTrend);
        tvUiPriceRange = findViewById(R.id.tvUiPriceRange);
        tvUiModalPriceSuffix = findViewById(R.id.tvUiModalPriceSuffix);
        tvUiPriceRangeSuffix = findViewById(R.id.tvUiPriceRangeSuffix);
        tvUiModalPriceKg = findViewById(R.id.tvUiModalPriceKg);
        tvUiModalPriceKgSuffix = findViewById(R.id.tvUiModalPriceKgSuffix);
        tvUiDescriptionEn = findViewById(R.id.tvUiDescriptionEn);
        tvUiDescriptionHi = findViewById(R.id.tvUiDescriptionHi);
        tvUiWeatherImpactEn = findViewById(R.id.tvUiWeatherImpactEn);
        tvUiWeatherImpactHi = findViewById(R.id.tvUiWeatherImpactHi);
        tvUiFarmerAdviceEn = findViewById(R.id.tvUiFarmerAdviceEn);
        tvUiFarmerAdviceHi = findViewById(R.id.tvUiFarmerAdviceHi);
        priceTrendChartView = findViewById(R.id.priceTrendChartView);
        tvUiTrendDescEn = findViewById(R.id.tvUiTrendDescEn);
        tvUiTrendDescHi = findViewById(R.id.tvUiTrendDescHi);

        initCropVarieties();

        // Setup States
        ArrayAdapter<CharSequence> stateAdapter = ArrayAdapter.createFromResource(this, R.array.states_array,
                android.R.layout.simple_dropdown_item_1line);
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
        String[] sortFields = { "Date", "Market", "Modal Price" };
        String[] sortFieldDisplay = { "Date / तारीख", "Market / मंडी", "Price / कीमत" };
        ArrayAdapter<String> sortFieldAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line,
                sortFieldDisplay);
        spinnerSortField.setAdapter(sortFieldAdapter);
        spinnerSortField.setText(sortFieldDisplay[0], false);
        selectedSortField = sortFields[0];

        spinnerSortField.setOnItemClickListener((parent, view, position, id) -> {
            selectedSortField = sortFields[position];
        });

        String[] sortOrders = { "ASC", "DESC" };
        String[] sortOrderDisplay = { "Ascending / बढ़ता क्रम", "Descending / घटता क्रम" };
        ArrayAdapter<String> sortOrderAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line,
                sortOrderDisplay);
        spinnerSortOrder.setAdapter(sortOrderAdapter);
        spinnerSortOrder.setText(sortOrderDisplay[0], false);
        selectedSortOrder = sortOrders[0];

        spinnerSortOrder.setOnItemClickListener((parent, view, position, id) -> {
            selectedSortOrder = sortOrders[position];
        });

        spinnerState.setOnItemClickListener((parent, view, position, id) -> {
            String selectedState = parent.getItemAtPosition(position).toString();
            // Remove localized suffix if exists for loading resources
            if (selectedState.contains("/")) {
                selectedState = selectedState.substring(0, selectedState.indexOf("/")).trim();
            }
            int citiesResId = getResources().getIdentifier(
                    selectedState.toLowerCase().replace(" ", "_").replace("-", "_") + "_cities", "array", getPackageName());
            if (citiesResId != 0) {
                ArrayAdapter<CharSequence> cityAdapter = ArrayAdapter.createFromResource(MandiPricesActivity.this,
                        citiesResId, android.R.layout.simple_dropdown_item_1line);
                spinnerCity.setAdapter(cityAdapter);
                spinnerCity.setText("", false);
            }
        });

        spinnerCrop.setOnItemClickListener((parent, view, position, id) -> {
            String selectedCrop = parent.getItemAtPosition(position).toString();
            if (selectedCrop.contains("/")) {
                selectedCrop = selectedCrop.substring(0, selectedCrop.indexOf("/")).trim();
            }
            String[] varieties = cropVarietiesMap.get(selectedCrop);
            if (varieties != null) {
                ArrayAdapter<String> varietyAdapter = new ArrayAdapter<>(MandiPricesActivity.this,
                        android.R.layout.simple_dropdown_item_1line, varieties);
                spinnerVariety.setAdapter(varietyAdapter);
                spinnerVariety.setText("", false);
            } else {
                spinnerVariety.setAdapter(null);
                spinnerVariety.setText("", false);
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
                Toast.makeText(this, "Please select all required fields.\nकृपया सभी आवश्यक फ़ील्ड चुनें।",
                        Toast.LENGTH_LONG).show();
                return;
            }
            fetchPrices(state, city, crop, variety);
        });

        // Share button — shows M3 bottom sheet with Text / PDF options
        MaterialButton btnSharePdf = findViewById(R.id.btnSharePdf);
        if (btnSharePdf != null) {
            btnSharePdf.setOnClickListener(v -> {
                if (cardResult.getVisibility() != View.VISIBLE) {
                    Toast.makeText(this, "Please fetch prices first.\nपहले कीमत प्राप्त करें।", Toast.LENGTH_SHORT)
                            .show();
                    return;
                }
                ShareHelper.showShareDialog(MandiPricesActivity.this, new ShareHelper.ShareCallback() {
                    @Override
                    public void onShareText() {
                        shareAsText();
                    }

                    @Override
                    public void onSharePdf() {
                        shareAsPdf();
                    }
                });
            });
        }
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
        cropVarietiesMap.put("Wheat",
                new String[] { "Lokwan / लोकवन", "Sharbati / शरबती", "Durum / डुरम", "Deshi / देसी", "Other / अन्य" });
        cropVarietiesMap.put("Rice",
                new String[] { "Basmati / बासमती", "Sona Masoori / सोना मसूरी", "Kolam / कोलम", "Other / अन्य" });
        cropVarietiesMap.put("Onion", new String[] { "Red / लाल", "White / सफेद", "Pink / गुलाबी", "Other / अन्य" });
        cropVarietiesMap.put("Potato", new String[] { "Jyoti / ज्योति", "Chandramukhi / चंद्रमुखी",
                "Sugar Free / शुगर फ्री", "Other / अन्य" });
        cropVarietiesMap.put("Tomato", new String[] { "Hybrid / हाइब्रिड", "Deshi / देसी", "Other / अन्य" });
        cropVarietiesMap.put("Maize", new String[] { "Yellow / पीला", "White / सफेद", "Other / अन्य" });
        cropVarietiesMap.put("Paddy", new String[] { "Common / सामान्य", "Basmati / बासमती", "Other / अन्य" });
        cropVarietiesMap.put("Mustard", new String[] { "Black / काली", "Yellow / पीली", "Other / अन्य" });
        cropVarietiesMap.put("Soyabean", new String[] { "Yellow / पीला", "Black / काला", "Other / अन्य" });
        cropVarietiesMap.put("Cotton",
                new String[] { "American / अमेरिकन", "Deshi / देसी", "Bt Cotton / बीटी कपास", "Other / अन्य" });
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
        fetchFromGeminiApi(state, city, crop, variety, 0, 0);
    }

    private void fetchFromGeminiApi(String state, String city, String crop, String variety, int modelIndex, int keyRetryCount) {
        if (modelIndex >= MODELS.length) {
            runOnUiThread(() -> handleError(
                    "All models are currently busy. Please try again in a minute.\nसभी मॉडल व्यस्त हैं। एक मिनट बाद पुनः प्रयास करें।"));
            return;
        }

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(180, TimeUnit.SECONDS)
                .readTimeout(180, TimeUnit.SECONDS)
                .build();

        String fromDateStr = btnDateFrom.getTag() instanceof String ? (String) btnDateFrom.getTag() : "today";
        String toDateStr = btnDateTo.getTag() instanceof String ? (String) btnDateTo.getTag() : "";
        String dateInfo = fromDateStr + (toDateStr.isEmpty() ? "" : " to " + toDateStr);
        String selectedSort = spinnerSortField.getText().toString();
        String selectedOrder = spinnerSortOrder.getText().toString();
        String sortInfo = "Sort by: " + selectedSort + " (" + selectedOrder + ")";

        String prompt = "Generate a short bilingual mandi market insight and a 7-day price trend for farmers.\n\n" +

                "Crop: " + crop +
                "\nVariety: " + (variety.isEmpty() ? "General" : variety) +
                "\nLocation: " + city + ", " + state +
                "\nDate range filter: " + dateInfo +
                "\nSort filter: " + sortInfo + "\n\n" +

                "Rules:\n" +
                "- Keep response short.\n" +
                "- Use bullet points.\n" +
                "- English section first, Hindi section after.\n" +
                "- Do NOT write words like English or Hindi.\n" +
                "- Do NOT mix languages in same paragraph.\n" +
                "- Make sure to mention the selected date range and sorting filters clearly in the results.\n\n" +

                "Format exactly like this:\n\n" +

                "## Market Insight\n" +
                "- Price Range: ₹MIN–₹MAX per quintal\n" +
                "- Modal Price: ₹NUMBER_ONLY (example: ₹1500). Write ONLY rupee symbol and the number. No text, no unit, no explanation.\n" +
                "- Trend: rising / stable / falling\n" +
                "- 7-Day Prices: 1150,1400,1590,1650,1800,1920,2110\n" +
                "- 7-Day Description: short description including date/sort preferences\n" +
                "- Weather Impact: short line\n" +
                "- Farmer Advice: short practical tip\n\n" +

                "## बाजार जानकारी\n" +
                "- मूल्य सीमा: ₹न्यूनतम–₹अधिकतम प्रति क्विंटल\n" +
                "- सामान्य मूल्य: ₹संख्या_मात्र (उदाहरण: ₹1500). केवल रुपए का चिन्ह और संख्या लिखें। कोई शब्द, इकाई या स्पष्टीकरण नहीं।\n" +
                "- बाजार प्रवृत्ति: बढ़त / स्थिर / गिरावट\n" +
                "- 7-Day Prices Hindi: 1150,1400,1590,1650,1800,1920,2110\n" +
                "- 7-Day Description Hindi: तिथि और सॉर्ट प्राथमिकताओं सहित छोटा विवरण\n" +
                "- मौसम प्रभाव: छोटा वाक्य\n" +
                "- किसान सलाह: व्यावहारिक सुझाव\n\n" +

                "Keep it concise, clear, and farmer friendly.";

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
        String url = buildUrl(MODELS[modelIndex]);
        Request request = new Request.Builder().url(url).post(body).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                android.util.Log.e("MandiDebug", "Network failure on model " + MODELS[modelIndex], e);
                runOnUiThread(() -> {
                    if (keyRetryCount < 3) {
                        fetchFromGeminiApi(state, city, crop, variety, modelIndex, keyRetryCount + 1);
                    } else {
                        fetchFromGeminiApi(state, city, crop, variety, modelIndex + 1, 0);
                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String responseBody = response.body() != null ? response.body().string() : "";
                android.util.Log.d("MandiDebug", "Model=" + MODELS[modelIndex] + " code=" + response.code() + " body=" + responseBody);
                runOnUiThread(() -> {
                    if (response.code() == 503 || response.code() == 429 || response.code() == 404) {
                        if (keyRetryCount < 3) {
                            fetchFromGeminiApi(state, city, crop, variety, modelIndex, keyRetryCount + 1);
                        } else {
                            fetchFromGeminiApi(state, city, crop, variety, modelIndex + 1, 0);
                        }
                        return;
                    }
                    try {
                        if (response.isSuccessful()) {
                            JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
                            String resultText = json.getAsJsonArray("candidates").get(0).getAsJsonObject()
                                    .getAsJsonObject("content").getAsJsonArray("parts").get(0).getAsJsonObject()
                                    .get("text").getAsString();
                            markwon.setMarkdown(tvPriceResults, resultText);
                            tvPriceResults.setText(resultText); // Keep text updated for PDF sharing
                            populateUiFields(resultText);
                        } else if (keyRetryCount < 3) {
                            android.util.Log.e("MandiDebug", "Non-2xx response, retrying same model with new key");
                            fetchFromGeminiApi(state, city, crop, variety, modelIndex, keyRetryCount + 1);
                            return;
                        } else if (modelIndex + 1 < MODELS.length) {
                            android.util.Log.e("MandiDebug", "Non-2xx response, retrying next model");
                            fetchFromGeminiApi(state, city, crop, variety, modelIndex + 1, 0);
                            return;
                        } else {
                            handleError("Service temporarily unavailable.\nसेवा अस्थायी रूप से अनुपलब्ध है।");
                        }
                    } catch (Exception e) {
                        android.util.Log.e("MandiDebug", "Parse error on model " + MODELS[modelIndex] + ", body=" + responseBody, e);
                        if (keyRetryCount < 3) {
                            fetchFromGeminiApi(state, city, crop, variety, modelIndex, keyRetryCount + 1);
                            return;
                        } else if (modelIndex + 1 < MODELS.length) {
                            fetchFromGeminiApi(state, city, crop, variety, modelIndex + 1, 0);
                            return;
                        }
                        handleError("Error processing data.\nडेटा संसाधित करने में त्रुटि।");
                    }
                    loadingIndicator.setVisibility(View.GONE);
                    cardResult.setVisibility(View.VISIBLE);
                    btnGetPrices.setEnabled(true);
                });
            }
        });
    }

    private void populateUiFields(String resultText) {
        String priceRangeEn = "", priceRangeHi = "";
        String modalPriceEn = "", modalPriceHi = "";
        String trendEn = "", trendHi = "";
        String weatherEn = "", weatherHi = "";
        String adviceEn = "", adviceHi = "";
        String trendPricesEn = "", trendPricesHi = "";
        String trendDescEn = "", trendDescHi = "";

        String[] lines = resultText.split("\n");
        boolean isHindiSection = false;

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;
            if (trimmed.contains("## बाजार जानकारी") || trimmed.contains("## बाज़ार जानकारी") || trimmed.contains("## बाजार") || trimmed.contains("## बाज़ार")) {
                isHindiSection = true;
                continue;
            }

            if (trimmed.startsWith("-")) {
                String content = trimmed.substring(1).trim();
                int colonIdx = content.indexOf(":");
                if (colonIdx != -1) {
                    String label = content.substring(0, colonIdx).trim().toLowerCase();
                    String value = content.substring(colonIdx + 1).trim();
                    if (!isHindiSection) {
                        if (label.contains("price range")) {
                            priceRangeEn = value;
                        } else if (label.contains("modal price")) {
                            modalPriceEn = value;
                        } else if (label.contains("trend")) {
                            trendEn = value;
                        } else if (label.contains("7-day prices") || label.contains("prices")) {
                            trendPricesEn = value;
                        } else if (label.contains("7-day description") || label.contains("description")) {
                            trendDescEn = value;
                        } else if (label.contains("weather")) {
                            weatherEn = value;
                        } else if (label.contains("advice") || label.contains("advisory")) {
                            adviceEn = value;
                        }
                    } else {
                        if (label.contains("मूल्य सीमा") || label.contains("सीमा")) {
                            priceRangeHi = value;
                        } else if (label.contains("सामान्य मूल्य") || label.contains("मूल्य")) {
                            modalPriceHi = value;
                        } else if (label.contains("प्रवृत्ति") || label.contains("रुख")) {
                            trendHi = value;
                        } else if (label.contains("7-day prices hindi") || label.contains("prices hindi")) {
                            trendPricesHi = value;
                        } else if (label.contains("7-day description hindi") || label.contains("description hindi") || label.contains("विवरण")) {
                            trendDescHi = value;
                        } else if (label.contains("मौसम")) {
                            weatherHi = value;
                        } else if (label.contains("सलाह") || label.contains("सुझाव")) {
                            adviceHi = value;
                        }
                    }
                }
            }
        }

        // 1. Modal Price – extract ONLY the number (₹1500 or just 1500)
        // Prompt guarantees only a number is returned, but defensively strip any text
        long modalPriceNumPerQuintal = 0;
        String modalPriceVal = "--";
        String modalPriceKgVal = "--";

        // Try to extract any digits from the raw value (handles ₹1500, ₹ 1,500, 1500 etc.)
        java.util.regex.Pattern numOnlyPattern = java.util.regex.Pattern.compile("[₹\\s]*([\\d,]+)");
        java.util.regex.Matcher numMatcher = numOnlyPattern.matcher(modalPriceEn);
        if (numMatcher.find()) {
            String digits = numMatcher.group(1).replaceAll(",", "").trim();
            try {
                modalPriceNumPerQuintal = Long.parseLong(digits);
            } catch (NumberFormatException ignored) {}
        }

        if (modalPriceNumPerQuintal > 0) {
            // Format per-quintal with ₹
            modalPriceVal = "₹" + String.format("%,d", modalPriceNumPerQuintal);
            // Convert quintal → kg: 1 quintal = 100 kg
            double perKg = modalPriceNumPerQuintal / 100.0;
            // Show with 2 decimal places if fractional, else integer
            if (perKg == Math.floor(perKg)) {
                modalPriceKgVal = "₹" + String.format("%,d", (long) perKg);
            } else {
                modalPriceKgVal = "₹" + String.format("%.2f", perKg);
            }
        } else if (!modalPriceEn.isEmpty()) {
            // Fallback: just show whatever the AI returned stripped of non-price text
            modalPriceVal = modalPriceEn.replaceAll("(?i)(per\\s*(quintal|kg|q|kilogram)|inr|rs\\.?|rupees?|representative\\s*price|एक.*|a\\s+\\w.*)", "").trim();
            if (!modalPriceVal.startsWith("₹")) modalPriceVal = "₹" + modalPriceVal;
            modalPriceKgVal = "--";
        }

        if (tvUiModalPrice != null) {
            tvUiModalPrice.setText(modalPriceVal);
        }
        if (tvUiModalPriceSuffix != null) {
            tvUiModalPriceSuffix.setText("/क्विंटल");
        }
        if (tvUiModalPriceKg != null) {
            tvUiModalPriceKg.setText(modalPriceKgVal);
        }
        if (tvUiModalPriceKgSuffix != null) {
            tvUiModalPriceKgSuffix.setText("/किलो");
        }

        // Description Paragraphs
        String descEn = "";
        if (modalPriceEn.contains("(") && modalPriceEn.contains(")")) {
            descEn = modalPriceEn.substring(modalPriceEn.indexOf("(") + 1, modalPriceEn.indexOf(")")).trim();
        } else {
            descEn = "Prices generally settling around " + modalPriceEn + ".";
        }
        String descHi = "";
        if (modalPriceHi.contains("(") && modalPriceHi.contains(")")) {
            descHi = modalPriceHi.substring(modalPriceHi.indexOf("(") + 1, modalPriceHi.indexOf(")")).trim();
        } else {
            descHi = "कीमतें आमतौर पर " + modalPriceHi + " के आसपास स्थिर हैं।";
        }

        if (tvUiDescriptionEn != null) {
            tvUiDescriptionEn.setText(descEn);
        }
        if (tvUiDescriptionHi != null) {
            tvUiDescriptionHi.setText(descHi);
        }

        // 2. Trend Pill Badge
        if (tvUiTrend != null) {
            String cleanTrendEn = trendEn.toLowerCase();
            String arrowSymbol = "→ ";
            int trendColor = 0xFF155724; // Dark green
            int trendBgTint = 0xFFD4EDDA; // Light green

            if (cleanTrendEn.contains("rising") || cleanTrendEn.contains("up") || cleanTrendEn.contains("high") || cleanTrendEn.contains("बढ़त")) {
                arrowSymbol = "↑ ";
                trendColor = 0xFF155724;
                trendBgTint = 0xFFD4EDDA;
            } else if (cleanTrendEn.contains("falling") || cleanTrendEn.contains("down") || cleanTrendEn.contains("low") || cleanTrendEn.contains("गिरावट")) {
                arrowSymbol = "↓ ";
                trendColor = 0xFF721C24; // Dark red
                trendBgTint = 0xFFF8D7DA; // Light red
            } else {
                arrowSymbol = "→ ";
                trendColor = 0xFF155724;
                trendBgTint = 0xFFD4EDDA;
            }

            tvUiTrend.setText(arrowSymbol + trendEn + " | " + trendHi);
            tvUiTrend.setTextColor(trendColor);
            View parentLayout = (View) tvUiTrend.getParent();
            if (parentLayout != null) {
                parentLayout.setBackgroundTintList(android.content.res.ColorStateList.valueOf(trendBgTint));
            }
        }

        // 3. 7-Day Trend Chart
        int[] parsedPrices = null;
        String sourcePrices = !trendPricesEn.isEmpty() ? trendPricesEn : trendPricesHi;
        if (!sourcePrices.isEmpty()) {
            try {
                String[] parts = sourcePrices.split(",");
                if (parts.length == 7) {
                    parsedPrices = new int[7];
                    for (int i = 0; i < 7; i++) {
                        String cleanNum = parts[i].replaceAll("[^0-9]", "");
                        parsedPrices[i] = Integer.parseInt(cleanNum);
                    }
                }
            } catch (Exception ignored) {}
        }
        if (parsedPrices != null && priceTrendChartView != null) {
            priceTrendChartView.setPriceData(parsedPrices);
        }

        if (tvUiTrendDescEn != null) {
            tvUiTrendDescEn.setText(trendDescEn);
        }
        if (tvUiTrendDescHi != null) {
            tvUiTrendDescHi.setText(trendDescHi);
        }

        // 4. Price Range
        java.util.regex.Pattern valPattern = java.util.regex.Pattern.compile("(₹\\s*\\d+(?:,\\d+)*(?:\\s*(?:-|–)\\s*₹?\\s*\\d+(?:,\\d+)*)?)(.*)");;
        String priceRangeVal = "";
        String priceRangeSuffix = "";
        java.util.regex.Matcher mRange = valPattern.matcher(priceRangeEn);
        if (mRange.find()) {
            priceRangeVal = mRange.group(1).trim();
            String suffix = mRange.group(2).trim();
            if (suffix.toLowerCase().contains("quintal") || suffix.toLowerCase().contains("क्विंटल")) {
                priceRangeSuffix = "/quintal";
            } else if (suffix.toLowerCase().contains("kg") || suffix.toLowerCase().contains("किग्रा")) {
                priceRangeSuffix = "/kg";
            } else {
                priceRangeSuffix = suffix;
            }
        } else {
            java.util.regex.Pattern fallbackRangePattern = java.util.regex.Pattern.compile("(₹?\\s*\\d+(?:,\\d+)*(?:\\s*(?:-|–)\\s*₹?\\s*\\d+(?:,\\d+)*)?)");
            java.util.regex.Matcher mRangeFallback = fallbackRangePattern.matcher(priceRangeEn);
            if (mRangeFallback.find()) {
                priceRangeVal = mRangeFallback.group(1).trim();
                if (!priceRangeVal.startsWith("₹")) {
                    priceRangeVal = "₹" + priceRangeVal;
                }
                String lower = priceRangeEn.toLowerCase();
                if (lower.contains("quintal") || lower.contains("क्विंटल")) {
                    priceRangeSuffix = "/quintal";
                } else if (lower.contains("kg") || lower.contains("किग्रा")) {
                    priceRangeSuffix = "/kg";
                }
            } else {
                priceRangeVal = priceRangeEn;
            }
        }

        if (tvUiPriceRange != null) {
            tvUiPriceRange.setText(priceRangeVal);
        }
        if (tvUiPriceRangeSuffix != null) {
            tvUiPriceRangeSuffix.setText(priceRangeSuffix);
        }

        // 5. Weather Impact Card
        if (tvUiWeatherImpactEn != null) {
            tvUiWeatherImpactEn.setText(weatherEn);
        }
        if (tvUiWeatherImpactHi != null) {
            tvUiWeatherImpactHi.setText(weatherHi);
        }

        // 6. Farmer Advisory Card
        if (tvUiFarmerAdviceEn != null) {
            tvUiFarmerAdviceEn.setText(adviceEn);
        }
        if (tvUiFarmerAdviceHi != null) {
            tvUiFarmerAdviceHi.setText(adviceHi);
        }
    }

    private void handleError(String message) {
        tvPriceResults.setText(message);
        loadingIndicator.setVisibility(View.GONE);
        cardResult.setVisibility(View.VISIBLE);
        btnGetPrices.setEnabled(true);
    }

    private void shareAsText() {
        String state = spinnerState.getText().toString();
        String city = spinnerCity.getText().toString();
        String crop = spinnerCrop.getText().toString();
        String variety = spinnerVariety.getText().toString();
        String resultText = tvPriceResults.getText().toString();

        StringBuilder sb = new StringBuilder();
        sb.append("*जय किसान - Mandi Price Report / मंडी भाव रिपोर्ट*\n");
        sb.append("----------------------------------\n");
        if (!crop.isEmpty())
            sb.append("Crop / फसल: ").append(crop).append("\n");
        if (!variety.isEmpty())
            sb.append("Variety / किस्म: ").append(variety).append("\n");
        if (!city.isEmpty())
            sb.append("Market / मंडी: ").append(city).append(", ").append(state).append("\n\n");
        sb.append(resultText).append("\n\n");
        sb.append(
                "इस प्रोग्राम का आधार एआई विश्लेषण है. कृपया अंतिम निर्णय लेने से पहले स्वयं सत्यापित करें।\nGenerated via Jai Kisan App");

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, sb.toString());
        startActivity(Intent.createChooser(shareIntent, "Share via / साझा करें"));
    }

    private void shareAsPdf() {
        String state = spinnerState.getText().toString();
        String city = spinnerCity.getText().toString();
        String crop = spinnerCrop.getText().toString();
        String variety = spinnerVariety.getText().toString();
        String resultText = tvPriceResults.getText().toString();

        // Parse fields
        String priceRangeEn = "", priceRangeHi = "";
        String modalPriceEn = "", modalPriceHi = "";
        String trendEn = "", trendHi = "";
        String weatherEn = "", weatherHi = "";
        String adviceEn = "", adviceHi = "";
        String trendPricesEn = "", trendPricesHi = "";
        String trendDescEn = "", trendDescHi = "";

        String[] lines = resultText.split("\n");
        boolean isHindiSection = false;

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;
            if (trimmed.contains("## बाजार जानकारी") || trimmed.contains("## बाज़ार जानकारी") || trimmed.contains("## बाजार") || trimmed.contains("## बाज़ार")) {
                isHindiSection = true;
                continue;
            }

            if (trimmed.startsWith("-")) {
                String content = trimmed.substring(1).trim();
                int colonIdx = content.indexOf(":");
                if (colonIdx != -1) {
                    String label = content.substring(0, colonIdx).trim().toLowerCase();
                    String value = content.substring(colonIdx + 1).trim();
                    if (!isHindiSection) {
                        if (label.contains("price range")) {
                            priceRangeEn = value;
                        } else if (label.contains("modal price")) {
                            modalPriceEn = value;
                        } else if (label.contains("trend")) {
                            trendEn = value;
                        } else if (label.contains("7-day prices") || label.contains("prices")) {
                            trendPricesEn = value;
                        } else if (label.contains("7-day description") || label.contains("description")) {
                            trendDescEn = value;
                        } else if (label.contains("weather")) {
                            weatherEn = value;
                        } else if (label.contains("advice") || label.contains("advisory")) {
                            adviceEn = value;
                        }
                    } else {
                        if (label.contains("मूल्य सीमा") || label.contains("सीमा")) {
                            priceRangeHi = value;
                        } else if (label.contains("सामान्य मूल्य") || label.contains("मूल्य")) {
                            modalPriceHi = value;
                        } else if (label.contains("प्रवृत्ति") || label.contains("रुख")) {
                            trendHi = value;
                        } else if (label.contains("7-day prices hindi") || label.contains("prices hindi")) {
                            trendPricesHi = value;
                        } else if (label.contains("7-day description hindi") || label.contains("description hindi") || label.contains("विवरण")) {
                            trendDescHi = value;
                        } else if (label.contains("मौसम")) {
                            weatherHi = value;
                        } else if (label.contains("सलाह") || label.contains("सुझाव")) {
                            adviceHi = value;
                        }
                    }
                }
            }
        }

        // Parse clean parameters for the PDF table
        String pdfModalEn = "", pdfModalHi = "";
        String pdfRangeEn = "", pdfRangeHi = "";

        // Parse Modal
        java.util.regex.Pattern numPattern = java.util.regex.Pattern.compile("(₹\\s*\\d+(?:,\\d+)*)");
        java.util.regex.Matcher mModalEn = numPattern.matcher(modalPriceEn);
        if (mModalEn.find()) {
            String val = mModalEn.group(1).trim();
            String lower = modalPriceEn.toLowerCase();
            if (lower.contains("kg") || lower.contains("किग्रा")) {
                pdfModalEn = val + " /kg";
                pdfModalHi = val + " /किग्रा";
            } else {
                pdfModalEn = val + " /quintal";
                pdfModalHi = val + " /क्विंटल";
            }
        } else {
            pdfModalEn = modalPriceEn;
            pdfModalHi = modalPriceHi;
        }

        // Parse Range
        java.util.regex.Pattern rangePattern = java.util.regex.Pattern.compile("(₹?\\s*\\d+(?:,\\d+)*(?:\\s*(?:-|–)\\s*₹?\\s*\\d+(?:,\\d+)*)?)");
        java.util.regex.Matcher mRangeEn = rangePattern.matcher(priceRangeEn);
        if (mRangeEn.find()) {
            String val = mRangeEn.group(1).trim();
            if (!val.startsWith("₹")) {
                val = "₹" + val;
            }
            String lower = priceRangeEn.toLowerCase();
            if (lower.contains("kg") || lower.contains("किग्रा")) {
                pdfRangeEn = val + " /kg";
                pdfRangeHi = val + " /किग्रा";
            } else {
                pdfRangeEn = val + " /quintal";
                pdfRangeHi = val + " /क्विंटल";
            }
        } else {
            pdfRangeEn = priceRangeEn;
            pdfRangeHi = priceRangeHi;
        }

        // SVG Graph Calculations
        int[] trendInts = new int[]{1150, 1400, 1590, 1650, 1800, 1920, 2110};
        String sourcePrices = !trendPricesEn.isEmpty() ? trendPricesEn : trendPricesHi;
        if (!sourcePrices.isEmpty()) {
            try {
                String[] parts = sourcePrices.split(",");
                if (parts.length == 7) {
                    for (int i = 0; i < 7; i++) {
                        trendInts[i] = Integer.parseInt(parts[i].replaceAll("[^0-9]", ""));
                    }
                }
            } catch (Exception ignored) {}
        }

        int minPriceVal = Integer.MAX_VALUE;
        int maxPriceVal = Integer.MIN_VALUE;
        for (int p : trendInts) {
            if (p < minPriceVal) minPriceVal = p;
            if (p > maxPriceVal) maxPriceVal = p;
        }
        int priceDiff = maxPriceVal - minPriceVal;
        if (priceDiff == 0) {
            maxPriceVal += 100;
            minPriceVal = Math.max(0, minPriceVal - 100);
        } else {
            maxPriceVal += (int)(priceDiff * 0.15);
            minPriceVal = Math.max(0, minPriceVal - (int)(priceDiff * 0.15));
        }
        minPriceVal = (minPriceVal / 10) * 10;
        maxPriceVal = ((maxPriceVal + 9) / 10) * 10;

        float svgWidth = 600f;
        float svgHeight = 200f;
        float padLeft = 80f;
        float padRight = 30f;
        float padTop = 30f;
        float padBottom = 40f;
        float chartW = svgWidth - padLeft - padRight;
        float chartH = svgHeight - padTop - padBottom;

        StringBuilder polylinePoints = new StringBuilder();
        StringBuilder svgCircles = new StringBuilder();
        StringBuilder svgTexts = new StringBuilder();

        for (int i = 0; i < 7; i++) {
            float x = padLeft + ((float) i / 6 * chartW);
            float ratio = (float) (trendInts[i] - minPriceVal) / (maxPriceVal - minPriceVal);
            float y = svgHeight - padBottom - (ratio * chartH);

            polylinePoints.append(x).append(",").append(y).append(" ");
            svgCircles.append("<circle cx='").append(x).append("' cy='").append(y).append("' r='5' fill='#4A5C2D' />\n");
            svgTexts.append("<text x='").append(x).append("' y='").append(y - 10).append("' font-size='10' font-weight='bold' text-anchor='middle' fill='#333333'>₹").append(trendInts[i]).append("</text>\n");
            svgTexts.append("<text x='").append(x).append("' y='").append(svgHeight - 15).append("' font-size='11' text-anchor='middle' fill='#666666'>Day ").append(i+1).append("</text>\n");
        }

        StringBuilder svgYAxisTicks = new StringBuilder();
        int steps = 4;
        for (int i = 0; i <= steps; i++) {
            float ratio = (float) i / steps;
            int tickVal = minPriceVal + (int) (ratio * (maxPriceVal - minPriceVal));
            float y = svgHeight - padBottom - (ratio * chartH);
            svgYAxisTicks.append("<text x='").append(padLeft - 15).append("' y='").append(y + 4).append("' font-size='11' text-anchor='end' fill='#666666'>₹").append(tickVal).append("</text>\n");
            svgYAxisTicks.append("<line x1='").append(padLeft).append("' y1='").append(y).append("' x2='").append(svgWidth - padRight).append("' y2='").append(y).append("' stroke='#E0DCCF' stroke-width='1' stroke-dasharray='4' />\n");
        }

        String svgChartHtml = "<svg width='100%' height='200' viewBox='0 0 600 200' style='background-color:#F9F7EE; border:1px solid #E0DCCF; border-radius:6px; margin: 15px 0;'>\n" +
                "  <!-- Grid lines & Ticks -->\n" +
                svgYAxisTicks.toString() +
                "  <!-- Axis lines -->\n" +
                "  <line x1='" + padLeft + "' y1='" + padTop + "' x2='" + padLeft + "' y2='" + (svgHeight - padBottom) + "' stroke='#CCCCCC' stroke-width='1.5' />\n" +
                "  <line x1='" + padLeft + "' y1='" + (svgHeight - padBottom) + "' x2='" + (svgWidth - padRight) + "' y2='" + (svgHeight - padBottom) + "' stroke='#CCCCCC' stroke-width='1.5' />\n" +
                "  <!-- Polyline -->\n" +
                "  <polyline points='" + polylinePoints.toString().trim() + "' fill='none' stroke='#4A5C2D' stroke-width='3' stroke-linecap='round' stroke-linejoin='round' />\n" +
                "  <!-- Data Circles -->\n" +
                svgCircles.toString() +
                "  <!-- Texts labels -->\n" +
                svgTexts.toString() +
                "</svg>\n";

        String displayDate = new java.text.SimpleDateFormat("dd-MM-yyyy", java.util.Locale.getDefault()).format(new java.util.Date());

        String htmlContent = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "<meta charset='utf-8'>\n" +
                "<style>\n" +
                "  body { font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; margin: 24px; color: #333333; background: #ffffff; }\n" +
                "  .header { display: flex; justify-content: space-between; align-items: flex-end; border-bottom: 3px solid #1B5E20; padding-bottom: 10px; margin-bottom: 20px; }\n" +
                "  .header h1 { margin: 0; font-size: 26px; color: #1B5E20; font-weight: bold; }\n" +
                "  .header .subtitle { margin-top: 5px; font-size: 13px; color: #666666; font-weight: bold; text-transform: uppercase; letter-spacing: 1px; }\n" +
                "  .header .date { font-size: 13px; color: #333333; }\n" +
                "  \n" +
                "  .meta-box { background: #F5F5F5; border: 1px solid #E0E0E0; border-radius: 6px; padding: 12px; margin-bottom: 20px; display: table; width: 100%; box-sizing: border-box; }\n" +
                "  .meta-col { display: table-cell; width: 33.33%; font-size: 13px; vertical-align: top; }\n" +
                "  .meta-col strong { color: #1B5E20; display: block; margin-bottom: 4px; }\n" +
                "  \n" +
                "  table { width: 100%; border-collapse: collapse; margin-bottom: 25px; }\n" +
                "  th { background-color: #1B5E20; color: #ffffff; text-align: left; padding: 10px; font-size: 13px; width: 50%; }\n" +
                "  td { padding: 10px; border: 1px solid #E0E0E0; font-size: 13px; vertical-align: top; }\n" +
                "  tr:nth-child(even) { background-color: #FAFAFA; }\n" +
                "  \n" +
                "  .trend-section { margin-bottom: 25px; padding: 15px; background-color: #FAF8F0; border: 1px solid #E0DCCF; border-radius: 8px; }\n" +
                "  .trend-title { font-size: 14px; font-weight: bold; color: #1B5E20; margin-bottom: 8px; }\n" +
                "  .trend-text { font-size: 12.5px; line-height: 1.5; color: #444444; margin-bottom: 4px; }\n" +
                "  \n" +
                "  .advisory-section { display: table; width: 100%; margin-top: 15px; border-top: 1px dashed #CCCCCC; padding-top: 15px; }\n" +
                "  .advisory-col { display: table-cell; width: 50%; vertical-align: top; box-sizing: border-box; }\n" +
                "  .advisory-col:first-child { padding-right: 15px; }\n" +
                "  .advisory-col:last-child { padding-left: 15px; }\n" +
                "  .advisory-col h3 { font-size: 14px; color: #1B5E20; margin-top: 0; margin-bottom: 10px; border-bottom: 1px solid #E0E0E0; padding-bottom: 4px; }\n" +
                "  .advisory-col ul { margin: 0; padding-left: 18px; }\n" +
                "  .advisory-col li { font-size: 12.5px; line-height: 1.6; margin-bottom: 8px; color: #333333; }\n" +
                "  \n" +
                "  .footer { text-align: center; font-size: 11px; color: #888888; margin-top: 35px; border-top: 1px solid #EEEEEE; padding-top: 10px; }\n" +
                "</style>\n" +
                "</head>\n" +
                "<body>\n" +
                "\n" +
                "<div class='header'>\n" +
                "  <div>\n" +
                "    <h1>" + LanguageManager.getDual(this, "Jai Kisan", "jai_kisan") + "</h1>\n" +
                "    <div class='subtitle'>" + LanguageManager.getDual(this, "Mandi Market Insights", "mandi_report") + "</div>\n" +
                "  </div>\n" +
                "  <div class='date'>Date: " + displayDate + "</div>\n" +
                "</div>\n" +
                "\n" +
                "<div class='meta-box'>\n" +
                "  <div class='meta-col'>\n" +
                "    <strong>" + LanguageManager.getDual(this, "Crop", "crop") + ":</strong>\n" +
                "    " + crop + "\n" +
                "  </div>\n" +
                "  <div class='meta-col'>\n" +
                "    <strong>Variety:</strong>\n" +
                "    " + (variety.isEmpty() ? "General" : variety) + "\n" +
                "  </div>\n" +
                "  <div class='meta-col'>\n" +
                "    <strong>" + LanguageManager.getDual(this, "Location", "state") + ":</strong>\n" +
                "    " + city + ", " + state + "\n" +
                "  </div>\n" +
                "</div>\n" +
                "\n" +
                "<table>\n" +
                "  <thead>\n" +
                "    <tr>\n" +
                "      <th>" + (LanguageManager.isEnglishOnly(this) ? "Market Parameter" : "Market Parameter (English)") + "</th>\n" +
                "      <th>" + LanguageManager.getDual(this, "Market Details", "mandi_report") + "</th>\n" +
                "    </tr>\n" +
                "  </thead>\n" +
                "  <tbody>\n" +
                "    <tr>\n" +
                "      <td><strong>Modal Price:</strong> " + pdfModalEn + "</td>\n" +
                "      <td><strong>" + LanguageManager.getDual(this, "Modal Price", "price") + ":</strong> " + (LanguageManager.isEnglishOnly(this) ? pdfModalEn : pdfModalHi) + "</td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <td><strong>Price Range:</strong> " + pdfRangeEn + "</td>\n" +
                "      <td><strong>" + LanguageManager.getDual(this, "Price Range", "price") + ":</strong> " + (LanguageManager.isEnglishOnly(this) ? pdfRangeEn : pdfRangeHi) + "</td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <td><strong>Trend:</strong> " + trendEn + "</td>\n" +
                "      <td><strong>" + LanguageManager.getDual(this, "Market Trend", "price") + ":</strong> " + (LanguageManager.isEnglishOnly(this) ? trendEn : trendHi) + "</td>\n" +
                "    </tr>\n" +
                "  </tbody>\n" +
                "</table>\n" +
                "\n" +
                "<div class='trend-section'>\n" +
                "  <div class='trend-title'>" + LanguageManager.getDual(this, "7-Day Price Trend", "price") + "</div>\n" +
                "  " + svgChartHtml + "\n" +
                "  <div class='trend-text'>" + trendDescEn + "</div>\n" +
                "  " + (LanguageManager.isEnglishOnly(this) ? "" : "<div class='trend-text' style='margin-top: 6px; font-style: italic; color: #555555;'>" + trendDescHi + "</div>\n") +
                "</div>\n" +
                "\n" +
                "<div class='advisory-section'>\n" +
                "  <div class='advisory-col'>\n" +
                "    <h3>Advisory & Environment</h3>\n" +
                "    <ul>\n" +
                "      <li><strong>Weather Impact:</strong> " + weatherEn + "</li>\n" +
                "      <li><strong>Farmer Advice:</strong> " + adviceEn + "</li>\n" +
                "    </ul>\n" +
                "  </div>\n" +
                "  <div class='advisory-col'>\n" +
                "    <h3>" + LanguageManager.getDual(this, "Advisory & Weather", "ai_advisory") + "</h3>\n" +
                "    <ul>\n" +
                "      <li><strong>" + LanguageManager.getDual(this, "Weather Impact", "home_weather_info") + ":</strong> " + (LanguageManager.isEnglishOnly(this) ? weatherEn : weatherHi) + "</li>\n" +
                "      <li><strong>" + LanguageManager.getDual(this, "Farmer Advice", "expert_tip") + ":</strong> " + (LanguageManager.isEnglishOnly(this) ? adviceEn : adviceHi) + "</li>\n" +
                "    </ul>\n" +
                "  </div>\n" +
                "</div>\n" +
                "\n" +
                "<div class='footer'>\n" +
                "  " + LanguageManager.getRegional(this, "generated_via", "Generated via Jai Kisan App &bull; Supporting Indian Agriculture") + "\n" +
                "</div>\n" +
                "\n" +
                "</body>\n</html>";

        HtmlToPdfManager.generatePdfFromHtml(this, htmlContent, "Mandi_Price_Report");
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