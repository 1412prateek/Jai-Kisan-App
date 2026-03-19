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
    private MaterialCardView cardResult;
    private TextView tvDiseaseName, tvDiseaseInfo;
    private MaterialButton btnDetectDisease, btnShare;
    private CircularProgressIndicator loadingIndicator;
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
        cardResult = findViewById(R.id.cardResult);
        tvDiseaseName = findViewById(R.id.tvDiseaseName);
        tvDiseaseInfo = findViewById(R.id.tvDiseaseInfo);
        tvDiseaseInfo.setMovementMethod(LinkMovementMethod.getInstance());

        MaterialButton btnCaptureImage = findViewById(R.id.btnCaptureImage);
        MaterialButton btnUploadImage = findViewById(R.id.btnUploadImage);
        btnDetectDisease = findViewById(R.id.btnDetectDisease);
        loadingIndicator = findViewById(R.id.loadingIndicator);
        btnShare = findViewById(R.id.btnShare);

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
                cardResult.setVisibility(View.GONE);
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
        String prompt = "You are an agricultural expert for Indian farmers. Analyze this image.\n" +
                "Your response MUST be in a clean, fully bilingual, and point-wise format. For EACH section, follow these rules exactly:\n" +
                "1. Create a single bold heading line with the English heading, a space, a forward slash, a space, and then the bold Hindi heading.\n" +
                "2. On the next line, write the complete English description for that section using bullet points (`- `).\n" +
                "3. Leave one empty line for a paragraph break.\n" +
                "4. On the next line, write the complete Hindi translation for that description, also using bullet points (`- `).\n\n" +
                "**Crucial Example of the required format for every section:**\n" +
                "**Possible Diseases / संभावित रोग**\n" +
                "- The leaf shows signs of Bacterial Leaf Streak.\n" +
                "- This is common in maize crops during humid weather.\n\n" +
                "- पत्ती पर बैक्टीरियल लीफ स्ट्रीक के लक्षण दिखाई दे रहे हैं।\n" +
                "- यह आर्द्र मौसम के दौरान मक्के की फसलों में आम है।\n\n" +
                "**VERY IMPORTANT FOR 'Government Schemes' SECTION:** After the description, you MUST provide a link using Markdown syntax. The link text MUST be 'Click here for more information / अधिक जानकारी के लिए यहां क्लिक करें'. The URL should be a genuine, official government portal like https://agricoop.gov.in/.\n\n" +
                "Now, provide your full analysis for the image using this exact, point-wise, fully bilingual format for all these sections:\n" +
                "- Introduction\n" +
                "- Crop Name\n" +
                "- Health Status\n" +
                "- Possible Diseases\n" +
                "- Survival Chance\n" +
                "- Recommended Medicines\n" +
                "- Government Schemes";
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
                    tvDiseaseName.setText("Error");
                    tvDiseaseInfo.setText("Failed to connect. Please check your internet connection.\nकनेक्शन विफल। कृपया अपना इंटरनेट कनेक्शन जांचें।\n" + e.getMessage());
                    cardResult.setVisibility(View.VISIBLE);
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
                            tvDiseaseName.setText("API Error: " + response.code());
                            tvDiseaseInfo.setText(responseBody);
                            cardResult.setVisibility(View.VISIBLE);
                            return;
                        }
                        JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
                        String result = json
                                .getAsJsonArray("candidates")
                                .get(0).getAsJsonObject()
                                .getAsJsonObject("content")
                                .getAsJsonArray("parts")
                                .get(0).getAsJsonObject()
                                .get("text").getAsString();

                        currentResultText = result;

                        tvDiseaseName.setText("Analysis Report (विश्लेषण रिपोर्ट)");
                        markwon.setMarkdown(tvDiseaseInfo, result);
                        cardResult.setVisibility(View.VISIBLE);

                    } catch (Exception e) {
                        tvDiseaseName.setText("Error");
                        tvDiseaseInfo.setText("Unexpected response from server.\nसर्वर से अप्रत्याशित प्रतिक्रिया।");
                        cardResult.setVisibility(View.VISIBLE);
                    } finally {
                        loadingIndicator.setVisibility(View.GONE);
                        btnDetectDisease.setEnabled(true);
                    }
                });
            }
        });
    }
}

