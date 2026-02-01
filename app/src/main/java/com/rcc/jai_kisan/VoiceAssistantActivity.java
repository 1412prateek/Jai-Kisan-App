package com.rcc.jai_kisan;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.noties.markwon.Markwon;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import com.rcc.jai_kisan.BuildConfig;

public class VoiceAssistantActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_SPEECH_INPUT = 1000;

    private TextView tvStatus, tvResultHindi, tvResultEnglish;
    private ImageButton btnMic, btnSpeakHindi, btnSpeakEnglish;
    private MaterialToolbar toolbar;

    private ProgressBar progressBar;

    private TextToSpeech textToSpeech;
    private Markwon markwon;

    private List<JsonObject> chatHistory = new ArrayList<>();

    private boolean isSpeakingHindi = false;
    private boolean isSpeakingEnglish = false;

    private static final String GEMINI_API_KEY = BuildConfig.GEMINI_API_KEY;
    private static final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemma-3-4b-it:generateContent?key=" + GEMINI_API_KEY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_assistant);

        markwon = Markwon.create(this);

        toolbar = findViewById(R.id.toolbar);
        tvStatus = findViewById(R.id.tvStatus);

        tvResultHindi = findViewById(R.id.tvResultHindi);
        tvResultEnglish = findViewById(R.id.tvResultEnglish);

        btnMic = findViewById(R.id.btnMic);
        btnSpeakHindi = findViewById(R.id.btnSpeakHindi);
        btnSpeakEnglish = findViewById(R.id.btnSpeakEnglish);

        progressBar = findViewById(R.id.progressBar);

        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        textToSpeech = new TextToSpeech(getApplicationContext(), status -> {
            if (status != TextToSpeech.ERROR) {
                textToSpeech.setLanguage(new Locale("hi", "IN"));
                speakText("नमस्ते! मैं किसान मित्र हूँ। पूछिये क्या मदद करूँ?", new Locale("hi", "IN"));
            }
        });

        btnMic.setOnClickListener(v -> speak());

        btnSpeakHindi.setOnClickListener(v -> {
            if (textToSpeech.isSpeaking() && isSpeakingHindi) {
                textToSpeech.stop();
                isSpeakingHindi = false;
            } else {
                String rawText = tvResultHindi.getText().toString();
                if (!rawText.isEmpty() && !rawText.equals("Thinking... / सोच रहा हूँ...")) {
                    String cleanText = cleanMarkdownForSpeech(rawText);
                    if (textToSpeech.isSpeaking()) textToSpeech.stop();
                    isSpeakingHindi = true;
                    isSpeakingEnglish = false;
                    speakText(cleanText, new Locale("hi", "IN"));
                }
            }
        });

        btnSpeakEnglish.setOnClickListener(v -> {
            if (textToSpeech.isSpeaking() && isSpeakingEnglish) {
                textToSpeech.stop();
                isSpeakingEnglish = false;
            } else {
                String rawText = tvResultEnglish.getText().toString();
                if (!rawText.isEmpty() && !rawText.equals("Thinking...")) {
                    String cleanText = cleanMarkdownForSpeech(rawText);
                    if (textToSpeech.isSpeaking()) textToSpeech.stop();
                    isSpeakingEnglish = true;
                    isSpeakingHindi = false;
                    speakText(cleanText, Locale.US);
                }
            }
        });

        initializeChat();
    }

    private String cleanMarkdownForSpeech(String markdown) {
        if (markdown == null) return "";
        // Remove bold/italic markers
        String clean = markdown.replaceAll("\\*\\*", "").replaceAll("\\*", "");
        // Remove hash headers
        clean = clean.replaceAll("#+", "");
        // Remove the separator
        clean = clean.replaceAll("\\|\\|\\|", "");
        return clean.trim();
    }

    private void initializeChat() {
        chatHistory.clear();
    }

    private void speak() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN");
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Ask your question / अपना सवाल पूछें");

        try {
            startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT);
        } catch (Exception e) {
            Toast.makeText(this, "Voice input not supported on this device.", Toast.LENGTH_SHORT).show();
        }
    }

    private void speakText(String text, Locale locale) {
        textToSpeech.setLanguage(locale);
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SPEECH_INPUT && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (result != null && !result.isEmpty()) {
                String userQuery = result.get(0);
                tvStatus.setText("You asked: " + userQuery);
                askGemini(userQuery);
            }
        }
    }

    private void askGemini(String query) {
        runOnUiThread(() -> {
            tvResultHindi.setText("Thinking... / सोच रहा हूँ...");
            tvResultEnglish.setText("Thinking...");
            progressBar.setVisibility(View.VISIBLE);
        });

        // ✅ INCREASED TIMEOUT: Ensures it has time to write BOTH languages
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(180, TimeUnit.SECONDS)
                .readTimeout(180, TimeUnit.SECONDS)
                .build();

        String finalQuery;
        if (chatHistory.isEmpty()) {
            // ✅ FIXED PROMPT: Forces ||| Separator & Bilingual Output
            String systemPrompt = "Role: 'Kisan Mitra' (Agriculture Expert).\n\n" +
                    "**CRITICAL RULE:** You MUST provide the answer in **BOTH Hindi AND English** separated by '|||'.\n" +
                    "If you fail to provide the separator '|||', the system will crash.\n\n" +
                    "**RESPONSE STRUCTURE:**\n" +
                    "1. **HINDI PART:**\n" +
                    "   - 3 Bullet Points (Direct Advice)\n" +
                    "   - Short Paragraph\n" +
                    "   - 1-Line Conclusion\n" +
                    "2. **SEPARATOR:**\n" +
                    "   |||\n" +
                    "3. **ENGLISH PART:**\n" +
                    "   - Exact translation of the Hindi part.\n\n" +
                    "**RESTRICTIONS:**\n" +
                    "- Refuse non-agriculture questions.\n" +
                    "- No fillers ('Okay', 'Here is').\n" +
                    "- Use **Bold** titles.\n\n" +
                    "User Question: ";
            finalQuery = systemPrompt + query;
        } else {
            finalQuery = query;
        }

        JsonObject userPart = new JsonObject();
        userPart.addProperty("text", finalQuery);

        JsonArray userParts = new JsonArray();
        userParts.add(userPart);

        JsonObject userContent = new JsonObject();
        userContent.addProperty("role", "user");
        userContent.add("parts", userParts);

        chatHistory.add(userContent);

        JsonObject requestJson = new JsonObject();
        JsonArray contentsArray = new JsonArray();

        // Sliding window to save tokens
        int historyLimit = 4;
        int startIndex = Math.max(0, chatHistory.size() - historyLimit);
        for (int i = startIndex; i < chatHistory.size(); i++) {
            contentsArray.add(chatHistory.get(i));
        }

        requestJson.add("contents", contentsArray);

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
                    progressBar.setVisibility(View.GONE);
                    tvResultHindi.setText("Connection Failed. Check internet.");
                    tvResultEnglish.setText("Connection Failed.");
                    if (!chatHistory.isEmpty()) chatHistory.remove(chatHistory.size() - 1);
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseBody = response.body().string();
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    try {
                        if (!response.isSuccessful()) {
                            tvResultHindi.setText("Server Error: " + response.code());
                            tvResultEnglish.setText("Check Quota. Code: " + response.code());
                            if (!chatHistory.isEmpty()) chatHistory.remove(chatHistory.size() - 1);
                            return;
                        }

                        JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();

                        if (!json.has("candidates") || json.getAsJsonArray("candidates").size() == 0) {
                            tvResultHindi.setText("No response received. Try again.");
                            if (!chatHistory.isEmpty()) chatHistory.remove(chatHistory.size() - 1);
                            return;
                        }

                        JsonObject candidate = json.getAsJsonArray("candidates").get(0).getAsJsonObject();
                        JsonObject content = candidate.getAsJsonObject("content");
                        String answer = content.getAsJsonArray("parts")
                                .get(0).getAsJsonObject()
                                .get("text").getAsString();

                        chatHistory.add(content);

                        // ✅ SPLIT LOGIC
                        String[] parts = answer.split("\\|\\|\\|");

                        if (parts.length >= 2) {
                            String hindiText = parts[0].trim();
                            String englishText = parts[1].trim();

                            markwon.setMarkdown(tvResultHindi, hindiText);
                            markwon.setMarkdown(tvResultEnglish, englishText);

                        } else {
                            // Fallback: If AI fails to split, show full text in Hindi box and warn in English box
                            markwon.setMarkdown(tvResultHindi, answer);
                            tvResultEnglish.setText("Translation unavailable (AI skipped separator).");
                        }

                    } catch (Exception e) {
                        tvResultHindi.setText("Error parsing response.");
                        e.printStackTrace();
                        if (!chatHistory.isEmpty()) chatHistory.remove(chatHistory.size() - 1);
                    }
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }
}