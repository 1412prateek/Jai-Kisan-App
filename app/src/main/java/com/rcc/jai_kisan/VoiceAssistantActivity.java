package com.rcc.jai_kisan;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
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
import androidx.core.content.FileProvider;
import com.google.android.material.appbar.MaterialToolbar;

import java.io.File;

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

public class VoiceAssistantActivity extends BaseActivity {

    private static final int REQUEST_CODE_SPEECH_INPUT = 1000;

    private TextView tvStatus, tvResultHindi, tvResultEnglish;
    private ImageButton btnMic, btnSpeakHindi, btnSpeakEnglish;
    private MaterialToolbar toolbar;

    private View progressBar;

    private TextToSpeech textToSpeech;
    private Markwon markwon;

    // Container used for PDF rendering of the last AI response
    private View resultPdfContainer;

    private List<JsonObject> chatHistory = new ArrayList<>();

    private boolean isSpeakingHindi = false;
    private boolean isSpeakingEnglish = false;

    private static final String[] MODELS = {
        "gemini-2.5-flash",
        "gemini-2.5-flash-lite-preview-06-17",
        "gemini-2.0-flash"
    };

    private String buildUrl(String modelName) {
        return "https://generativelanguage.googleapis.com/v1beta/models/" + modelName + ":generateContent?key=" + GeminiKeyManager.getNextKey();
    }

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

        // Assign the result panel as our PDF container
        resultPdfContainer = tvResultHindi;

