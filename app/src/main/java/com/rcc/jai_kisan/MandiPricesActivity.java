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

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
    private ExecutorService executorService;
    private CircularProgressIndicator loadingIndicator;
    private MaterialCardView cardResult;

    private String selectedSortField;
    private String selectedSortOrder;

    private Map<String, String[]> cropVarietiesMap;

    final String GOV_API_KEY = "579b464db66ec23bdd000001b83385380c804b697dd2203bf51007bc";
    private static final String GEMINI_API_KEY = BuildConfig.GEMINI_API_KEY;

    // Using Gemma 3 4B for unlimited free usage
    private static final String GEMINI_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemma-3-4b-it:generateContent?key="
                    + GEMINI_API_KEY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mandi_prices);

        markwon = Markwon.create(this);
        executorService = Executors.newSingleThreadExecutor();

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Mandi Prices / मंडी भाव");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
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

        ArrayAdapter<CharSequence> stateAdapter = ArrayAdapter.createFromResource(this, R.array.states_array, android.R.layout.simple_dropdown_item_1line);
        spinnerState.setAdapter(stateAdapter);

        String[] crops = {
                "Wheat / गेहूं", "Rice / चावल", "Onion / प्याज", "Potato / आलू",
                "Tomato / टमाटर", "Maize / मक्का", "Paddy / धान", "Mustard / सरसों",
                "Soyabean / सोयाबीन", "Cotton / कपास"
        };
        ArrayAdapter<String> cropAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, crops);
        spinnerCrop.setAdapter(cropAdapter);

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
            spinnerCity.setAdapter(null);

            String englishStateName = selectedState.split("/")[0].trim();

            String arrayName = englishStateName.toLowerCase()
                    .replace(" & ", "_").replace(" and ", "_")
                    .replace(" ", "_").replace("-", "_") + "_cities";
            int citiesArrayId = getResources().getIdentifier(arrayName, "array", getPackageName());
            if (citiesArrayId != 0) {
                ArrayAdapter<CharSequence> cityAdapter = ArrayAdapter.createFromResource(MandiPricesActivity.this, citiesArrayId, android.R.layout.simple_dropdown_item_1line);
                spinnerCity.setAdapter(cityAdapter);
            } else {
                Log.e("CITY_SPINNER", "No city array found for: " + arrayName);
                Toast.makeText(this, "No districts found for " + selectedState, Toast.LENGTH_SHORT).show();
            }
        });

        spinnerCrop.setOnItemClickListener((parent, view, position, id) -> {
            String selectedCropFull = (String) parent.getItemAtPosition(position);
            String englishCropName = selectedCropFull.split("/")[0].trim();

            spinnerVariety.setText("", false);
            if (cropVarietiesMap.containsKey(englishCropName)) {
                String[] varieties = cropVarietiesMap.get(englishCropName);
                ArrayAdapter<String> varietyAdapter = new ArrayAdapter<>(MandiPricesActivity.this, android.R.layout.simple_dropdown_item_1line, varieties);
                spinnerVariety.setAdapter(varietyAdapter);
            } else {
                String[] defaultVarieties = {"Other / अन्य", "Common / सामान्य"};
                ArrayAdapter<String> varietyAdapter = new ArrayAdapter<>(MandiPricesActivity.this, android.R.layout.simple_dropdown_item_1line, defaultVarieties);
                spinnerVariety.setAdapter(varietyAdapter);
            }
        });


        btnDateFrom.setOnClickListener(v -> showDatePicker(btnDateFrom));
        btnDateTo.setOnClickListener(v -> showDatePicker(btnDateTo));

        btnReset.setOnClickListener(v -> {
            spinnerState.setText(spinnerState.getAdapter().getItem(0).toString(), false);
            spinnerCity.setAdapter(null);
            spinnerCity.setText("", false);
            spinnerCrop.setText("", false);
            spinnerVariety.setText("", false);
            spinnerSortField.setText(sortFieldDisplay[0], false);
            spinnerSortOrder.setText(sortOrderDisplay[0], false);
            selectedSortField = sortFieldApi[0];
            selectedSortOrder = sortOrderApi[0];
            btnDateFrom.setText("From / से");
            btnDateTo.setText("To / तक");
            btnDateFrom.setTag(null);
            btnDateTo.setTag(null);
            tvPriceResults.setText("Prices will be shown here / यहाँ कीमतें दिखाई जाएँगी।");
            cardResult.setVisibility(View.VISIBLE);
            loadingIndicator.setVisibility(View.GONE);
        });

        btnGetPrices.setOnClickListener(v -> {
            String state = spinnerState.getText().toString();
            String city = spinnerCity.getText().toString();
            String crop = spinnerCrop.getText().toString();
            String variety = spinnerVariety.getText().toString();

            if (state.isEmpty() || city.isEmpty() || crop.isEmpty()) {
                Toast.makeText(this, "Please select State, City and Crop.\nकृपया राज्य, शहर और फसल चुनें।", Toast.LENGTH_LONG).show();
                return;
            }
            fetchPrices(state, city, crop, variety);
        });
    }

    private void initCropVarieties() {
        cropVarietiesMap = new HashMap<>();

        cropVarietiesMap.put("Wheat", new String[]{
                "Lokwan / लोकवन", "Sharbati / शरबती", "Durum / डुरम", "Deshi / देसी", "Other / अन्य"
        });
        cropVarietiesMap.put("Rice", new String[]{
                "Basmati / बासमती", "Sona Masoori / सोना मसूरी", "Kolam / कोलम", "IR 64 / आई आर 64", "Other / अन्य"
        });
        cropVarietiesMap.put("Onion", new String[]{
                "Red / लाल", "White / सफेद", "Pink / गुलाबी", "Nasik / नासिक", "Other / अन्य"
        });
        cropVarietiesMap.put("Potato", new String[]{
                "Jyoti / ज्योति", "Chandramukhi / चंद्रमुखी", "Sugar Free / शुगर फ्री", "Deshi / देसी", "Other / अन्य"
        });
        cropVarietiesMap.put("Tomato", new String[]{
                "Hybrid / हाइब्रिड", "Deshi / देसी", "Himsona / हिमसोना", "Other / अन्य"
        });
        cropVarietiesMap.put("Maize", new String[]{
                "Yellow / पीला", "White / सफेद", "Hybrid / हाइब्रिड", "Other / अन्य"
        });
        cropVarietiesMap.put("Paddy", new String[]{
                "Common / सामान्य", "Grade A / ग्रेड ए", "Basmati / बासमती", "Other / अन्य"
        });
        cropVarietiesMap.put("Mustard", new String[]{
                "Black / काली", "Yellow / पीली", "Hybrid / हाइब्रिड", "Other / अन्य"
        });
        cropVarietiesMap.put("Soyabean", new String[]{
                "Yellow / पीला", "Black / काला", "Other / अन्य"
        });
        cropVarietiesMap.put("Cotton", new String[]{
                "American / अमेरिकन", "Deshi / देसी", "Bt Cotton / बीटी कपास", "Other / अन्य"
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDatePicker(MaterialButton button) {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Date / तारीख चुनें")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            SimpleDateFormat apiSdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            apiSdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            String apiDateString = apiSdf.format(new Date(selection));
            button.setTag(apiDateString);

            SimpleDateFormat displaySdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String displayDate = displaySdf.format(new Date(selection));
            button.setText(displayDate);
        });

        datePicker.show(getSupportFragmentManager(), "date_picker");
    }

    private void fetchPrices(String state, String city, String crop, String variety) {
        runOnUiThread(() -> {
            loadingIndicator.setVisibility(View.VISIBLE);
            cardResult.setVisibility(View.GONE);
            btnGetPrices.setEnabled(false);
        });

        executorService.execute(() -> {
            String apiCity = city.split("/")[0].trim();
            String apiCrop = crop.split("/")[0].trim();
            String apiVariety = variety.isEmpty() ? "" : variety.split("/")[0].trim();

            String govResult = fetchFromGovApi(apiCity, apiCrop, apiVariety);

            if (govResult != null && !govResult.isEmpty()) {
                runOnUiThread(() -> {
                    markwon.setMarkdown(tvPriceResults, govResult);
                    loadingIndicator.setVisibility(View.GONE);
                    cardResult.setVisibility(View.VISIBLE);
                    btnGetPrices.setEnabled(true);
                });
            } else if (govResult != null) {
                runOnUiThread(() -> {
                    tvPriceResults.setText("Official data not found, getting expert AI report...\nआधिकारिक डेटा नहीं मिला, विशेषज्ञ AI रिपोर्ट प्राप्त की जा रही है...");
                    cardResult.setVisibility(View.VISIBLE);
                });
                fetchFromGeminiApi(state, city, crop, variety);
            } else {
                runOnUiThread(() -> {
                    tvPriceResults.setText("Official source unavailable, getting expert AI report...\nआधिकारिक स्रोत अनुपलब्ध है, विशेषज्ञ AI रिपोर्ट प्राप्त की जा रही है...");
                    cardResult.setVisibility(View.VISIBLE);
                });
                fetchFromGeminiApi(state, city, crop, variety);
            }
        });
    }

    private String fetchFromGovApi(String city, String crop, String variety) {
        HttpURLConnection conn = null;
        try {
            String fromDate = btnDateFrom.getTag() instanceof String ? (String) btnDateFrom.getTag() : "";
            String toDate = btnDateTo.getTag() instanceof String ? (String) btnDateTo.getTag() : "";

            StringBuilder queryBuilder = new StringBuilder("https://api.data.gov.in/resource/9ef84268-d588-465a-a308-a864a43d0070");
            queryBuilder.append("?api-key=").append(GOV_API_KEY).append("&format=json&limit=100");
            queryBuilder.append("&filters[district]=").append(java.net.URLEncoder.encode(city, "UTF-8"));
            queryBuilder.append("&filters[commodity]=").append(java.net.URLEncoder.encode(crop, "UTF-8"));

            if (!variety.isEmpty() && !variety.equalsIgnoreCase("Other") && !variety.equalsIgnoreCase("Common")) {
                queryBuilder.append("&filters[variety]=").append(java.net.URLEncoder.encode(variety, "UTF-8"));
            }

            if (!fromDate.isEmpty()) queryBuilder.append("&filters[arrival_date][from]=").append(fromDate);
            if (!toDate.isEmpty()) queryBuilder.append("&filters[arrival_date][to]=").append(toDate);

            if (selectedSortField != null && !selectedSortField.isEmpty() && selectedSortOrder != null && !selectedSortOrder.isEmpty()) {
                queryBuilder.append("&sort[").append(selectedSortField).append("]=").append(selectedSortOrder);
            }

            String queryUrl = queryBuilder.toString();
            Log.d("API_URL", queryUrl);
            conn = (HttpURLConnection) new URL(queryUrl).openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(15000);

            if (conn.getResponseCode() == 200) {
                InputStream is = conn.getInputStream();
                Scanner scanner = new Scanner(is).useDelimiter("\\A");
                String responseStr = scanner.hasNext() ? scanner.next() : "";

                if (responseStr.trim().isEmpty() || !responseStr.trim().startsWith("{")) {
                    Log.e("GOV_API_ERROR", "Received empty or invalid JSON response from server.");
                    return null;
                }

                JSONObject response = new JSONObject(responseStr);

                if (!response.has("records")) {
                    Log.e("GOV_API_ERROR", "API response does not contain 'records' key. Response: " + responseStr);
                    return null;
                }

                JSONArray records = response.getJSONArray("records");

                if (records.length() > 0) {
                    StringBuilder resultText = new StringBuilder();
                    for (int i = 0; i < records.length(); i++) {
                        JSONObject item = records.getJSONObject(i);
                        resultText.append("● **Date / तिथि:** ").append(item.optString("arrival_date", "N/A")).append("  \n")
                                .append("  **Market / मंडी:** ").append(item.optString("market", "N/A")).append("  \n")
                                .append("  **Variety / किस्म:** ").append(item.optString("variety", "N/A")).append("  \n")
                                .append("  **Min / न्यूनतम:** ₹").append(item.optString("min_price", "N/A")).append("  \n")
                                .append("  **Max / अधिकतम:** ₹").append(item.optString("max_price", "N/A")).append("  \n")
                                .append("  **Modal / औसत:** ₹").append(item.optString("modal_price", "N/A")).append("\n\n");
                    }
                    return resultText.toString();
                } else {
                    return "";
                }
            } else {
                Log.e("GOV_API_ERROR", "Server error code: " + conn.getResponseCode());
                return null;
            }
        } catch (Exception e) {
            Log.e("GOV_API_ERROR", "Network/JSON error", e);
            return null;
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    private void fetchFromGeminiApi(String state, String city, String crop, String variety) {
        // ✅ FIX: Increased Timeout to 180s to prevent "Check Internet" errors
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(180, TimeUnit.SECONDS)
                .readTimeout(180, TimeUnit.SECONDS)
                .build();
        String fromDateStr = btnDateFrom.getTag() instanceof String ? (String) btnDateFrom.getTag() : "today";
        String toDateStr = btnDateTo.getTag() instanceof String ? (String) btnDateTo.getTag() : "";
        String dateInfo = "for " + fromDateStr + (toDateStr.isEmpty() ? "" : " to " + toDateStr);
        if(fromDateStr.equals("today") && toDateStr.isEmpty()){
            dateInfo = "for today (" + new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date()) + ")";
        }

        String cropTitle = crop;
        if (!variety.isEmpty()) {
            cropTitle += " (" + variety + ")";
        }

        // ✅ FIXED PROMPT: Enforcing Strict Paragraph Separation for narrative sections
        String prompt = "Generate a title: '**Agriculture Data Expert Report: " + cropTitle + " in " + city + "**'.\n\n" +
                "Then, provide a complete, bilingual report for " + cropTitle + " in " + city + " district, " + state + ", " + dateInfo + ". " +
                "Your response MUST be in clean Markdown format.\n\n" +
                "**STRICT FORMAT RULES:**\n" +
                "1. For the 'Market Prices' section: Use a list with labels (English / Hindi).\n" +
                "2. For ALL OTHER SECTIONS (Weather, Market Intelligence, etc.): \n" +
                "   - Write the **English Paragraph** first.\n" +
                "   - Leave an **Empty Line**.\n" +
                "   - Write the **Hindi Paragraph**.\n" +
                "   - **DO NOT** use mixed 'English / Hindi' text inside the paragraphs.\n\n" +
                "**REQUIRED SECTIONS:**\n\n" +
                "1. **Market Prices / मंडी भाव**\n" +
                "   * **Date / तिथि:** [Date]\n" +
                "   * **Market / मंडी:** " + city + " (Estimated / अनुमानित)\n" +
                "   * **Variety / किस्म:** " + (variety.isEmpty() ? "General / सामान्य" : variety) + "\n" +
                "   * **Min Price / न्यूनतम भाव:** ₹[price]\n" +
                "   * **Max Price / अधिकतम भाव:** ₹[price]\n" +
                "   * **Modal Price / औसत भाव:** ₹[price]\n\n" +
                "2. **Crop Stage & Phenology / फसल चरण और फेनोलॉजी**\n" +
                "   [Write a brief summary in English.]\n\n" +
                "   [Write the exact Hindi translation.]\n\n" +
                "3. **Weather Forecast / मौसम पूर्वानुमान**\n" +
                "   [Write a brief 3-day forecast in English.]\n\n" +
                "   [Write the exact Hindi translation.]\n\n" +
                "4. **Market Intelligence / बाजार आसूचना**\n" +
                "   [Write key insights in English.]\n\n" +
                "   [Write the exact Hindi translation.]\n\n" +
                "5. **Recommendations / सिफारिशें**\n" +
                "   [Write actionable advice in English.]\n\n" +
                "   [Write the exact Hindi translation.]\n\n" +
                "6. **Disclaimer / अस्वीकरण**\n" +
                "   [Write the disclaimer in English.]\n\n" +
                "   [Write the disclaimer in Hindi.]";


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
                    tvPriceResults.setText("Could not get an AI report. Please check your internet connection.\nAI रिपोर्ट प्राप्त नहीं हो सकी। कृपया अपना इंटरनेट कनेक्शन जांचें।");
                    loadingIndicator.setVisibility(View.GONE);
                    cardResult.setVisibility(View.VISIBLE);
                    btnGetPrices.setEnabled(true);
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String responseBody = response.body().string();
                final int responseCode = response.code();

                runOnUiThread(() -> {
                    String finalResultTextForFallback = "An unexpected error occurred.\nएक अप्रत्याशित त्रुटि हुई।";
                    try {
                        if (response.isSuccessful()) {
                            JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
                            if (json.has("candidates") && json.getAsJsonArray("candidates").size() > 0) {
                                JsonObject candidate = json.getAsJsonArray("candidates").get(0).getAsJsonObject();
                                if (candidate.has("finishReason") && "SAFETY".equals(candidate.get("finishReason").getAsString())) {
                                    finalResultTextForFallback = "Could not generate estimate due to safety settings.\nसुरक्षा सेटिंग्स के कारण अनुमान उत्पन्न नहीं हो सका।";
                                } else if (candidate.has("content")) {
                                    String geminiMarkdownResult = candidate.getAsJsonObject("content").getAsJsonArray("parts").get(0).getAsJsonObject().get("text").getAsString();
                                    markwon.setMarkdown(tvPriceResults, geminiMarkdownResult);
                                    // Success! Return here.
                                    loadingIndicator.setVisibility(View.GONE);
                                    cardResult.setVisibility(View.VISIBLE);
                                    btnGetPrices.setEnabled(true);
                                    return;
                                } else {
                                    finalResultTextForFallback = "Estimate unavailable. Try different criteria.\nअनुमान अनुपलब्ध। भिन्न मानदंड आजमाएं।";
                                }
                            } else {
                                Log.w("GEMINI_API_WARN", "Empty or invalid response from Gemini: " + responseBody);
                                finalResultTextForFallback = "Could not get an estimate from AI.\nAI से अनुमान प्राप्त नहीं हो सका।";
                            }
                        } else {
                            Log.e("GEMINI_API_ERROR", "API Error Code: " + responseCode + ", Body: " + responseBody);
                            finalResultTextForFallback = "AI service error (" + responseCode + "). Please try again.\nAI सेवा त्रुटि (" + responseCode + ")। कृपया पुनः प्रयास करें।";
                        }
                    } catch (Exception e){
                        Log.e("GEMINI_API_ERROR", "Error parsing Gemini response", e);
                        finalResultTextForFallback = "Received an unreadable response from the AI expert.\nAI विशेषज्ञ से एक अपठनीय प्रतिक्रिया प्राप्त हुई।";
                    }

                    tvPriceResults.setText(finalResultTextForFallback);
                    loadingIndicator.setVisibility(View.GONE);
                    cardResult.setVisibility(View.VISIBLE);
                    btnGetPrices.setEnabled(true);
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}