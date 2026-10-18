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

    private com.google.android.material.textfield.TextInputLayout layoutState, layoutCity, layoutCrop, layoutVariety, layoutSortBy, layoutSortOrder;
    private TextView tvNoteOptionalDate, tvTitleModalPrice, tvTitleMarketTrend, tvTitlePriceRange, tvTitle7DayTrend;
    private TextView tvTitleWeatherImpact, tvTitleFarmerAdvisory, tvTitleDisclaimer, tvDisclaimerHi;
    private MaterialButton btnSharePdf;
    private MaterialToolbar toolbar;
    private String[] currentSortFieldDisplay;

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

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        layoutState = findViewById(R.id.layout_state);
        layoutCity = findViewById(R.id.layout_city);
        layoutCrop = findViewById(R.id.layout_crop);
        layoutVariety = findViewById(R.id.layout_variety);
        layoutSortBy = findViewById(R.id.layout_sort_by);
        layoutSortOrder = findViewById(R.id.layout_sort_order);

        tvNoteOptionalDate = findViewById(R.id.tv_note_optional_date);
        tvTitleModalPrice = findViewById(R.id.tv_title_modal_price);
        tvTitleMarketTrend = findViewById(R.id.tv_title_market_trend);
        tvTitlePriceRange = findViewById(R.id.tv_title_price_range);
        tvTitle7DayTrend = findViewById(R.id.tv_title_7day_trend);
        tvTitleWeatherImpact = findViewById(R.id.tv_title_weather_impact);
        tvTitleFarmerAdvisory = findViewById(R.id.tv_title_farmer_advisory);
        tvTitleDisclaimer = findViewById(R.id.tv_title_disclaimer);
        tvDisclaimerHi = findViewById(R.id.tv_disclaimer_hi);

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

        // Sort Config
        String[] sortFields = { "Date", "Market", "Modal Price" };
        selectedSortField = sortFields[0];

        spinnerSortField.setOnItemClickListener((parent, view, position, id) -> {
            selectedSortField = sortFields[position];
        });

        String[] sortOrders = { "ASC", "DESC" };
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
            String[] varieties = LanguageManager.getLocalizedVarieties(MandiPricesActivity.this, selectedCrop);
            if (varieties != null && varieties.length > 0) {
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

        btnReset.setOnClickListener(v -> resetFields());

        btnGetPrices.setOnClickListener(v -> {
            String state = spinnerState.getText().toString();
            String city = spinnerCity.getText().toString();
            String crop = spinnerCrop.getText().toString();
            String variety = spinnerVariety.getText().toString();

            if (state.isEmpty() || city.isEmpty() || crop.isEmpty()) {
                Toast.makeText(this, LanguageManager.getDual(this, "Please select all required fields.", "please_fill_all"),
                        Toast.LENGTH_LONG).show();
                return;
            }
            fetchPrices(state, city, crop, variety);
        });

        // Share button — shows M3 bottom sheet with Text / PDF options
        btnSharePdf = findViewById(R.id.btnSharePdf);
        if (btnSharePdf != null) {
            btnSharePdf.setOnClickListener(v -> {
                if (cardResult.getVisibility() != View.VISIBLE) {
                    Toast.makeText(this, LanguageManager.getDual(this, "Please fetch prices first.", "get_prices"), Toast.LENGTH_SHORT)
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

        localizeUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        localizeUI();
    }

    private void localizeUI() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(LanguageManager.getDual(this, "Mandi Prices", "mandi_prices"));
        }
        if (layoutState != null) layoutState.setHint(LanguageManager.getDual(this, "State", "state") + " *");
        if (layoutCity != null) layoutCity.setHint(LanguageManager.getDual(this, "District", "city") + " *");
        if (layoutCrop != null) layoutCrop.setHint(LanguageManager.getDual(this, "Commodity", "crop"));
        if (layoutVariety != null) layoutVariety.setHint(LanguageManager.getDual(this, "Variety (Optional)", "variety_optional"));
        if (layoutSortBy != null) layoutSortBy.setHint(LanguageManager.getDual(this, "Sort By", "sort_by"));
        if (layoutSortOrder != null) layoutSortOrder.setHint(LanguageManager.getDual(this, "Order", "order"));

        if (btnDateFrom != null && btnDateFrom.getTag() == null) {
            btnDateFrom.setText(LanguageManager.getDual(this, "From", "from"));
        }
        if (btnDateTo != null && btnDateTo.getTag() == null) {
            btnDateTo.setText(LanguageManager.getDual(this, "To", "to"));
        }
        if (tvNoteOptionalDate != null) {
            tvNoteOptionalDate.setText(LanguageManager.getDual(this, "Note: Date selection is optional.", "note_optional_date"));
        }
        if (btnReset != null) {
            btnReset.setText(LanguageManager.getDual(this, "Reset", "reset"));
        }
        if (btnGetPrices != null) {
            btnGetPrices.setText(LanguageManager.getDual(this, "Preview", "preview"));
        }

        if (tvTitleModalPrice != null) tvTitleModalPrice.setText(LanguageManager.getDual(this, "Modal Price", "modal_price"));
        if (tvTitleMarketTrend != null) tvTitleMarketTrend.setText(LanguageManager.getDual(this, "Market Trend", "market_trend"));
        if (tvTitlePriceRange != null) tvTitlePriceRange.setText(LanguageManager.getDual(this, "Estimated Price Range", "price_range"));
        if (tvTitle7DayTrend != null) tvTitle7DayTrend.setText(LanguageManager.getDual(this, "7-Day Price Trend", "trend_7day_title"));
        if (tvTitleWeatherImpact != null) tvTitleWeatherImpact.setText("⚠ " + LanguageManager.getDual(this, "Weather Impact", "weather_impact_title"));
        if (tvTitleFarmerAdvisory != null) tvTitleFarmerAdvisory.setText("💡 " + LanguageManager.getDual(this, "Farmer Advisory", "farmer_advisory_title"));
        if (btnSharePdf != null) btnSharePdf.setText(LanguageManager.getDual(this, "Share Report", "share_report"));
        if (tvTitleDisclaimer != null) tvTitleDisclaimer.setText("⚠ " + LanguageManager.getDual(this, "Disclaimer", "disclaimer_title"));

        if (tvDisclaimerHi != null) {
            if (LanguageManager.isEnglishOnly(this)) {
                tvDisclaimerHi.setVisibility(View.GONE);
            } else {
                tvDisclaimerHi.setVisibility(View.VISIBLE);
                tvDisclaimerHi.setText(LanguageManager.getRegional(this, "disclaimer_body", "यह रिपोर्ट AI आधारित विश्लेषण और अनुमानित बाजार प्रवृत्तियों पर आधारित है। वास्तविक मंडी कीमतें स्थान और बाजार की स्थिति के अनुसार भिन्न हो सकती हैं।"));
            }
        }

        // Setup States
        String[] states = LanguageManager.getLocalizedStates(this);
        spinnerState.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, states));

        // Setup Crops
        String[] crops = LanguageManager.getLocalizedCrops(this);
        spinnerCrop.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, crops));

        // Setup Sort Display
        currentSortFieldDisplay = new String[]{
                LanguageManager.getDual(this, "Date", "date"),
                LanguageManager.getDual(this, "Market", "market"),
                LanguageManager.getDual(this, "Price", "price")
        };
        spinnerSortField.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, currentSortFieldDisplay));
        if (spinnerSortField.getText().toString().isEmpty()) {
            spinnerSortField.setText(currentSortFieldDisplay[0], false);
        }

        String[] sortOrderDisplay = {
                LanguageManager.getDual(this, "Ascending", "ascending"),
                LanguageManager.getDual(this, "Descending", "descending")
        };
        spinnerSortOrder.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, sortOrderDisplay));
        if (spinnerSortOrder.getText().toString().isEmpty()) {
            spinnerSortOrder.setText(sortOrderDisplay[0], false);
        }
    }

    private void resetFields() {
        spinnerState.setText("", false);
        spinnerCity.setAdapter(null);
        spinnerCity.setText("", false);
        spinnerCrop.setText("", false);
        spinnerVariety.setText("", false);
        if (currentSortFieldDisplay != null && currentSortFieldDisplay.length > 0) {
            spinnerSortField.setText(currentSortFieldDisplay[0], false);
        }
        btnDateFrom.setText(LanguageManager.getDual(this, "From", "from"));
        btnDateTo.setText(LanguageManager.getDual(this, "To", "to"));
        btnDateFrom.setTag(null);
        btnDateTo.setTag(null);
        tvPriceResults.setText(LanguageManager.getDual(this, "Market data will be shown here.", "market_insight"));
        cardResult.setVisibility(View.VISIBLE);
        loadingIndicator.setVisibility(View.GONE);
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
            tvPriceResults.setText(LanguageManager.getDual(this, "Generating Market Insights...", "thinking"));
        });

        // Directly call AI API
        fetchFromGeminiApi(state, city, crop, variety, 0, 0);
    }

    private void fetchFromGeminiApi(String state, String city, String crop, String variety, int modelIndex, int keyRetryCount) {
        if (modelIndex >= MODELS.length) {
            runOnUiThread(() -> handleError(
                    LanguageManager.getDual(this, "All models are currently busy. Please try again in a minute.", "all_models_busy")));
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

        LanguageManager.LangItem langItem = LanguageManager.getSelectedLanguageItem(this);
        boolean isEngOnly = LanguageManager.isEnglishOnly(this);
        String targetLang = isEngOnly ? "English" : langItem.englishName + " (" + langItem.nativeName + ")";

        String prompt;
        if (isEngOnly) {
            prompt = "Generate a concise mandi market insight and 7-day price trend for farmers in English.\n\n" +
                    "Crop: " + crop + "\n" +
                    "Variety: " + (variety.isEmpty() ? "General" : variety) + "\n" +
                    "Location: " + city + ", " + state + "\n" +
                    "Date range filter: " + dateInfo + "\n" +
                    "Sort filter: " + sortInfo + "\n\n" +
                    "Rules:\n" +
                    "- Keep response short.\n" +
                    "- Use bullet points.\n" +
                    "- English only.\n\n" +
                    "Format exactly like this:\n\n" +
                    "## Market Insight\n" +
                    "- Price Range: ₹MIN–₹MAX per quintal\n" +
                    "- Modal Price: ₹NUMBER_ONLY (example: ₹1500). Write ONLY rupee symbol and the number.\n" +
                    "- Trend: rising / stable / falling\n" +
                    "- 7-Day Prices: 1150,1400,1590,1650,1800,1920,2110\n" +
                    "- 7-Day Description: short description including date/sort preferences\n" +
                    "- Weather Impact: short line\n" +
                    "- Farmer Advice: short practical tip\n\n" +
                    "Keep it concise, clear, and farmer friendly.";
        } else {
            prompt = "Generate a short bilingual mandi market insight and a 7-day price trend for farmers.\n\n" +
                    "Crop: " + crop + "\n" +
                    "Variety: " + (variety.isEmpty() ? "General" : variety) + "\n" +
                    "Location: " + city + ", " + state + "\n" +
                    "Date range filter: " + dateInfo + "\n" +
                    "Sort filter: " + sortInfo + "\n\n" +
                    "Rules:\n" +
                    "- Keep response short.\n" +
                    "- Use bullet points.\n" +
                    "- Section 1 must be in English.\n" +
                    "- Section 2 must be translated into " + targetLang + " (do NOT use Hindi unless selected language is Hindi).\n" +
                    "- Do NOT mix languages in same section.\n" +
                    "- Make sure to mention the selected date range and sorting filters clearly in the results.\n\n" +
                    "Format exactly like this:\n\n" +
                    "## Market Insight (English)\n" +
                    "- Price Range: ₹MIN–₹MAX per quintal\n" +
                    "- Modal Price: ₹NUMBER_ONLY (example: ₹1500). Write ONLY rupee symbol and the number. No text, no unit, no explanation.\n" +
                    "- Trend: rising / stable / falling\n" +
                    "- 7-Day Prices: 1150,1400,1590,1650,1800,1920,2110\n" +
                    "- 7-Day Description: short description including date/sort preferences\n" +
                    "- Weather Impact: short line\n" +
                    "- Farmer Advice: short practical tip\n\n" +
                    "## Market Insight (" + langItem.englishName + ")\n" +
                    "- Price Range: ₹MIN–₹MAX per quintal translated in " + targetLang + "\n" +
                    "- Modal Price: ₹NUMBER_ONLY (example: ₹1500). Write ONLY rupee symbol and the number. No text, no unit, no explanation.\n" +
                    "- Trend: rising / stable / falling translated in " + targetLang + "\n" +
                    "- 7-Day Prices: 1150,1400,1590,1650,1800,1920,2110\n" +
                    "- 7-Day Description: short description in " + targetLang + " including date/sort preferences\n" +
                    "- Weather Impact: short line in " + targetLang + "\n" +
                    "- Farmer Advice: short practical tip in " + targetLang + "\n\n" +
                    "Keep it concise, clear, and farmer friendly.";
        }

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
                            handleError(LanguageManager.getDual(MandiPricesActivity.this, "Service temporarily unavailable.", "service_unavailable"));
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
                        handleError(LanguageManager.getDual(MandiPricesActivity.this, "Error processing data.", "service_unavailable"));
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
        int sectionCount = 0;

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;
            if (trimmed.startsWith("##") || trimmed.startsWith("#")) {
                sectionCount++;
                continue;
            }

            if (trimmed.startsWith("-")) {
                String content = trimmed.substring(1).trim();
                int colonIdx = content.indexOf(":");
                if (colonIdx != -1) {
                    String label = content.substring(0, colonIdx).trim().toLowerCase();
                    String value = content.substring(colonIdx + 1).trim();
                    if (sectionCount <= 1) {
                        if (label.contains("price range") || label.contains("range")) {
                            priceRangeEn = value;
                        } else if (label.contains("modal price") || label.contains("modal")) {
                            modalPriceEn = value;
                        } else if (label.contains("trend")) {
                            trendEn = value;
                        } else if (label.contains("prices")) {
                            trendPricesEn = value;
                        } else if (label.contains("description")) {
                            trendDescEn = value;
                        } else if (label.contains("weather")) {
                            weatherEn = value;
                        } else if (label.contains("advice") || label.contains("advisory")) {
                            adviceEn = value;
                        }
                    } else {
                        if (label.contains("price range") || label.contains("range") || label.contains("मूल्य") || label.contains("सीमा") || label.contains("দর") || label.contains("శ్రేణి") || label.contains("வரம்பு")) {
                            priceRangeHi = value;
                        } else if (label.contains("modal price") || label.contains("modal") || label.contains("सामान्य") || label.contains("मूल्य") || label.contains("ధర") || label.contains("விலை") || label.contains("দর")) {
                            modalPriceHi = value;
                        } else if (label.contains("trend") || label.contains("प्रवृत्ति") || label.contains("रुख") || label.contains("ధోరణి") || label.contains("போக்கு") || label.contains("প্রবণতা")) {
                            trendHi = value;
                        } else if (label.contains("prices")) {
                            trendPricesHi = value;
                        } else if (label.contains("description") || label.contains("विवरण") || label.contains("விவரம்") || label.contains("వివరణ") || label.contains("বিবরণ")) {
                            trendDescHi = value;
                        } else if (label.contains("weather") || label.contains("मौसम") || label.contains("வானிலை") || label.contains("వాతావరణం") || label.contains("আবহাওয়া")) {
                            weatherHi = value;
                        } else if (label.contains("advice") || label.contains("advisory") || label.contains("सलाह") || label.contains("ఆలోచన") || label.contains("ஆலோசனை") || label.contains("পরামর্শ")) {
                            adviceHi = value;
                        }
                    }
                }
            }
        }

        // 1. Modal Price – extract ONLY the number (₹1500 or just 1500)
        long modalPriceNumPerQuintal = 0;
        String modalPriceVal = "--";
        String modalPriceKgVal = "--";

        java.util.regex.Pattern numOnlyPattern = java.util.regex.Pattern.compile("[₹\\s]*([\\d,]+)");
        java.util.regex.Matcher numMatcher = numOnlyPattern.matcher(modalPriceEn);
        if (numMatcher.find()) {
            String digits = numMatcher.group(1).replaceAll(",", "").trim();
            try {
                modalPriceNumPerQuintal = Long.parseLong(digits);
            } catch (NumberFormatException ignored) {}
        }

        if (modalPriceNumPerQuintal > 0) {
            modalPriceVal = "₹" + String.format("%,d", modalPriceNumPerQuintal);
            double perKg = modalPriceNumPerQuintal / 100.0;
            if (perKg == Math.floor(perKg)) {
                modalPriceKgVal = "₹" + String.format("%,d", (long) perKg);
            } else {
                modalPriceKgVal = "₹" + String.format("%.2f", perKg);
            }
        } else if (!modalPriceEn.isEmpty()) {
            modalPriceVal = modalPriceEn.replaceAll("(?i)(per\\s*(quintal|kg|q|kilogram)|inr|rs\\.?|rupees?|representative\\s*price|एक.*|a\\s+\\w.*)", "").trim();
            if (!modalPriceVal.startsWith("₹")) modalPriceVal = "₹" + modalPriceVal;
            modalPriceKgVal = "--";
        }

        boolean isEngOnly = LanguageManager.isEnglishOnly(this);

        String qReg = LanguageManager.getRegional(this, "quintal_suffix", "क्विंटल");
        String kgReg = LanguageManager.getRegional(this, "kg_suffix", "किग्रा");
        if (qReg.startsWith("/")) qReg = qReg.substring(1).trim();
        if (kgReg.startsWith("/")) kgReg = kgReg.substring(1).trim();

        String qSuffix = isEngOnly ? "/quintal" : "/quintal (" + qReg + ")";
        String kgSuffix = isEngOnly ? "/kg" : "/kg (" + kgReg + ")";

        if (tvUiModalPrice != null) {
            tvUiModalPrice.setText(modalPriceVal);
        }
        if (tvUiModalPriceSuffix != null) {
            tvUiModalPriceSuffix.setText(" " + qSuffix);
        }
        if (tvUiModalPriceKg != null) {
            tvUiModalPriceKg.setText(modalPriceKgVal);
        }
        if (tvUiModalPriceKgSuffix != null) {
            tvUiModalPriceKgSuffix.setText(" " + kgSuffix);
        }

        // Hide unstyled fallback description views to keep result card clean & organized
        if (tvUiDescriptionEn != null) {
            tvUiDescriptionEn.setVisibility(View.GONE);
        }
        if (tvUiDescriptionHi != null) {
            tvUiDescriptionHi.setVisibility(View.GONE);
        }

        // 2. Trend Pill Badge
        if (tvUiTrend != null) {
            String combinedTrend = (trendEn + " " + trendHi).toLowerCase();
            String arrowSymbol = "→ ";
            int trendColor = 0xFF155724; // Dark green
            int trendBgTint = 0xFFD4EDDA; // Light green

            if (combinedTrend.contains("rising") || combinedTrend.contains("up") || combinedTrend.contains("high") || combinedTrend.contains("बढ़त") || combinedTrend.contains("పెరు") || combinedTrend.contains("വർദ്ധി")) {
                arrowSymbol = "↑ ";
                trendColor = 0xFF155724;
                trendBgTint = 0xFFD4EDDA;
            } else if (combinedTrend.contains("falling") || combinedTrend.contains("down") || combinedTrend.contains("low") || combinedTrend.contains("गिरावट") || combinedTrend.contains("తగ్గు") || combinedTrend.contains("കുറ")) {
                arrowSymbol = "↓ ";
                trendColor = 0xFF721C24; // Dark red
                trendBgTint = 0xFFF8D7DA; // Light red
            } else {
                arrowSymbol = "→ ";
                trendColor = 0xFF155724;
                trendBgTint = 0xFFD4EDDA;
            }

            String trendEnClean = trendEn.trim();
            if (!trendEnClean.isEmpty()) {
                trendEnClean = Character.toUpperCase(trendEnClean.charAt(0)) + (trendEnClean.length() > 1 ? trendEnClean.substring(1) : "");
            }
            String trendHiClean = trendHi.trim();

            String displayTrend;
            if (isEngOnly || trendHiClean.isEmpty() || trendHiClean.equalsIgnoreCase(trendEnClean)) {
                displayTrend = arrowSymbol + trendEnClean;
            } else {
                displayTrend = arrowSymbol + trendEnClean + "\n" + trendHiClean;
            }
            tvUiTrend.setText(displayTrend);
            tvUiTrend.setTextColor(trendColor);
            tvUiTrend.setBackgroundTintList(android.content.res.ColorStateList.valueOf(trendBgTint));
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
            if (isEngOnly || trendDescHi.isEmpty()) {
                tvUiTrendDescHi.setVisibility(View.GONE);
            } else {
                tvUiTrendDescHi.setVisibility(View.VISIBLE);
                tvUiTrendDescHi.setText(trendDescHi);
            }
        }

        // 4. Price Range
        java.util.regex.Pattern valPattern = java.util.regex.Pattern.compile("(₹\\s*\\d+(?:,\\d+)*(?:\\s*(?:-|–)\\s*₹?\\s*\\d+(?:,\\d+)*)?)(.*)");
        String priceRangeVal = "";
        java.util.regex.Matcher mRange = valPattern.matcher(priceRangeEn);
        if (mRange.find()) {
            priceRangeVal = mRange.group(1).trim();
        } else {
            java.util.regex.Pattern fallbackRangePattern = java.util.regex.Pattern.compile("(₹?\\s*\\d+(?:,\\d+)*(?:\\s*(?:-|–)\\s*₹?\\s*\\d+(?:,\\d+)*)?)");
            java.util.regex.Matcher mRangeFallback = fallbackRangePattern.matcher(priceRangeEn);
            if (mRangeFallback.find()) {
                priceRangeVal = mRangeFallback.group(1).trim();
                if (!priceRangeVal.startsWith("₹")) {
                    priceRangeVal = "₹" + priceRangeVal;
                }
            } else {
                priceRangeVal = priceRangeEn;
            }
        }

        if (tvUiPriceRange != null) {
            tvUiPriceRange.setText(priceRangeVal);
        }
        if (tvUiPriceRangeSuffix != null) {
            tvUiPriceRangeSuffix.setText(isEngOnly ? " / Q" : " / Q (" + qReg + ")");
        }

        // 5. Weather Impact Card
        if (tvUiWeatherImpactEn != null) {
            tvUiWeatherImpactEn.setText(weatherEn);
        }
        if (tvUiWeatherImpactHi != null) {
            if (isEngOnly || weatherHi.isEmpty()) {
                tvUiWeatherImpactHi.setVisibility(View.GONE);
            } else {
                tvUiWeatherImpactHi.setVisibility(View.VISIBLE);
                tvUiWeatherImpactHi.setText(weatherHi);
            }
        }

        // 6. Farmer Advisory Card
        if (tvUiFarmerAdviceEn != null) {
            tvUiFarmerAdviceEn.setText(adviceEn);
        }
        if (tvUiFarmerAdviceHi != null) {
            if (isEngOnly || adviceHi.isEmpty()) {
                tvUiFarmerAdviceHi.setVisibility(View.GONE);
            } else {
                tvUiFarmerAdviceHi.setVisibility(View.VISIBLE);
                tvUiFarmerAdviceHi.setText(adviceHi);
            }
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
        sb.append("*").append(LanguageManager.getDual(this, "Jai Kisan - Mandi Price Report", "mandi_report")).append("*\n");
        sb.append("----------------------------------\n");
        if (!crop.isEmpty())
            sb.append(LanguageManager.getDual(this, "Crop", "crop")).append(": ").append(crop).append("\n");
        if (!variety.isEmpty())
            sb.append(LanguageManager.getDual(this, "Variety", "variety")).append(": ").append(variety).append("\n");
        if (!city.isEmpty())
            sb.append(LanguageManager.getDual(this, "Market", "market")).append(": ").append(city).append(", ").append(state).append("\n\n");
        sb.append(resultText).append("\n\n");
        sb.append(LanguageManager.getRegional(this, "generated_via", "Generated via Jai Kisan App"));

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, sb.toString());
        startActivity(Intent.createChooser(shareIntent, LanguageManager.getDual(this, "Share via", "share_report")));
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
        int sectionCount = 0;

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;
            if (trimmed.startsWith("##") || trimmed.startsWith("#")) {
                sectionCount++;
                continue;
            }

            if (trimmed.startsWith("-")) {
                String content = trimmed.substring(1).trim();
                int colonIdx = content.indexOf(":");
                if (colonIdx != -1) {
                    String label = content.substring(0, colonIdx).trim().toLowerCase();
                    String value = content.substring(colonIdx + 1).trim();
                    if (sectionCount <= 1) {
                        if (label.contains("price range") || label.contains("range")) {
                            priceRangeEn = value;
                        } else if (label.contains("modal price") || label.contains("modal")) {
                            modalPriceEn = value;
                        } else if (label.contains("trend")) {
                            trendEn = value;
                        } else if (label.contains("prices")) {
                            trendPricesEn = value;
                        } else if (label.contains("description")) {
                            trendDescEn = value;
                        } else if (label.contains("weather")) {
                            weatherEn = value;
                        } else if (label.contains("advice") || label.contains("advisory")) {
                            adviceEn = value;
                        }
                    } else {
                        if (label.contains("price range") || label.contains("range") || label.contains("मूल्य") || label.contains("सीमा") || label.contains("দর") || label.contains("శ్రేణి") || label.contains("வரம்பு")) {
                            priceRangeHi = value;
                        } else if (label.contains("modal price") || label.contains("modal") || label.contains("सामान्य") || label.contains("मूल्य") || label.contains("ధర") || label.contains("விலை") || label.contains("দর")) {
                            modalPriceHi = value;
                        } else if (label.contains("trend") || label.contains("प्रवृत्ति") || label.contains("रुख") || label.contains("ధోరణి") || label.contains("போக்கு") || label.contains("প্রবণতা")) {
                            trendHi = value;
                        } else if (label.contains("prices")) {
                            trendPricesHi = value;
                        } else if (label.contains("description") || label.contains("विवरण") || label.contains("விவரம்") || label.contains("వివరణ") || label.contains("বিবরণ")) {
                            trendDescHi = value;
                        } else if (label.contains("weather") || label.contains("मौसम") || label.contains("வானிலை") || label.contains("వాతావరణం") || label.contains("আবহাওয়া")) {
                            weatherHi = value;
                        } else if (label.contains("advice") || label.contains("advisory") || label.contains("सलाह") || label.contains("ఆలోచన") || label.contains("ஆலோசனை") || label.contains("পরামর্শ")) {
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
            pdfModalEn = val + " /quintal";
            pdfModalHi = val + " " + LanguageManager.getRegional(this, "quintal_suffix", "/क्विंटल");
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
            pdfRangeEn = val + " /quintal";
            pdfRangeHi = val + " " + LanguageManager.getRegional(this, "quintal_suffix", "/क्विंटल");
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
                "  @page { size: A4; margin: 10mm 8mm; }\n" +
                "  body { font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; margin: 0; padding: 0; color: #333333; background: #ffffff; }\n" +
                "  h1, h2, h3, h4, .section-title { page-break-after: avoid; break-after: avoid; }\n" +
                "  .meta-box, table, .trend-section, .advisory-section, .pdf-disclaimer { page-break-inside: avoid; break-inside: avoid; }\n" +
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
                "  .footer { text-align: center; font-size: 11px; color: #888888; margin-top: 30px; border-top: 1px solid #EEEEEE; padding-top: 10px; }\n" +
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
                LanguageManager.getPdfDisclaimerHtml(this) +
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