        // Wire up optional Share button (id: btnSharePdf)
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) com.google.android.material.button.MaterialButton btnSharePdf = findViewById(R.id.btnSharePdf);
        if (btnSharePdf != null) {
            btnSharePdf.setOnClickListener(v -> shareResult());
        }

        textToSpeech = new TextToSpeech(getApplicationContext(), status -> {
            if (status != TextToSpeech.ERROR) {
                textToSpeech.setLanguage(new Locale("hi", "IN"));
                speakText("\u0928\u092e\u0938\u094d\u0924\u0947! \u092e\u0948\u0902 \u0915\u093f\u0938\u093e\u0928 \u092e\u093f\u0924\u094d\u0930 \u0939\u0942\u0901\u0964 \u092a\u0942\u091b\u093f\u092f\u0947 \u0915\u094d\u092f\u093e \u092e\u0926\u0926 \u0915\u0930\u0942\u0901?", new Locale("hi", "IN"));
            }
        });

        btnMic.setOnClickListener(v -> speak());

        btnSpeakHindi.setOnClickListener(v -> {
            if (textToSpeech.isSpeaking() && isSpeakingHindi) {
                textToSpeech.stop();
                isSpeakingHindi = false;
            } else {
                String rawText = tvResultHindi.getText().toString();
                if (!rawText.isEmpty() && !rawText.equals("Thinking... / \u0938\u094b\u091a \u0930\u0939\u093e \u0939\u0942\u0901...")) {
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
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Ask your question / \u0905\u092a\u0928\u093e \u0938\u0935\u093e\u0932 \u092a\u0942\u091b\u0947\u0902");

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
            tvResultHindi.setText("Thinking... / \u0938\u094b\u091a \u0930\u0939\u093e \u0939\u0942\u0901...");
            tvResultEnglish.setText("Thinking...");
            progressBar.setVisibility(View.VISIBLE);
        });

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

        askGeminiRecursive(query, 0);
    }

    private void askGeminiRecursive(final String query, final int modelIndex) {
        if (modelIndex >= MODELS.length) {
            runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                tvResultHindi.setText("All models are currently busy. Please try again in a minute.\n\u0938\u092d\u0940 \u092e\u0949\u0921\u0932 \u0935\u094d\u092f\u0938\u094d\u0924 \u0939\u0948\u0902\u0964 \u090f\u0915 \u092e\u093f\u0928\u091f \u092c\u093e\u0926 \u092a\u0941\u0928\u0903 \u092a\u094d\u0930\u092f\u093e\u0938 \u0915\u0930\u0947\u0902\u0964");
                tvResultEnglish.setText("All models are busy. Try again later.");
                if (!chatHistory.isEmpty()) chatHistory.remove(chatHistory.size() - 1);
            });
            return;
        }

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(180, TimeUnit.SECONDS)
                .readTimeout(180, TimeUnit.SECONDS)
                .build();

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

        String url = buildUrl(MODELS[modelIndex]);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    askGeminiRecursive(query, modelIndex + 1);
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseBody = response.body() != null ? response.body().string() : "";
                runOnUiThread(() -> {
                    if (response.code() == 503 || response.code() == 429) {
                        askGeminiRecursive(query, modelIndex + 1);
                        return;
                    }

                    try {
                        if (response.isSuccessful()) {
                            JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();

                            if (!json.has("candidates") || json.getAsJsonArray("candidates").size() == 0) {
                                if (modelIndex + 1 < MODELS.length) {
                                    askGeminiRecursive(query, modelIndex + 1);
                                } else {
                                    tvResultHindi.setText("No response received. Try again.");
                                    tvResultEnglish.setText("No response received.");
                                    if (!chatHistory.isEmpty()) chatHistory.remove(chatHistory.size() - 1);
                                    progressBar.setVisibility(View.GONE);
                                }
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
                            progressBar.setVisibility(View.GONE);

                        } else {
                            if (modelIndex + 1 < MODELS.length) {
                                askGeminiRecursive(query, modelIndex + 1);
                            } else {
                                tvResultHindi.setText("Server Error: " + response.code());
                                tvResultEnglish.setText("Check Quota. Code: " + response.code());
                                if (!chatHistory.isEmpty()) chatHistory.remove(chatHistory.size() - 1);
                                progressBar.setVisibility(View.GONE);
                            }
                        }
                    } catch (Exception e) {
                        if (modelIndex + 1 < MODELS.length) {
                            askGeminiRecursive(query, modelIndex + 1);
                        } else {
                            tvResultHindi.setText("Error parsing response.");
                            tvResultEnglish.setText("Error parsing response.");
                            e.printStackTrace();
                            if (!chatHistory.isEmpty()) chatHistory.remove(chatHistory.size() - 1);
                            progressBar.setVisibility(View.GONE);
                        }
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

    private void shareResult() {
        String hindiVal = tvResultHindi.getText().toString().trim();
        if (hindiVal.isEmpty() || hindiVal.contains("Thinking...") || hindiVal.contains("\u0938\u094b\u091a")) {
            Toast.makeText(this, "Please ask a question first / \u092a\u0939\u0932\u0947 \u092a\u094d\u0930\u0936\u094d\u0928 \u092a\u0942\u091b\u0947\u0902", Toast.LENGTH_SHORT).show();
            return;
        }

        ShareHelper.showShareDialog(this, new ShareHelper.ShareCallback() {
            @Override
            public void onShareText() {
                shareAsText();
            }

            @Override
            public void onSharePdf() {
                shareAsPdf();
            }
        });
    }

    private void shareAsText() {
        String hindiVal = tvResultHindi.getText().toString();
        String englishVal = tvResultEnglish.getText().toString();

        StringBuilder sb = new StringBuilder();
        sb.append("*Jai Kisan - Voice Assistant Report / \u0935\u0949\u092f\u0938 \u0905\u0938\u093f\u0938\u094d\u091f\u0947\u0902\u091f \u0930\u093f\u092a\u094b\u0930\u094d\u091f*\n");
        sb.append("----------------------------------\n\n");

        sb.append("*Hindi Response / \u0939\u093f\u0902\u0926\u0940 \u091c\u0935\u093e\u092c:*\n");
        sb.append(hindiVal).append("\n\n");

        sb.append("*English Response / \u0905\u0902\u0917\u094d\u0930\u0947\u091c\u0940 \u091c\u0935\u093e\u092c:*\n");
        sb.append(englishVal).append("\n\n");

        sb.append("Generated via Jai Kisan App");

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, sb.toString());
        startActivity(Intent.createChooser(shareIntent, "Share via / \u0938\u093e\u091d\u093e \u0915\u0930\u0947\u0902"));
    }

    private void shareAsPdf() {
        String hindiVal = tvResultHindi.getText().toString();
        String englishVal = tvResultEnglish.getText().toString();

        String formattedHindi = hindiVal
                .replaceAll("(?m)^## (.*)$", "<h3>$1</h3>")
                .replaceAll("(?m)^- (.*)$", "<li>$1</li>");
        String formattedEnglish = englishVal
                .replaceAll("(?m)^## (.*)$", "<h3>$1</h3>")
                .replaceAll("(?m)^- (.*)$", "<li>$1</li>");

        String appTitle = LanguageManager.getDual(this, "Jai Kisan", "jai_kisan");
        String reportTitle = LanguageManager.getDual(this, "AI Voice Assistant Chat Summary", "voice_report");
        String metaText = LanguageManager.getDual(this, "AI Conversation Assistant", "ai_advisory");
        String regionalAdviceTitle = LanguageManager.getDual(this, "Regional Advice", "ai_advisory");
        String englishAdviceTitle = "English Advice";
        String footerText = LanguageManager.getRegional(this, "generated_via", "Generated via Jai Kisan App &bull; Supporting Indian Agriculture");

        String htmlContent = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "<meta charset=\"UTF-8\">\n" +
                "<style>\n" +
                "    body { font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; color: #333; margin: 20px; }\n" +
                "    .header { border-bottom: 3px solid #009688; padding-bottom: 10px; margin-bottom: 20px; display: flex; justify-content: space-between; align-items: center; }\n" +
                "    .app-title { color: #009688; font-size: 28px; font-weight: bold; margin: 0; }\n" +
                "    .report-title { font-size: 14px; text-transform: uppercase; color: #666; letter-spacing: 1px; }\n" +
                "    \n" +
                "    .meta-box { background: #F5F5F5; border: 1px solid #E0E0E0; border-radius: 6px; padding: 12px; margin-bottom: 20px; font-size: 13px; }\n" +
                "    .meta-item strong { color: #009688; }\n" +
                "\n" +
                "    .section-title { font-size: 15px; color: #009688; font-weight: bold; margin-top: 20px; margin-bottom: 8px; border-bottom: 1px solid #DDD; padding-bottom: 4px; }\n" +
                "    .content-text { font-size: 13px; line-height: 1.6; margin-bottom: 25px; }\n" +
                "    .content-text ul { margin: 0; padding-left: 18px; }\n" +
                "    .content-text li { margin-bottom: 5px; }\n" +
                "    \n" +
                "    .footer { text-align: center; font-size: 11px; color: #999; margin-top: 40px; border-top: 1px solid #EEE; padding-top: 10px; }\n" +
                "</style>\n" +
                "</head>\n" +
                "<body>\n" +
                "\n" +
                "<div class=\"header\">\n" +
                "    <div>\n" +
                "        <h1 class=\"app-title\">" + appTitle + "</h1>\n" +
                "        <div class=\"report-title\">" + reportTitle + "</div>\n" +
                "    </div>\n" +
                "    <div style=\"text-align: right; font-size: 12px; color: #666;\">Date: " + new java.text.SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new java.util.Date()) + "</div>\n" +
                "</div>\n" +
                "\n" +
                "<div class=\"meta-box\">\n" +
                "    <strong>" + metaText + "</strong>\n" +
                "</div>\n" +
                "\n" +
                (LanguageManager.isEnglishOnly(this) ? "" :
                "<div class=\"section-title\">" + regionalAdviceTitle + "</div>\n" +
                "<div class=\"content-text\">\n" +
                "    " + formattedHindi + "\n" +
                "</div>\n\n") +
                "<div class=\"section-title\">" + englishAdviceTitle + "</div>\n" +
                "<div class=\"content-text\">\n" +
                "    " + formattedEnglish + "\n" +
                "</div>\n" +
                "\n" +
                "<div class=\"footer\">\n" +
                "    " + footerText + "\n" +
                "</div>\n" +
                "\n" +
                "</body>\n" +
                "</html>";

        HtmlToPdfManager.generatePdfFromHtml(this, htmlContent, "JaiKisan_Assistant_Report");
    }
}