// backup file for working detection using gemma

package com.rcc.jai_kisan;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.method.LinkMovementMethod;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import android.widget.LinearLayout;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.ByteArrayOutputStream;
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


public class DetectDiseaseActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;

    // This is your new, 100% secure line
    private static final String GEMINI_API_KEY = BuildConfig.GEMINI_API_KEY;
    // THIS LINE IS THE PROBLEM (it's an unstable "preview" model)
    // THIS IS THE CORRECT, STABLE, LITE MODEL

    // NEW (Fixed with specific version ID)
    // Alternative: Use 'v1' instead of 'v1beta'
    // CORRECTED URL: Uses the stable "Gemini 2.5 Flash" (Standard) found in your list
    // CORRECTED URL: Use Gemini 2.0 Flash (Stable, High Limit)
    // USE THIS EXACT URL - It points to the updated 1.5 Flash (Sep 2024 version)
    // USE THIS EXACT MODEL NAME
    // FASTER VERSION (4 Billion Parameters instead of 12)
    private static final String GEMINI_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key="
                    + GEMINI_API_KEY;

    private ImageView imagePreview;
    private TextView placeholderText;
    private LinearLayout llResultsContainer, llDos, llDonts, llFertilizers;
    private TextView tvCropNameTitle, tvHealthStatus, tvHealthScore, tvDiseaseName, tvScientificName, tvFinalSummary, tvSurvivalScore;
    private CircularProgressIndicator progressHealthScore, loadingIndicator;
    private LinearProgressIndicator progressSurvival;
    private MaterialButton btnDetectDisease, btnShare, btnFertilizerCalc, btnCostCalc, btnIrrigationCalc, btnYieldCalc;
    private Markwon markwon;
    private Bitmap selectedBitmap = null;
    private String currentResultText = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detect_disease);

        // ✅ IMPROVEMENT: Setup Toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Photo Expert / फोटो विशेषज्ञ");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        markwon = Markwon.create(this);

        imagePreview = findViewById(R.id.imagePreview);
        placeholderText = findViewById(R.id.placeholderText);
        llResultsContainer = findViewById(R.id.llResultsContainer);
        tvCropNameTitle = findViewById(R.id.tvCropNameTitle);
        tvHealthStatus = findViewById(R.id.tvHealthStatus);
        progressHealthScore = findViewById(R.id.progressHealthScore);
        tvHealthScore = findViewById(R.id.tvHealthScore);
        tvDiseaseName = findViewById(R.id.tvDiseaseName);
        tvScientificName = findViewById(R.id.tvScientificName);
        llDos = findViewById(R.id.llDos);
        llDonts = findViewById(R.id.llDonts);
        llFertilizers = findViewById(R.id.llFertilizers);
        tvFinalSummary = findViewById(R.id.tvFinalSummary);
        progressSurvival = findViewById(R.id.progressSurvival);
        tvSurvivalScore = findViewById(R.id.tvSurvivalScore);

        MaterialButton btnCaptureImage = findViewById(R.id.btnCaptureImage);
        MaterialButton btnUploadImage = findViewById(R.id.btnUploadImage);
        btnDetectDisease = findViewById(R.id.btnDetectDisease);
        loadingIndicator = findViewById(R.id.loadingIndicator);
        btnShare = findViewById(R.id.btnShare);

        btnFertilizerCalc = findViewById(R.id.btnFertilizerCalc);
        btnCostCalc = findViewById(R.id.btnCostCalc);
        btnIrrigationCalc = findViewById(R.id.btnIrrigationCalc);
        btnYieldCalc = findViewById(R.id.btnYieldCalc);

        btnCaptureImage.setOnClickListener(v -> {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        });
        btnUploadImage.setOnClickListener(v -> {
            Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(pickPhoto, REQUEST_IMAGE_PICK);
        });
        btnDetectDisease.setOnClickListener(v -> {
            if (selectedBitmap == null) {
                Toast.makeText(this, "Please select or capture an image first.", Toast.LENGTH_SHORT).show();
            } else {
                loadingIndicator.setVisibility(View.VISIBLE);
                btnDetectDisease.setEnabled(false);
                llResultsContainer.setVisibility(View.GONE);
                callGeminiAPI(selectedBitmap);
            }
        });
        btnShare.setOnClickListener(v -> {
            if (currentResultText != null && !currentResultText.isEmpty()) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Jai Kisan - Photo Expert Report");
                shareIntent.putExtra(Intent.EXTRA_TEXT, currentResultText);
                startActivity(Intent.createChooser(shareIntent, "Share Report via / रिपोर्ट साझा करें"));
            } else {
                Toast.makeText(this, "No report to share.", Toast.LENGTH_SHORT).show();
            }
        });

        // NEW: Google Maps Button Setup inside the Card
        MaterialButton btnOpenMaps = findViewById(R.id.btnOpenMaps);
        btnOpenMaps.setOnClickListener(v -> {
            // Searches for nearby agriculture centers/equipment rentals
            Uri gmmIntentUri = Uri.parse("geo:0,0?q=agricultural+equipment+and+fertilizer+store");
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            try {
                startActivity(mapIntent);
            } catch (Exception e) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=agricultural+equipment+and+fertilizer+store"));
                startActivity(browserIntent);
            }
        });
    }

    // ✅ IMPROVEMENT: Handle clicks on the Toolbar's back arrow
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // End this activity and go back to the previous one
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            Bitmap bitmap = null;
            try {
                if (requestCode == REQUEST_IMAGE_CAPTURE && data.getExtras() != null) {
                    bitmap = (Bitmap) data.getExtras().get("data");
                } else if (requestCode == REQUEST_IMAGE_PICK && data.getData() != null) {
                    Uri selectedImage = data.getData();
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                }
                if (bitmap != null) {
                    selectedBitmap = bitmap;
                    imagePreview.setImageBitmap(bitmap);
                    placeholderText.setVisibility(View.GONE);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    


    private void populateActionList(LinearLayout container, JsonArray array, boolean isDo) {
        container.removeAllViews();
        int iconSize = (int) (24 * getResources().getDisplayMetrics().density);
        int margin = (int) (12 * getResources().getDisplayMetrics().density);
        
        for (int i = 0; i < array.size(); i++) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(0, 0, 0, margin);
            
            ImageView icon = new ImageView(this);
            icon.setLayoutParams(new LinearLayout.LayoutParams(iconSize, iconSize));
            if (isDo) {
                icon.setImageResource(R.drawable.ic_check_circle);
                icon.setColorFilter(android.graphics.Color.parseColor("#2E7D32"));
            } else {
                icon.setImageResource(R.drawable.ic_close);
                icon.setColorFilter(android.graphics.Color.parseColor("#C62828"));
            }
            
            TextView tv = new TextView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 
                LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(margin, 0, 0, 0);
            tv.setLayoutParams(params);
            tv.setText(array.get(i).getAsString());
            tv.setTextColor(android.graphics.Color.parseColor("#333333"));
            tv.setTextSize(14f);
            
            row.addView(icon);
            row.addView(tv);
            container.addView(row);
        }
    }

    private void populateFertilizerList(LinearLayout container, JsonArray array) {
        container.removeAllViews();
        int iconSize = (int) (24 * getResources().getDisplayMetrics().density);
        int margin = (int) (12 * getResources().getDisplayMetrics().density);
        
        for (int i = 0; i < array.size(); i++) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(0, 0, 0, margin);
            
            ImageView icon = new ImageView(this);
            icon.setLayoutParams(new LinearLayout.LayoutParams(iconSize, iconSize));
            // Use pest icon with teal tint to represent cures
            icon.setImageResource(R.drawable.ic_pest);
            icon.setColorFilter(android.graphics.Color.parseColor("#00838F"));
            
            TextView tv = new TextView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 
                LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(margin, 0, 0, 0);
            tv.setLayoutParams(params);
            tv.setText(array.get(i).getAsString());
            tv.setTextColor(android.graphics.Color.parseColor("#004D40"));
            tv.setTextSize(14f);
            
            row.addView(icon);
            row.addView(tv);
            container.addView(row);
        }
    }

    private String getSanitizedCropName(String raw) {
        String lowerRaw = raw.toLowerCase();
        String[] CROPS = {"Wheat / गेहूं", "Rice / धान", "Maize / मक्का", "Cotton / कपास", "Sugarcane / गन्ना", "Soybean / सोयाबीन", "Mustard / सरसों", "Potato / आलू", "Onion / प्याज", "Chili / मिर्च", "Millets / बाजरा", "Pulses / दालें"};
        for (String c : CROPS) {
            String primary = c.split("/")[0].trim().toLowerCase();
            if (lowerRaw.contains(primary)) return c;
        }
        return raw;
    }

    private void callGeminiAPI(Bitmap bitmap) {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
        String base64Image = Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP);

        JsonArray parts = new JsonArray();
        JsonObject textPart = new JsonObject();
        String prompt = "You are an agricultural expert for Indian farmers. Analyze this image and respond EXACTLY in JSON format.\n" +
                "DO NOT wrap the response in ```json ``` markdown tags.\n" +
                "Your response MUST be fully bilingual. For text fields, First write the English meaning, leave one line drop, then write the Hindi meaning.\n" +
                "Construct the JSON exactly matching these keys:\n" +
                "{\n" +
                "  \"crop_name\": \"Crop name in English / Hindi (e.g. Wheat / गेहूँ)\",\n" +
                "  \"health_status\": \"Short severity string (e.g. Severely Infected / गंभीर रूप से संक्रमित)\",\n" +
                "  \"health_score\": 45,\n" +
                "  \"disease_name\": \"Name of the disease (e.g. Leaf Rust / पत्ती का रतुआ रोग)\",\n" +
                "  \"scientific_names\": \"Scientific/Botanical names of pathogens if any (e.g. Puccinia triticina)\",\n" +
                "  \"survival_chance\": 85,\n" +
                "  \"dos\": [\n" +
                "    \"Short Action 1 in English / Hindi\",\n" +
                "    \"Action 2 in English / Hindi\"\n" +
                "  ],\n" +
                "  \"donts\": [\n" +
                "    \"Avoid Action 1 in English / Hindi\"\n" +
                "  ],\n" +
                "  \"suggested_fertilizers\": [\n" +
                "    \"Fertilizer 1 exact details in English / Hindi\",\n" +
                "    \"Cure 2 in English / Hindi\"\n" +
                "  ],\n" +
                "  \"final_summary\": \"A short 2-3 line summary in English and Hindi.\"\n" +
                "}";
        textPart.addProperty("text", prompt);

        parts.add(textPart);
        JsonObject imagePart = new JsonObject();
        JsonObject inlineData = new JsonObject();
        inlineData.addProperty("mimeType", "image/jpeg");
        inlineData.addProperty("data", base64Image);
        imagePart.add("inlineData", inlineData);
        parts.add(imagePart);
        JsonObject content = new JsonObject();
        content.add("parts", parts);
        JsonArray contents = new JsonArray();
        contents.add(content);
        JsonObject requestJson = new JsonObject();
        requestJson.add("contents", contents);

        RequestBody body = RequestBody.create(
                requestJson.toString(),
                MediaType.get("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(GEMINI_URL)
                .post(body)
                .build();


        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    tvCropNameTitle.setText("Error");
                    tvHealthScore.setText("0");
                    if (tvFinalSummary != null) tvFinalSummary.setText("Failed to connect. Please check your internet connection.\nकनेक्शन विफल। कृपया अपना इंटरनेट कनेक्शन जांचें।\n" + e.getMessage());
                    llResultsContainer.setVisibility(View.VISIBLE);
                    loadingIndicator.setVisibility(View.GONE);
                    btnDetectDisease.setEnabled(true);
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseBody = response.body().string();
                runOnUiThread(() -> {
                    try {
                        if (!response.isSuccessful()) {
                            tvCropNameTitle.setText("API Error: " + response.code());
                            if (tvFinalSummary != null) tvFinalSummary.setText(responseBody);
                            llResultsContainer.setVisibility(View.VISIBLE);
                            return;
                        }
                        JsonObject rootJson = JsonParser.parseString(responseBody).getAsJsonObject();
                        String rawJsonString = rootJson
                                .getAsJsonArray("candidates")
                                .get(0).getAsJsonObject()
                                .getAsJsonObject("content")
                                .getAsJsonArray("parts")
                                .get(0).getAsJsonObject()
                                .get("text").getAsString();

                        // Clean up markdown ticks if Gemini included them
                        if (rawJsonString.startsWith("```json")) {
                            rawJsonString = rawJsonString.substring(7, rawJsonString.length() - 3).trim();
                        } else if (rawJsonString.startsWith("```")) {
                            rawJsonString = rawJsonString.substring(3, rawJsonString.length() - 3).trim();
                        }

                        JsonObject jsonResponse = JsonParser.parseString(rawJsonString).getAsJsonObject();
                        
                        String cropName = jsonResponse.has("crop_name") ? jsonResponse.get("crop_name").getAsString() : "Unknown Crop";
                        String healthStatus = jsonResponse.has("health_status") ? jsonResponse.get("health_status").getAsString() : "N/A";
                        int healthScore = jsonResponse.has("health_score") ? jsonResponse.get("health_score").getAsInt() : 0;
                        String diseaseName = jsonResponse.has("disease_name") ? jsonResponse.get("disease_name").getAsString() : "N/A";
                        String scientificName = jsonResponse.has("scientific_names") ? jsonResponse.get("scientific_names").getAsString() : "Unknown";
                        int survivalChance = jsonResponse.has("survival_chance") ? jsonResponse.get("survival_chance").getAsInt() : 0;
                        String finalSummary = jsonResponse.has("final_summary") ? jsonResponse.get("final_summary").getAsString() : "N/A";

                        currentResultText = "Crop: " + cropName + "\nStatus: " + healthStatus + "\nDisease: " + diseaseName + "\n\nSummary:\n" + finalSummary;

                        tvCropNameTitle.setText(cropName);
                        tvHealthStatus.setText(healthStatus);
                        tvHealthScore.setText(String.valueOf(healthScore));
                        progressHealthScore.setProgressCompat(healthScore, true);

                        if (healthScore >= 70) {
                            progressHealthScore.setIndicatorColor(android.graphics.Color.parseColor("#2E7D32"));
                        } else if (healthScore >= 40) {
                            progressHealthScore.setIndicatorColor(android.graphics.Color.parseColor("#F9A825"));
                        } else {
                            progressHealthScore.setIndicatorColor(android.graphics.Color.parseColor("#C62828"));
                        }
                        
                        tvDiseaseName.setText("Detected: " + diseaseName);
                        tvScientificName.setText(scientificName);
                        tvSurvivalScore.setText(survivalChance + "%");
                        progressSurvival.setProgressCompat(survivalChance, true);
                        markwon.setMarkdown(tvFinalSummary, finalSummary);

                        if (jsonResponse.has("dos") && jsonResponse.get("dos").isJsonArray()) {
                            populateActionList(llDos, jsonResponse.getAsJsonArray("dos"), true);
                        } else {
                            llDos.removeAllViews();
                        }
                        
                        if (jsonResponse.has("donts") && jsonResponse.get("donts").isJsonArray()) {
                            populateActionList(llDonts, jsonResponse.getAsJsonArray("donts"), false);
                        } else {
                            llDonts.removeAllViews();
                        }

                        if (jsonResponse.has("suggested_fertilizers") && jsonResponse.get("suggested_fertilizers").isJsonArray()) {
                            populateFertilizerList(llFertilizers, jsonResponse.getAsJsonArray("suggested_fertilizers"));
                        } else {
                            llFertilizers.removeAllViews();
                        }

                        // Connect the Farming Tools intents with auto-filled Crop info
                        String mappedCropStr = getSanitizedCropName(cropName);

                        String suggestedFertsStr = "";
                        if (jsonResponse.has("suggested_fertilizers") && jsonResponse.get("suggested_fertilizers").isJsonArray()) {
                            JsonArray fertsArray = jsonResponse.getAsJsonArray("suggested_fertilizers");
                            StringBuilder sb = new StringBuilder();
                            for (int idx = 0; idx < fertsArray.size(); idx++) {
                                sb.append(fertsArray.get(idx).getAsString()).append(". ");
                            }
                            suggestedFertsStr = sb.toString();
                        }
                        String finalSuggestedFertsStr = suggestedFertsStr;

                        btnFertilizerCalc.setOnClickListener(vClick -> {
                            Intent i = new Intent(DetectDiseaseActivity.this, FertilizerCalculatorActivity.class);
                            i.putExtra("autofill_crop", mappedCropStr);
                            if (!finalSuggestedFertsStr.isEmpty()) {
                                i.putExtra("suggested_fertilizers", finalSuggestedFertsStr);
                            }
                            startActivity(i);
                        });

                        btnYieldCalc.setOnClickListener(vClick -> {
                            Intent i = new Intent(DetectDiseaseActivity.this, YieldEstimatorActivity.class);
                            i.putExtra("autofill_crop", mappedCropStr);
                            startActivity(i);
                        });

                        btnIrrigationCalc.setOnClickListener(vClick -> {
                            Intent i = new Intent(DetectDiseaseActivity.this, IrrigationPlannerActivity.class);
                            i.putExtra("autofill_crop", mappedCropStr);
                            startActivity(i);
                        });

                        btnCostCalc.setOnClickListener(vClick -> {
                            Intent i = new Intent(DetectDiseaseActivity.this, CostCalculatorActivity.class);
                            i.putExtra("autofill_crop", mappedCropStr);
                            startActivity(i);
                        });

                        llResultsContainer.setVisibility(View.VISIBLE);

                    } catch (Exception e) {
                        tvCropNameTitle.setText("Error");
                        if (tvFinalSummary != null) tvFinalSummary.setText("Unexpected response from server.\nसर्वर से अप्रत्याशित प्रतिक्रिया।\n" + e.getMessage());
                        llResultsContainer.setVisibility(View.VISIBLE);
                    } finally {
                        loadingIndicator.setVisibility(View.GONE);
                        btnDetectDisease.setEnabled(true);
                    }
                });
            }
        });
    }
}

