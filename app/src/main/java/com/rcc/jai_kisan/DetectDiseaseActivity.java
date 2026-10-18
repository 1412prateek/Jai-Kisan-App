package com.rcc.jai_kisan;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.method.LinkMovementMethod;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.noties.markwon.Markwon;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DetectDiseaseActivity extends BaseActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;

    private static final String[] MODELS = {
        "gemini-2.5-flash",
        "gemini-2.5-flash-lite-preview-06-17",
        "gemini-2.0-flash"
    };

    private ImageView imagePreview;
    private TextView placeholderText;
    private LinearLayout cardResult;
    private View loadingIndicator;
    private MaterialButton btnDetectDisease;
    private Bitmap selectedBitmap = null;
    private String currentResultText = "";
    private View resultPdfContainer;
    private DiseaseResult lastDiseaseResult = null;

    // Results UI Elements
    private TextView tvCropName, tvHealthStatus;
    private CircularHealthScoreView healthScoreView;
    private MaterialCardView cardDiseaseBadge, cardSurvivalChance;
    private ImageView ivDiseaseIcon;
    private TextView tvDiseaseTitle, tvScientificName;
    private LinearLayout layoutDosContainer, layoutDontsContainer, layoutFertilizersContainer;
    private LinearLayout layoutCalculatorButtons;
    private MaterialButton btnFertilizersCalc, btnCostsCalc, btnIrrigationPlanner, btnYieldEstimator;
    private TextView tvSummaryEn, tvSummaryHi;
    private TextView tvSurvivalChancePercent, tvSurvivalChanceLabelEn, tvSurvivalChanceLabelHi;
    private LinearProgressIndicator survivalProgressIndicator;
    private MaterialButton btnGovtScheme;
    private MaterialButton btnFindCenters, btnShareReport;
    private LinearLayout layoutFindCentersShareRow;

    private TextView tvResponseTime;
    private long apiStartTime;

    // Structured DiseaseResult class mirroring the Kotlin definition
    public static class DiseaseResult {
        public final String cropNameEn;
        public final String cropNameHi;
        public final String healthStatusEn;
        public final String healthStatusHi;
        public final int healthScore;
        public final String diseaseNameEn;
        public final String diseaseNameHi;
        public final String scientificName;
        public final String introductionEn;
        public final String introductionHi;
        public final List<String> dosEn;
        public final List<String> dosHi;
        public final List<String> dontsEn;
        public final List<String> dontsHi;
        public final List<String> fertilizersEn;
        public final List<String> fertilizersHi;
        public final int survivalChance;
        public final String summaryEn;
        public final String summaryHi;
        public final String govtSchemeUrl;
        public final String govtSchemeLabelEn;
        public final String govtSchemeLabelHi;

        public DiseaseResult(String cropNameEn, String cropNameHi, String healthStatusEn, String healthStatusHi,
                             int healthScore, String diseaseNameEn, String diseaseNameHi, String scientificName,
                             String introductionEn, String introductionHi, List<String> dosEn, List<String> dosHi,
                             List<String> dontsEn, List<String> dontsHi, List<String> fertilizersEn, List<String> fertilizersHi,
                             int survivalChance, String summaryEn, String summaryHi, String govtSchemeUrl,
                             String govtSchemeLabelEn, String govtSchemeLabelHi) {
            this.cropNameEn = cropNameEn;
            this.cropNameHi = cropNameHi;
            this.healthStatusEn = healthStatusEn;
            this.healthStatusHi = healthStatusHi;
            this.healthScore = healthScore;
            this.diseaseNameEn = diseaseNameEn;
            this.diseaseNameHi = diseaseNameHi;
            this.scientificName = scientificName;
            this.introductionEn = introductionEn;
            this.introductionHi = introductionHi;
            this.dosEn = dosEn;
            this.dosHi = dosHi;
            this.dontsEn = dontsEn;
            this.dontsHi = dontsHi;
            this.fertilizersEn = fertilizersEn;
            this.fertilizersHi = fertilizersHi;
            this.survivalChance = survivalChance;
            this.summaryEn = summaryEn;
            this.summaryHi = summaryHi;
            this.govtSchemeUrl = govtSchemeUrl;
            this.govtSchemeLabelEn = govtSchemeLabelEn;
            this.govtSchemeLabelHi = govtSchemeLabelHi;
        }
    }

    private String buildUrl(String modelName) {
        return "https://generativelanguage.googleapis.com/v1beta/models/" + modelName + ":generateContent?key=" + GeminiKeyManager.getNextKey();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detect_disease);

        // Setup Toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        imagePreview = findViewById(R.id.imagePreview);
        placeholderText = findViewById(R.id.placeholderText);
        cardResult = findViewById(R.id.cardResult);
        loadingIndicator = findViewById(R.id.loadingIndicator);
        btnDetectDisease = findViewById(R.id.btnDetectDisease);

        // Initialize Results Views
        tvCropName = findViewById(R.id.tvCropName);
        tvHealthStatus = findViewById(R.id.tvHealthStatus);
        tvResponseTime = findViewById(R.id.tvResponseTime);
        healthScoreView = findViewById(R.id.healthScoreView);
        cardSurvivalChance = findViewById(R.id.cardSurvivalChance);
        tvSurvivalChanceLabelEn = findViewById(R.id.tvSurvivalChanceLabelEn);
        tvSurvivalChanceLabelHi = findViewById(R.id.tvSurvivalChanceLabelHi);
        cardDiseaseBadge = findViewById(R.id.cardDiseaseBadge);
        ivDiseaseIcon = findViewById(R.id.ivDiseaseIcon);
        tvDiseaseTitle = findViewById(R.id.tvDiseaseTitle);
        tvScientificName = findViewById(R.id.tvScientificName);
        layoutDosContainer = findViewById(R.id.layoutDosContainer);
        layoutDontsContainer = findViewById(R.id.layoutDontsContainer);
        layoutFertilizersContainer = findViewById(R.id.layoutFertilizersContainer);
        layoutCalculatorButtons = findViewById(R.id.layoutCalculatorButtons);
        btnFertilizersCalc = findViewById(R.id.btnFertilizersCalc);
        btnCostsCalc = findViewById(R.id.btnCostsCalc);
        btnIrrigationPlanner = findViewById(R.id.btnIrrigationPlanner);
        btnYieldEstimator = findViewById(R.id.btnYieldEstimator);
        tvSummaryEn = findViewById(R.id.tvSummaryEn);
        tvSummaryHi = findViewById(R.id.tvSummaryHi);
        tvSurvivalChancePercent = findViewById(R.id.tvSurvivalChancePercent);
        survivalProgressIndicator = findViewById(R.id.survivalProgressIndicator);
        btnGovtScheme = findViewById(R.id.btnGovtScheme);
        btnFindCenters = findViewById(R.id.btnFindCenters);
        btnShareReport = findViewById(R.id.btnShareReport);
        layoutFindCentersShareRow = findViewById(R.id.layoutFindCentersShareRow);

        resultPdfContainer = cardResult;

        MaterialButton btnCaptureImage = findViewById(R.id.btnCaptureImage);
        MaterialButton btnUploadImage = findViewById(R.id.btnUploadImage);

        btnCaptureImage.setOnClickListener(v -> {
            try {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(this, LanguageManager.getDual(this, "No camera app found", "crop"), Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, LanguageManager.getDual(this, "Could not open camera", "crop"), Toast.LENGTH_SHORT).show();
            }
        });

        btnUploadImage.setOnClickListener(v -> {
            try {
                Intent pickIntent = new Intent(Intent.ACTION_GET_CONTENT);
                pickIntent.setType("image/*");
                pickIntent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(Intent.createChooser(pickIntent, LanguageManager.getDual(this, "Select Crop Image", "crop")), REQUEST_IMAGE_PICK);
            } catch (ActivityNotFoundException e) {
                try {
                    Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(pickPhoto, REQUEST_IMAGE_PICK);
                } catch (Exception ex) {
                    Toast.makeText(this, LanguageManager.getDual(this, "No photo picker available", "crop"), Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(this, LanguageManager.getDual(this, "Could not open gallery", "crop"), Toast.LENGTH_SHORT).show();
            }
        });

        btnDetectDisease.setOnClickListener(v -> {
            if (selectedBitmap == null) {
                Toast.makeText(this, LanguageManager.getDual(this, "Please select or capture an image first", "no_image_selected"), Toast.LENGTH_SHORT).show();
            } else {
                loadingIndicator.setVisibility(View.VISIBLE);
                btnDetectDisease.setEnabled(false);
                cardResult.setVisibility(View.GONE);
                callGeminiAPI(selectedBitmap);
            }
        });

        btnShareReport.setOnClickListener(v -> shareReport());

        localizeUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        localizeUI();
    }

    private void localizeUI() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(LanguageManager.getDual(this, "Photo Expert", "photo_expert"));
        }
        TextView tvDetectDescription = findViewById(R.id.tvDetectDescription);
        if (tvDetectDescription != null) {
            String enDesc = "Analyze any crop, leaf, or fruit. Capture or upload a photo for an expert report.";
            String regDesc = LanguageManager.getRegional(this, "detect_desc", "किसी भी फसल, पत्ती या फल का विश्लेषण करें। विशेषज्ञ रिपोर्ट पाने के लिए फोटो खींचें या अपलोड करें।");
            tvDetectDescription.setText(LanguageManager.formatLines(this, enDesc, regDesc));
        }
        if (placeholderText != null) {
            placeholderText.setText(LanguageManager.formatLines(this, "No Image Selected", LanguageManager.getRegional(this, "no_image_selected", "कोई छवि चयनित नहीं")));
        }
        MaterialButton btnCapture = findViewById(R.id.btnCaptureImage);
        if (btnCapture != null) {
            btnCapture.setText(LanguageManager.getButtonLines(this, "Capture", "capture"));
        }
        MaterialButton btnUpload = findViewById(R.id.btnUploadImage);
        if (btnUpload != null) {
            btnUpload.setText(LanguageManager.getButtonLines(this, "Upload", "upload"));
        }
        if (btnDetectDisease != null) {
            btnDetectDisease.setText(LanguageManager.getButtonLines(this, "Analyze Photo", "analyze_photo"));
        }
        if (tvSurvivalChanceLabelEn != null) {
            tvSurvivalChanceLabelEn.setText("Survival Chance");
        }
        if (tvSurvivalChanceLabelHi != null) {
            if (LanguageManager.isEnglishOnly(this)) {
                tvSurvivalChanceLabelHi.setVisibility(View.GONE);
            } else {
                tvSurvivalChanceLabelHi.setText(LanguageManager.getRegional(this, "survival_chance", "जीवित रहने की संभावना"));
                tvSurvivalChanceLabelHi.setVisibility(View.VISIBLE);
            }
        }
        TextView tvSummaryLabel = findViewById(R.id.tvSummaryLabel);
        if (tvSummaryLabel != null) {
            tvSummaryLabel.setText(LanguageManager.getDual(this, "Summary", "summary"));
        }
        TextView tvDosLabel = findViewById(R.id.tvDosLabel);
        if (tvDosLabel != null) {
            tvDosLabel.setText(LanguageManager.getDual(this, "Recommended Actions (Do's)", "recommended_actions"));
        }
        TextView tvDontsLabel = findViewById(R.id.tvDontsLabel);
        if (tvDontsLabel != null) {
            tvDontsLabel.setText(LanguageManager.getDual(this, "Things to Avoid (Don'ts)", "things_to_avoid"));
        }
        TextView tvCuresLabel = findViewById(R.id.tvCuresLabel);
        if (tvCuresLabel != null) {
            tvCuresLabel.setText(LanguageManager.getDual(this, "Cures & Fertilizers", "cures_fertilizers"));
        }
        TextView tvApplyToolsLabel = findViewById(R.id.tvApplyToolsLabel);
        if (tvApplyToolsLabel != null) {
            tvApplyToolsLabel.setText(LanguageManager.getDual(this, "Apply to Farming Tools", "apply_to_tools"));
        }
        if (btnFertilizersCalc != null) {
            btnFertilizersCalc.setText(LanguageManager.getButtonLines(this, "Fertilizer Calc", "fertilizer_calc"));
        }
        if (btnCostsCalc != null) {
            btnCostsCalc.setText(LanguageManager.getButtonLines(this, "Cost Calculator", "cost_calc"));
        }
        if (btnIrrigationPlanner != null) {
            btnIrrigationPlanner.setText(LanguageManager.getButtonLines(this, "Irrigation Planner", "irrigation_plan"));
        }
        if (btnYieldEstimator != null) {
            btnYieldEstimator.setText(LanguageManager.getButtonLines(this, "Yield Estimator", "yield_estimator"));
        }
        if (btnFindCenters != null) {
            btnFindCenters.setText(LanguageManager.getButtonLines(this, "Find Centers", "find_centers"));
        }
        if (btnShareReport != null) {
            btnShareReport.setText(LanguageManager.getButtonLines(this, "Share Report", "share_report"));
        }
        if (lastDiseaseResult != null) {
            displayResult(currentResultText);
        }
    }

    private void shareReport() {
        if (lastDiseaseResult == null) {
            Toast.makeText(this, "No report to share / \u093e\u0938\u093e\u091d\u093e \u0915\u0930\u0928\u0947 \u0915\u0947 \u0932\u093f\u090f \u0915\u094b\u090a \u0930\u093f\u092a\u094b\u0930\u094d\u091f \u0928\u0939\u0940\u0902 \u0939\u0948", Toast.LENGTH_SHORT).show();
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
        if (lastDiseaseResult == null) return;
        DiseaseResult res = lastDiseaseResult;

        StringBuilder sb = new StringBuilder();
        sb.append("*").append(LanguageManager.getDual(this, "Jai Kisan - Crop Health Report", "diagnosis_report")).append("*\n");
        sb.append("----------------------------------\n");
        sb.append(LanguageManager.getDual(this, "Crop", "crop")).append(": ").append(LanguageManager.formatDual(this, res.cropNameEn, res.cropNameHi)).append("\n");
        sb.append(LanguageManager.getDual(this, "Status", "health_status")).append(": ").append(LanguageManager.formatDual(this, res.healthStatusEn, res.healthStatusHi)).append("\n");
        sb.append(LanguageManager.getDual(this, "Health Score", "crop")).append(": ").append(res.healthScore).append("/100\n");
        sb.append(LanguageManager.getDual(this, "Survival Chance", "survival_chance")).append(": ").append(res.survivalChance).append("%\n");
        if (res.scientificName != null && !res.scientificName.equals("N/A") && !res.scientificName.isEmpty()) {
            sb.append("Pathogen: ").append(res.scientificName).append("\n");
        }
        sb.append(LanguageManager.getDual(this, "Condition", "detected")).append(": ").append(LanguageManager.formatDual(this, res.diseaseNameEn, res.diseaseNameHi)).append("\n\n");

        sb.append("*").append(LanguageManager.getDual(this, "Introduction", "ai_advisory")).append(":*\n");
        sb.append(res.introductionEn).append("\n");
        if (!LanguageManager.isEnglishOnly(this) && res.introductionHi != null && !res.introductionHi.trim().isEmpty()) {
            sb.append(res.introductionHi).append("\n");
        }
        sb.append("\n");

        if (!res.dosEn.isEmpty()) {
            sb.append("*").append(LanguageManager.getDual(this, "Do's", "recommended_actions")).append(":*\n");
            for (int i = 0; i < Math.min(res.dosEn.size(), res.dosHi.size()); i++) {
                sb.append("• ").append(LanguageManager.formatDual(this, res.dosEn.get(i), res.dosHi.get(i))).append("\n");
            }
            sb.append("\n");
        }

        if (!res.dontsEn.isEmpty()) {
            sb.append("*").append(LanguageManager.getDual(this, "Don'ts", "things_to_avoid")).append(":*\n");
            for (int i = 0; i < Math.min(res.dontsEn.size(), res.dontsHi.size()); i++) {
                sb.append("• ").append(LanguageManager.formatDual(this, res.dontsEn.get(i), res.dontsHi.get(i))).append("\n");
            }
            sb.append("\n");
        }

        if (!res.fertilizersEn.isEmpty()) {
            sb.append("*").append(LanguageManager.getDual(this, "Cures & Treatment", "cures_fertilizers")).append(":*\n");
            for (int i = 0; i < Math.min(res.fertilizersEn.size(), res.fertilizersHi.size()); i++) {
                sb.append("• ").append(LanguageManager.formatDual(this, res.fertilizersEn.get(i), res.fertilizersHi.get(i))).append("\n");
            }
            sb.append("\n");
        }

        sb.append("*").append(LanguageManager.getDual(this, "Summary", "summary")).append(":*\n");
        sb.append(res.summaryEn).append("\n");
        if (!LanguageManager.isEnglishOnly(this) && res.summaryHi != null && !res.summaryHi.trim().isEmpty()) {
            sb.append(res.summaryHi).append("\n");
        }
        sb.append("\n");

        sb.append(LanguageManager.getRegional(this, "generated_via", "Generated via Jai Kisan App"));

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, sb.toString());
        startActivity(Intent.createChooser(shareIntent, LanguageManager.getDual(this, "Share via", "share_report")));
    }

    private void shareAsPdf() {
        if (lastDiseaseResult == null) return;
        DiseaseResult res = lastDiseaseResult;

        StringBuilder dosList = new StringBuilder();
        int dosCount = Math.min(res.dosEn.size(), res.dosHi.size());
        for (int i = 0; i < dosCount; i++) {
            dosList.append("<li>").append(LanguageManager.formatDual(this, res.dosEn.get(i), res.dosHi.get(i))).append("</li>");
        }

        StringBuilder dontsList = new StringBuilder();
        int dontsCount = Math.min(res.dontsEn.size(), res.dontsHi.size());
        for (int i = 0; i < dontsCount; i++) {
            dontsList.append("<li>").append(LanguageManager.formatDual(this, res.dontsEn.get(i), res.dontsHi.get(i))).append("</li>");
        }

        StringBuilder curesList = new StringBuilder();
        int curesCount = Math.min(res.fertilizersEn.size(), res.fertilizersHi.size());
        for (int i = 0; i < curesCount; i++) {
            curesList.append("<li>").append(LanguageManager.formatDual(this, res.fertilizersEn.get(i), res.fertilizersHi.get(i))).append("</li>");
        }

        String appTitle = LanguageManager.getDual(this, "Jai Kisan", "jai_kisan");
        String reportTitle = LanguageManager.getDual(this, "Crop Health Diagnosis Card", "diagnosis_report");
        String cropLabel = LanguageManager.getDual(this, "Crop", "crop");
        String statusLabel = LanguageManager.getDual(this, "Status", "crop");
        String pathogenLabel = "Pathogen";
        String healthScoreLabel = LanguageManager.getDual(this, "Health Score", "crop");
        String survivalChanceLabel = LanguageManager.getDual(this, "Survival Chance", "crop");
        String diagnosisLabel = LanguageManager.getDual(this, "Diagnosis", "ai_advisory");
        String condIntroLabel = LanguageManager.getDual(this, "Crop Condition Introduction", "ai_advisory");
        String dosLabel = LanguageManager.getDual(this, "Do's", "crop");
        String dontsLabel = LanguageManager.getDual(this, "Don'ts", "crop");
        String curesLabel = LanguageManager.getDual(this, "Cures & Treatment", "crop");
        String summaryLabel = LanguageManager.getDual(this, "AI Diagnosis Summary", "ai_advisory");
        String footerText = LanguageManager.getRegional(this, "generated_via", "Generated via Jai Kisan App &bull; Supporting Indian Agriculture");

        String cropVal = LanguageManager.formatDual(this, res.cropNameEn, res.cropNameHi);
        String statusVal = LanguageManager.formatDual(this, res.healthStatusEn, res.healthStatusHi);
        String diseaseVal = LanguageManager.formatDual(this, res.diseaseNameEn, res.diseaseNameHi);

        String htmlContent = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "<meta charset=\"UTF-8\">\n" +
                "<style>\n" +
                "    @page { size: A4; margin: 10mm 8mm; }\n" +
                "    body { font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; color: #333; margin: 0; padding: 0; }\n" +
                "    h1, h2, h3, h4, .section-title { page-break-after: avoid; break-after: avoid; }\n" +
                "    .meta-box, .gauge-container, .badge, .list-box, .list-container, .cures-box, .summary-container, .pdf-disclaimer { page-break-inside: avoid; break-inside: avoid; }\n" +
                "    .header { border-bottom: 3px solid #2E7D32; padding-bottom: 10px; margin-bottom: 20px; display: flex; justify-content: space-between; align-items: center; }\n" +
                "    .app-title { color: #2E7D32; font-size: 28px; font-weight: bold; margin: 0; }\n" +
                "    .report-title { font-size: 14px; text-transform: uppercase; color: #666; letter-spacing: 1px; }\n" +
                "    \n" +
                "    .meta-box { background: #F5F5F5; border: 1px solid #E0E0E0; border-radius: 6px; padding: 12px; margin-bottom: 20px; display: grid; grid-template-columns: repeat(3, 1fr); gap: 10px; }\n" +
                "    .meta-item { font-size: 13px; }\n" +
                "    .meta-item strong { color: #2E7D32; display: block; margin-bottom: 2px; }\n" +
                "\n" +
                "    .gauge-container { display: flex; gap: 20px; margin-bottom: 20px; }\n" +
                "    .gauge-box { flex: 1; background: #FAFAFA; border: 1px solid #E0E0E0; border-radius: 6px; padding: 15px; text-align: center; }\n" +
                "    .gauge-val { font-size: 24px; font-weight: bold; color: #2E7D32; margin-top: 5px; }\n" +
                "    \n" +
                "    .badge { padding: 12px; border-radius: 6px; margin-bottom: 20px; font-size: 14px; border: 1px solid #DDD; }\n" +
                "    .badge-infected { background: #FCE8E6; color: #C62828; border-color: #E6B8B3; }\n" +
                "    .badge-healthy { background: #E8F5E9; color: #2E7D32; border-color: #A3D9A5; }\n" +
                "\n" +
                "    .section-title { font-size: 15px; color: #2E7D32; font-weight: bold; margin-top: 20px; margin-bottom: 8px; border-bottom: 1px solid #DDD; padding-bottom: 4px; }\n" +
                "    .content-text { font-size: 13px; line-height: 1.6; margin-bottom: 15px; }\n" +
                "\n" +
                "    .list-container { display: grid; grid-template-columns: repeat(2, 1fr); gap: 15px; margin-bottom: 20px; }\n" +
                "    .list-box { background: #FAFAFA; border: 1px solid #E0E0E0; border-radius: 6px; padding: 12px; }\n" +
                "    .list-box h4 { font-size: 13px; color: #333; margin: 0 0 8px 0; border-bottom: 1px solid #EEE; padding-bottom: 4px; }\n" +
                "    .list-box ul { margin: 0; padding-left: 18px; font-size: 12px; line-height: 1.5; }\n" +
                "    .list-box li { margin-bottom: 4px; }\n" +
                "    \n" +
                "    .cures-box { background: #E1F5FE; border-left: 4px solid #0288D1; padding: 12px; border-radius: 4px; margin-bottom: 20px; }\n" +
                "    .cures-box h4 { font-size: 13px; color: #01579B; margin: 0 0 8px 0; }\n" +
                "    .cures-box ul { margin: 0; padding-left: 18px; font-size: 12px; line-height: 1.5; }\n" +
                "\n" +
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
                "    <div style=\"text-align: right; font-size: 12px; color: #666;\">Date: " + new java.text.SimpleDateFormat("dd-MM-yyyy", java.util.Locale.getDefault()).format(new java.util.Date()) + "</div>\n" +
                "</div>\n" +
                "\n" +
                "<div class=\"meta-box\">\n" +
                "    <div class=\"meta-item\"><strong>" + cropLabel + "</strong>" + cropVal + "</div>\n" +
                "    <div class=\"meta-item\"><strong>" + statusLabel + "</strong>" + statusVal + "</div>\n" +
                "    <div class=\"meta-item\"><strong>" + pathogenLabel + "</strong>" + (res.scientificName.equals("N/A") ? "N/A" : res.scientificName) + "</div>\n" +
                "</div>\n" +
                "\n" +
                "<div class=\"gauge-container\">\n" +
                "    <div class=\"gauge-box\">\n" +
                "        <div style=\"font-size: 12px; color: #666;\">" + healthScoreLabel + "</div>\n" +
                "        <div class=\"gauge-val\">" + res.healthScore + "/100</div>\n" +
                "    </div>\n" +
                "    <div class=\"gauge-box\">\n" +
                "        <div style=\"font-size: 12px; color: #666;\">" + survivalChanceLabel + "</div>\n" +
                "        <div class=\"gauge-val\" style=\"color: " + (res.survivalChance >= 70 ? "#2E7D32" : (res.survivalChance >= 40 ? "#F59E0B" : "#D32F2F")) + ";\">" + res.survivalChance + "%</div>\n" +
                "    </div>\n" +
                "</div>\n" +
                "\n" +
                "<div class=\"badge " + (res.diseaseNameEn.equalsIgnoreCase("Healthy") || res.diseaseNameEn.equalsIgnoreCase("None") ? "badge-healthy" : "badge-infected") + "\">\n" +
                "    <strong>" + diagnosisLabel + ":</strong> " + diseaseVal + "\n" +
                "</div>\n" +
                "\n" +
                "<div class=\"section-title\">" + condIntroLabel + "</div>\n" +
                "<div class=\"content-text\">\n" +
                "    " + res.introductionEn + "\n" +
                (LanguageManager.isEnglishOnly(this) ? "" : "    <br>" + res.introductionHi + "\n") +
                "</div>\n" +
                "\n" +
                "<div class=\"list-container\">\n" +
                "    <div class=\"list-box\">\n" +
                "        <h4 style=\"color: #2E7D32;\">" + dosLabel + "</h4>\n" +
                "        <ul>" + dosList.toString() + "</ul>\n" +
                "    </div>\n" +
                "    <div class=\"list-box\">\n" +
                "        <h4 style=\"color: #C62828;\">" + dontsLabel + "</h4>\n" +
                "        <ul>" + dontsList.toString() + "</ul>\n" +
                "    </div>\n" +
                "</div>\n" +
                "\n" +
                (curesList.length() > 0 ? 
                "<div class=\"cures-box\">\n" +
                "    <h4>" + curesLabel + "</h4>\n" +
                "    <ul>" + curesList.toString() + "</ul>\n" +
                "</div>\n" : "") +
                "\n" +
                "<div class=\"summary-container\">\n" +
                "    <div class=\"section-title\">" + summaryLabel + "</div>\n" +
                "    <div class=\"content-text\">\n" +
                "        " + res.summaryEn + "\n" +
                (LanguageManager.isEnglishOnly(this) ? "" : "        <br>" + res.summaryHi + "\n") +
                "    </div>\n" +
                "</div>\n" +
                "\n" +
                LanguageManager.getPdfDisclaimerHtml(this) +
                "\n" +
                "<div class=\"footer\">\n" +
                "    " + footerText + "\n" +
                "</div>\n" +
                "\n" +
                "</body>\n" +
                "</html>";

        HtmlToPdfManager.generatePdfFromHtml(this, htmlContent, "JaiKisan_Diagnosis_Report");
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
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
                if (requestCode == REQUEST_IMAGE_CAPTURE) {
                    if (data.getExtras() != null && data.getExtras().get("data") != null) {
                        bitmap = (Bitmap) data.getExtras().get("data");
                    } else if (data.getData() != null) {
                        bitmap = decodeUriToBitmap(data.getData());
                    }
                } else if (requestCode == REQUEST_IMAGE_PICK && data.getData() != null) {
                    Uri selectedImage = data.getData();
                    bitmap = decodeUriToBitmap(selectedImage);
                }

                if (bitmap != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && bitmap.getConfig() == Bitmap.Config.HARDWARE) {
                        bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, false);
                    }
                    selectedBitmap = bitmap;
                    imagePreview.setImageBitmap(bitmap);
                    placeholderText.setVisibility(View.GONE);
                } else {
                    Toast.makeText(this, "Failed to load selected image / छवि लोड करने में विफल", Toast.LENGTH_SHORT).show();
                }
            } catch (Throwable t) {
                t.printStackTrace();
                Toast.makeText(this, "Error loading image / छवि लोड करने में त्रुटि", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private Bitmap decodeUriToBitmap(Uri uri) {
        if (uri == null) return null;
        InputStream is = null;
        try {
            is = getContentResolver().openInputStream(uri);
            if (is == null) return null;

            // First check bounds
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(is, null, options);
            is.close();

            int reqWidth = 1280;
            int reqHeight = 1280;
            int inSampleSize = 1;

            if (options.outHeight > reqHeight || options.outWidth > reqWidth) {
                final int halfHeight = options.outHeight / 2;
                final int halfWidth = options.outWidth / 2;
                while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                    inSampleSize *= 2;
                }
            }

            options.inSampleSize = inSampleSize;
            options.inJustDecodeBounds = false;
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;

            is = getContentResolver().openInputStream(uri);
            if (is == null) return null;
            Bitmap sampledBitmap = BitmapFactory.decodeStream(is, null, options);
            is.close();

            if (sampledBitmap != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && sampledBitmap.getConfig() == Bitmap.Config.HARDWARE) {
                Bitmap softwareBitmap = sampledBitmap.copy(Bitmap.Config.ARGB_8888, false);
                sampledBitmap.recycle();
                return softwareBitmap;
            }

            return sampledBitmap;
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ignored) {}
            }
        }
    }

    private void callGeminiAPI(Bitmap bitmap) {
        apiStartTime = System.currentTimeMillis();
        callGeminiAPI(bitmap, 0);
    }

    private void callGeminiAPI(Bitmap bitmap, int modelIndex) {
        if (modelIndex >= MODELS.length) {
            runOnUiThread(() -> {
                showApiError(-1, "All models are currently busy. Please try again in a minute.\nसभी मॉडल व्यस्त हैं। एक मिनट बाद पुनः प्रयास करें।");
            });
            return;
        }

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();

        Bitmap readyBitmap = bitmap;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && readyBitmap.getConfig() == Bitmap.Config.HARDWARE) {
            readyBitmap = readyBitmap.copy(Bitmap.Config.ARGB_8888, false);
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        readyBitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
        String base64Image = Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP);

        LanguageManager.LangItem langItem = LanguageManager.getSelectedLanguageItem(this);
        boolean isEngOnly = LanguageManager.isEnglishOnly(this);
        String targetLang = isEngOnly ? "English" : langItem.englishName + " (" + langItem.nativeName + ")";

        String prompt = "You are an expert agricultural scientist advising Indian farmers. Analyze the provided crop image.\n\n" +
                "CRITICAL INSTRUCTION: Your ENTIRE response must be a single valid JSON object. Do NOT add any text, markdown code fences, explanation, or preamble before or after the JSON. Output raw JSON only.\n\n" +
                "LANGUAGE RULE: The farmer's selected language is " + targetLang + ".\n" +
                (isEngOnly ? "Generate ALL fields in English only. For the *Hi fields, repeat the English text.\n\n" :
                 "All *En fields must be in English. All *Hi fields MUST be translated accurately into " + targetLang + " (do NOT use Hindi unless the selected language is Hindi).\n\n") +
                "The JSON must match this exact structure:\n" +
                "{\n" +
                "  \"cropNameEn\": \"Crop name in English\",\n" +
                "  \"cropNameHi\": \"Crop name in " + targetLang + "\",\n" +
                "  \"healthStatusEn\": \"One of: Healthy / Mildly Infected / Moderately Infected / Severely Infected / Critical\",\n" +
                "  \"healthStatusHi\": \"Health status in " + targetLang + "\",\n" +
                "  \"healthScore\": <integer 0-100, where 100 = perfectly healthy, 0 = dying>,\n" +
                "  \"diseaseNameEn\": \"Primary disease/condition name in English, or 'Healthy' if none\",\n" +
                "  \"diseaseNameHi\": \"Primary disease name in " + targetLang + "\",\n" +
                "  \"scientificName\": \"Scientific/Latin name of pathogen or 'N/A'\",\n" +
                "  \"introductionEn\": \"2-3 sentence introduction about what you observe in the image in English.\",\n" +
                "  \"introductionHi\": \"2-3 sentence introduction in " + targetLang + ".\",\n" +
                "  \"dosEn\": [\n" +
                "    \"First recommended action in English.\",\n" +
                "    \"Second recommended action in English.\",\n" +
                "    \"Third recommended action in English.\"\n" +
                "  ],\n" +
                "  \"dosHi\": [\n" +
                "    \"First recommended action in " + targetLang + ".\",\n" +
                "    \"Second recommended action in " + targetLang + ".\",\n" +
                "    \"Third recommended action in " + targetLang + ".\"\n" +
                "  ],\n" +
                "  \"dontsEn\": [\n" +
                "    \"First thing to avoid in English.\",\n" +
                "    \"Second thing to avoid in English.\",\n" +
                "    \"Third thing to avoid in English.\"\n" +
                "  ],\n" +
                "  \"dontsHi\": [\n" +
                "    \"First thing to avoid in " + targetLang + ".\",\n" +
                "    \"Second thing to avoid in " + targetLang + ".\",\n" +
                "    \"Third thing to avoid in " + targetLang + ".\"\n" +
                "  ],\n" +
                "  \"fertilizersEn\": [\n" +
                "    \"Fertilizer recommendation 1 in English with dosage.\",\n" +
                "    \"Fertilizer recommendation 2 in English with dosage.\",\n" +
                "    \"Fertilizer recommendation 3 in English with dosage.\"\n" +
                "  ],\n" +
                "  \"fertilizersHi\": [\n" +
                "    \"Fertilizer recommendation 1 in " + targetLang + " with dosage.\",\n" +
                "    \"Fertilizer recommendation 2 in " + targetLang + " with dosage.\",\n" +
                "    \"Fertilizer recommendation 3 in " + targetLang + " with dosage.\"\n" +
                "  ],\n" +
                "  \"survivalChance\": <integer 0-100>,\n" +
                "  \"summaryEn\": \"A comprehensive 3-4 sentence summary in English of the situation and recommended immediate actions.\",\n" +
                "  \"summaryHi\": \"Comprehensive summary in " + targetLang + ".\",\n" +
                "  \"govtSchemeUrl\": \"https://agricoop.gov.in/\",\n" +
                "  \"govtSchemeLabelEn\": \"Most relevant government scheme name for this crop/disease in English\",\n" +
                "  \"govtSchemeLabelHi\": \"Relevant government scheme name in " + targetLang + "\"\n" +
                "}";

        JsonArray parts = new JsonArray();
        JsonObject textPart = new JsonObject();
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

        String url = buildUrl(MODELS[modelIndex]);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    callGeminiAPI(bitmap, modelIndex + 1);
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseBody = response.body().string();
                runOnUiThread(() -> {
                    if (response.code() == 503 || response.code() == 429) {
                        callGeminiAPI(bitmap, modelIndex + 1);
                        return;
                    }

                    try {
                        if (!response.isSuccessful()) {
                            if (modelIndex + 1 < MODELS.length) {
                                callGeminiAPI(bitmap, modelIndex + 1);
                            } else {
                                showApiError(response.code(), "Server Error Code: " + response.code());
                            }
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

                        displayResult(result);

                    } catch (Exception e) {
                        if (modelIndex + 1 < MODELS.length) {
                            callGeminiAPI(bitmap, modelIndex + 1);
                        } else {
                            // If GSON parsing of whole API structure fails, show appropriate message
                            showApiError(-1, "Unexpected server response layout.");
                        }
                    }
                });
            }
        });
    }

    private void displayResult(String rawResult) {
        currentResultText = rawResult;
        DiseaseResult result = parseResponse(rawResult);

        if (result == null) {
            showApiError(-1, "Unable to parse agricultural report.");
            return;
        }
        lastDiseaseResult = result;

        double durationSec = (System.currentTimeMillis() - apiStartTime) / 1000.0;
        String timeEn = String.format(java.util.Locale.ENGLISH, "Response in %.1fs", durationSec);
        String timePattern = LanguageManager.getRegional(this, "response_time", "प्रतिक्रिया समय: %.1f सेकंड");
        String timeHi = String.format(java.util.Locale.getDefault(), timePattern, durationSec);
        tvResponseTime.setText(LanguageManager.formatDual(this, timeEn, timeHi));

        // Restore normal visibility status for all result layouts
        tvCropName.setVisibility(View.VISIBLE);
        tvHealthStatus.setVisibility(View.VISIBLE);
        tvResponseTime.setVisibility(View.VISIBLE);
        healthScoreView.setVisibility(View.VISIBLE);
        if (cardSurvivalChance != null) cardSurvivalChance.setVisibility(View.VISIBLE);
        cardDiseaseBadge.setVisibility(View.VISIBLE);
        layoutDosContainer.setVisibility(View.VISIBLE);
        layoutDontsContainer.setVisibility(View.VISIBLE);
        layoutFertilizersContainer.setVisibility(View.VISIBLE);
        layoutCalculatorButtons.setVisibility(View.VISIBLE);
        tvSummaryEn.setVisibility(View.VISIBLE);
        tvSummaryHi.setVisibility(LanguageManager.isEnglishOnly(this) ? View.GONE : View.VISIBLE);
        tvSurvivalChancePercent.setVisibility(View.VISIBLE);
        survivalProgressIndicator.setVisibility(View.VISIBLE);
        btnGovtScheme.setVisibility(View.VISIBLE);
        layoutFindCentersShareRow.setVisibility(View.VISIBLE);

        // Bind Main Fields
        tvCropName.setText(LanguageManager.formatDual(this, result.cropNameEn, result.cropNameHi));
        tvCropName.setTextColor(Color.BLACK);
        tvHealthStatus.setText(LanguageManager.formatDual(this, result.healthStatusEn, result.healthStatusHi));
        tvHealthStatus.setTextColor(Color.parseColor("#596475"));
        healthScoreView.setScore(result.healthScore);

        // Check if healthy crop
        boolean isHealthy = result.diseaseNameEn.equalsIgnoreCase("Healthy") 
                || result.diseaseNameEn.equalsIgnoreCase("None");

        if (isHealthy) {
            cardDiseaseBadge.setCardBackgroundColor(0xFFE8F5E9); // Greenish background
            cardDiseaseBadge.setStrokeColor(0xFFC8E6C9);
            ivDiseaseIcon.setImageResource(R.drawable.ic_sprout);
            ivDiseaseIcon.setImageTintList(ColorStateList.valueOf(0xFF2E7D32));
            tvDiseaseTitle.setText(LanguageManager.getDual(this, "Healthy", "healthy"));
            tvDiseaseTitle.setTextColor(0xFF2E7D32);
            tvScientificName.setVisibility(View.GONE);
        } else {
            cardDiseaseBadge.setCardBackgroundColor(0xFFFCE8E6); // Reddish background
            cardDiseaseBadge.setStrokeColor(0xFFFFCDD2);
            ivDiseaseIcon.setImageResource(R.drawable.ic_pest);
            ivDiseaseIcon.setImageTintList(ColorStateList.valueOf(0xFFC62828));
            tvDiseaseTitle.setText(LanguageManager.getDual(this, "Detected: ", "detected") + " " + LanguageManager.formatDual(this, result.diseaseNameEn, result.diseaseNameHi));
            tvDiseaseTitle.setTextColor(0xFFC62828);

            if (result.scientificName != null && !result.scientificName.equals("N/A") && !result.scientificName.isEmpty()) {
                tvScientificName.setText(result.scientificName);
                tvScientificName.setTextColor(0xFF8C1D1D);
                tvScientificName.setVisibility(View.VISIBLE);
            } else {
                tvScientificName.setVisibility(View.GONE);
            }
        }

        // Programmatically populate lists
        populateList(layoutDosContainer, result.dosEn, result.dosHi, R.drawable.ic_check_circle, 0xFF2E7D32);
        populateList(layoutDontsContainer, result.dontsEn, result.dontsHi, R.drawable.ic_close, 0xFFC62828);
        populateList(layoutFertilizersContainer, result.fertilizersEn, result.fertilizersHi, R.drawable.ic_sprout, 0xFF005B96);

        // Wire Calculator intents with fallback auto-fills
        btnFertilizersCalc.setOnClickListener(v -> {
            Intent intent = new Intent(this, FertilizerCalculatorActivity.class);
            intent.putExtra("crop_name_en", result.cropNameEn);
            intent.putExtra("crop_name_hi", result.cropNameHi);
            intent.putExtra("disease_name", result.diseaseNameEn);
            
            ArrayList<String> bilingualSuggestions = new ArrayList<>();
            int count = Math.min(result.fertilizersEn.size(), result.fertilizersHi.size());
            for (int i = 0; i < count; i++) {
                bilingualSuggestions.add(LanguageManager.formatDual(this, result.fertilizersEn.get(i), result.fertilizersHi.get(i)));
            }
            if (bilingualSuggestions.isEmpty()) {
                bilingualSuggestions.addAll(result.fertilizersEn);
            }
            intent.putStringArrayListExtra("fertilizer_suggestions", bilingualSuggestions);
            startActivity(intent);
        });

        btnCostsCalc.setOnClickListener(v -> {
            Intent intent = new Intent(this, CostCalculatorActivity.class);
            intent.putExtra("crop_name_en", result.cropNameEn);
            intent.putExtra("crop_name_hi", result.cropNameHi);
            intent.putExtra("disease_name", result.diseaseNameEn);
            intent.putExtra("survival_chance", result.survivalChance);
            intent.putExtra("health_score", result.healthScore);
            startActivity(intent);
        });

        btnIrrigationPlanner.setOnClickListener(v -> {
            Intent intent = new Intent(this, IrrigationPlannerActivity.class);
            intent.putExtra("crop_name_en", result.cropNameEn);
            intent.putExtra("crop_name_hi", result.cropNameHi);
            intent.putExtra("health_score", result.healthScore);
            startActivity(intent);
        });

        btnYieldEstimator.setOnClickListener(v -> {
            Intent intent = new Intent(this, YieldEstimatorActivity.class);
            intent.putExtra("crop_name_en", result.cropNameEn);
            intent.putExtra("crop_name_hi", result.cropNameHi);
            intent.putExtra("survival_chance", result.survivalChance);
            intent.putExtra("health_score", result.healthScore);
            startActivity(intent);
        });

        // Summary Texts
        tvSummaryEn.setText(result.summaryEn);
        if (LanguageManager.isEnglishOnly(this) || result.summaryHi == null || result.summaryHi.trim().isEmpty()) {
            tvSummaryHi.setVisibility(View.GONE);
        } else {
            tvSummaryHi.setText(result.summaryHi);
            tvSummaryHi.setVisibility(View.VISIBLE);
        }

        // Survival Chance Indicators
        tvSurvivalChancePercent.setText(result.survivalChance + "%");
        survivalProgressIndicator.setProgress(result.survivalChance);
        
        int survivalColor;
        if (result.survivalChance >= 70) {
            survivalColor = 0xFF2E7D32;
            if (cardSurvivalChance != null) {
                cardSurvivalChance.setCardBackgroundColor(0xFFF2F9F2);
                cardSurvivalChance.setStrokeColor(0xFFD7ECD9);
            }
            if (survivalProgressIndicator != null) {
                survivalProgressIndicator.setTrackColor(0xFFD7ECD9);
            }
        } else if (result.survivalChance >= 40) {
            survivalColor = 0xFFF59E0B;
            if (cardSurvivalChance != null) {
                cardSurvivalChance.setCardBackgroundColor(0xFFFFF9E6);
                cardSurvivalChance.setStrokeColor(0xFFFFE082);
            }
            if (survivalProgressIndicator != null) {
                survivalProgressIndicator.setTrackColor(0xFFFFECB3);
            }
        } else {
            survivalColor = 0xFFD32F2F;
            if (cardSurvivalChance != null) {
                cardSurvivalChance.setCardBackgroundColor(0xFFFCE8E6);
                cardSurvivalChance.setStrokeColor(0xFFFFCDD2);
            }
            if (survivalProgressIndicator != null) {
                survivalProgressIndicator.setTrackColor(0xFFFFCDD2);
            }
        }
        tvSurvivalChancePercent.setTextColor(survivalColor);
        survivalProgressIndicator.setIndicatorColor(survivalColor);

        // Government Scheme button mapping
        String schemeText = LanguageManager.formatDual(this, result.govtSchemeLabelEn, result.govtSchemeLabelHi);
        btnGovtScheme.setText(schemeText + "\n" + LanguageManager.getDual(this, "Click for more info", "click_for_info"));
        btnGovtScheme.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(result.govtSchemeUrl));
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "Unable to open link.", Toast.LENGTH_SHORT).show();
            }
        });

        // Map centers search query locator
        btnFindCenters.setOnClickListener(v -> {
            try {
                String query = Uri.encode("Krishi Vigyan Kendra near me");
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=" + query));
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "Maps app not found.", Toast.LENGTH_SHORT).show();
            }
        });

        cardResult.setVisibility(View.VISIBLE);
        loadingIndicator.setVisibility(View.GONE);
        btnDetectDisease.setEnabled(true);
    }

    private void populateList(LinearLayout container, List<String> itemsEn, List<String> itemsHi, int iconResId, int iconTint) {
        container.removeAllViews();
        boolean isEngOnly = LanguageManager.isEnglishOnly(this);
        int count = Math.min(itemsEn.size(), itemsHi.size());
        for (int i = 0; i < count; i++) {
            View itemView = getLayoutInflater().inflate(R.layout.item_bullet_point, container, false);
            ImageView ivIcon = itemView.findViewById(R.id.bulletIcon);
            TextView tvEn = itemView.findViewById(R.id.bulletTextEn);
            TextView tvHi = itemView.findViewById(R.id.bulletTextHi);

            ivIcon.setImageResource(iconResId);
            ivIcon.setImageTintList(ColorStateList.valueOf(iconTint));
            tvEn.setText(itemsEn.get(i));
            if (isEngOnly || itemsHi.get(i) == null || itemsHi.get(i).trim().isEmpty()) {
                tvHi.setVisibility(View.GONE);
            } else {
                tvHi.setText(itemsHi.get(i));
                tvHi.setVisibility(View.VISIBLE);
            }

            container.addView(itemView);
        }
    }

    private void showApiError(int code, String message) {
        tvCropName.setText("Error / त्रुटि");
        tvCropName.setTextColor(0xFFD32F2F);
        tvHealthStatus.setText(message != null ? message : "Failed to retrieve analysis.");
        tvHealthStatus.setTextColor(0xFFD32F2F);

        // Hide other structured cards for an elegant fallback display
        healthScoreView.setVisibility(View.GONE);
        if (cardSurvivalChance != null) cardSurvivalChance.setVisibility(View.GONE);
        cardDiseaseBadge.setVisibility(View.GONE);
        layoutDosContainer.setVisibility(View.GONE);
        layoutDontsContainer.setVisibility(View.GONE);
        layoutFertilizersContainer.setVisibility(View.GONE);
        layoutCalculatorButtons.setVisibility(View.GONE);
        tvSummaryEn.setVisibility(View.GONE);
        tvSummaryHi.setVisibility(View.GONE);
        tvSurvivalChancePercent.setVisibility(View.GONE);
        survivalProgressIndicator.setVisibility(View.GONE);
        btnGovtScheme.setVisibility(View.GONE);
        layoutFindCentersShareRow.setVisibility(View.GONE);

        cardResult.setVisibility(View.VISIBLE);
        loadingIndicator.setVisibility(View.GONE);
        btnDetectDisease.setEnabled(true);
    }

    private DiseaseResult parseResponse(String rawText) {
        try {
            String cleaned = rawText.trim();
            if (cleaned.startsWith("```json")) {
                cleaned = cleaned.substring("```json".length());
            }
            if (cleaned.startsWith("```")) {
                cleaned = cleaned.substring("```".length());
            }
            if (cleaned.endsWith("```")) {
                cleaned = cleaned.substring(0, cleaned.length() - 3);
            }
            cleaned = cleaned.trim();

            JsonObject json = JsonParser.parseString(cleaned).getAsJsonObject();

            String cropNameEn = json.has("cropNameEn") ? json.get("cropNameEn").getAsString() : "Unknown";
            String cropNameHi = json.has("cropNameHi") ? json.get("cropNameHi").getAsString() : "अज्ञात";
            String healthStatusEn = json.has("healthStatusEn") ? json.get("healthStatusEn").getAsString() : "Unknown";
            String healthStatusHi = json.has("healthStatusHi") ? json.get("healthStatusHi").getAsString() : "अज्ञात";
            int healthScore = json.has("healthScore") ? json.get("healthScore").getAsInt() : 50;
            String diseaseNameEn = json.has("diseaseNameEn") ? json.get("diseaseNameEn").getAsString() : "Unknown";
            String diseaseNameHi = json.has("diseaseNameHi") ? json.get("diseaseNameHi").getAsString() : "अज्ञात";
            String scientificName = json.has("scientificName") ? json.get("scientificName").getAsString() : "N/A";
            String introductionEn = json.has("introductionEn") ? json.get("introductionEn").getAsString() : "";
            String introductionHi = json.has("introductionHi") ? json.get("introductionHi").getAsString() : "";

            List<String> dosEn = parseJsonStringList(json, "dosEn");
            List<String> dosHi = parseJsonStringList(json, "dosHi");
            List<String> dontsEn = parseJsonStringList(json, "dontsEn");
            List<String> dontsHi = parseJsonStringList(json, "dontsHi");
            List<String> fertilizersEn = parseJsonStringList(json, "fertilizersEn");
            List<String> fertilizersHi = parseJsonStringList(json, "fertilizersHi");

            int survivalChance = json.has("survivalChance") ? json.get("survivalChance").getAsInt() : 50;
            String summaryEn = json.has("summaryEn") ? json.get("summaryEn").getAsString() : "";
            String summaryHi = json.has("summaryHi") ? json.get("summaryHi").getAsString() : "";
            String govtSchemeUrl = json.has("govtSchemeUrl") ? json.get("govtSchemeUrl").getAsString() : "https://agricoop.gov.in/";
            String govtSchemeLabelEn = json.has("govtSchemeLabelEn") ? json.get("govtSchemeLabelEn").getAsString() : "Government Schemes";
            String govtSchemeLabelHi = json.has("govtSchemeLabelHi") ? json.get("govtSchemeLabelHi").getAsString() : "सरकारी योजनाएं";

            return new DiseaseResult(
                    cropNameEn, cropNameHi, healthStatusEn, healthStatusHi, healthScore,
                    diseaseNameEn, diseaseNameHi, scientificName, introductionEn, introductionHi,
                    dosEn, dosHi, dontsEn, dontsHi, fertilizersEn, fertilizersHi,
                    survivalChance, summaryEn, summaryHi, govtSchemeUrl, govtSchemeLabelEn, govtSchemeLabelHi
            );
        } catch (Exception e) {
            return parseMarkdownToDiseaseResult(rawText);
        }
    }

    private List<String> parseJsonStringList(JsonObject json, String key) {
        List<String> list = new ArrayList<>();
        if (json.has(key) && json.get(key).isJsonArray()) {
            JsonArray array = json.getAsJsonArray(key);
            for (int i = 0; i < array.size(); i++) {
                list.add(array.get(i).getAsString());
            }
        }
        return list;
    }

    private DiseaseResult parseMarkdownToDiseaseResult(String markdown) {
        String cropNameEn = "Unknown";
        String cropNameHi = "अज्ञात";
        String healthStatusEn = "Unknown";
        String healthStatusHi = "अज्ञात";
        int healthScore = 50;
        String diseaseNameEn = "Unknown";
        String diseaseNameHi = "अज्ञात";
        String scientificName = "N/A";
        String introductionEn = "";
        String introductionHi = "";
        List<String> dosEn = new ArrayList<>();
        List<String> dosHi = new ArrayList<>();
        List<String> dontsEn = new ArrayList<>();
        List<String> dontsHi = new ArrayList<>();
        List<String> fertilizersEn = new ArrayList<>();
        List<String> fertilizersHi = new ArrayList<>();
        int survivalChance = 50;
        String summaryEn = "";
        String summaryHi = "";
        String govtSchemeUrl = "https://agricoop.gov.in/";
        String govtSchemeLabelEn = "Government Schemes";
        String govtSchemeLabelHi = "सरकारी योजनाएं";

        String[] lines = markdown.split("\n");
        String currentSection = "";

        for (String line : lines) {
            String cleanLine = line.trim();
            if (cleanLine.isEmpty()) continue;

            String lowerLine = cleanLine.toLowerCase();
            if (lowerLine.contains("crop name") || lowerLine.contains("फसल का नाम") || lowerLine.contains("फसल:")) {
                currentSection = "crop_name";
                continue;
            } else if (lowerLine.contains("health status") || lowerLine.contains("स्वास्थ्य स्थिति") || lowerLine.contains("स्वास्थ्य:")) {
                currentSection = "health_status";
                continue;
            } else if (lowerLine.contains("possible disease") || lowerLine.contains("disease") || lowerLine.contains("रोग:")) {
                currentSection = "disease";
                continue;
            } else if (lowerLine.contains("survival chance") || lowerLine.contains("जीवित रहने की संभावना")) {
                currentSection = "survival_chance";
                continue;
            } else if (lowerLine.contains("introduction") || lowerLine.contains("परिचय")) {
                currentSection = "introduction";
                continue;
            } else if (lowerLine.contains("do's") || lowerLine.contains("dos") || lowerLine.contains("क्या करें")) {
                currentSection = "dos";
                continue;
            } else if (lowerLine.contains("don'ts") || lowerLine.contains("donts") || lowerLine.contains("क्या न करें")) {
                currentSection = "donts";
                continue;
            } else if (lowerLine.contains("medicine") || lowerLine.contains("fertilizer") || lowerLine.contains("दवा") || lowerLine.contains("उर्वरक")) {
                currentSection = "fertilizers";
                continue;
            } else if (lowerLine.contains("scheme") || lowerLine.contains("योजना")) {
                currentSection = "schemes";
                continue;
            } else if (lowerLine.contains("summary") || lowerLine.contains("सारांश")) {
                currentSection = "summary";
                continue;
            }

            if (cleanLine.startsWith("-") || cleanLine.startsWith("*")) {
                String content = cleanLine.substring(1).trim();
                if (content.isEmpty()) continue;

                boolean isHi = isHindi(content);

                switch (currentSection) {
                    case "crop_name":
                        if (isHi) cropNameHi = content;
                        else cropNameEn = content;
                        break;
                    case "health_status":
                        if (isHi) healthStatusHi = content;
                        else {
                            healthStatusEn = content;
                            healthScore = inferHealthScore(content);
                        }
                        break;
                    case "disease":
                        if (isHi) diseaseNameHi = content;
                        else {
                            diseaseNameEn = content;
                            if (content.contains("(") && content.contains(")")) {
                                int start = content.indexOf("(");
                                int end = content.indexOf(")");
                                if (end > start) {
                                    scientificName = content.substring(start + 1, end).trim();
                                    diseaseNameEn = content.substring(0, start).trim();
                                }
                            }
                        }
                        break;
                    case "survival_chance":
                        int num = extractNumber(content);
                        if (num != -1) survivalChance = num;
                        break;
                    case "introduction":
                        if (isHi) introductionHi = (introductionHi.isEmpty() ? "" : introductionHi + " ") + content;
                        else introductionEn = (introductionEn.isEmpty() ? "" : introductionEn + " ") + content;
                        break;
                    case "dos":
                        if (isHi) dosHi.add(content);
                        else dosEn.add(content);
                        break;
                    case "donts":
                        if (isHi) dontsHi.add(content);
                        else dontsEn.add(content);
                        break;
                    case "fertilizers":
                        if (isHi) fertilizersHi.add(content);
                        else fertilizersEn.add(content);
                        break;
                    case "schemes":
                        if (content.contains("[") && content.contains("]")) {
                            int startTxt = content.indexOf("[");
                            int endTxt = content.indexOf("]");
                            int startUrl = content.indexOf("(");
                            int endUrl = content.indexOf(")");
                            if (endTxt > startTxt && endUrl > startUrl) {
                                String label = content.substring(startTxt + 1, endTxt).trim();
                                String url = content.substring(startUrl + 1, endUrl).trim();
                                govtSchemeUrl = url;
                                if (isHi) govtSchemeLabelHi = label;
                                else govtSchemeLabelEn = label;
                            }
                        } else {
                            if (isHi) govtSchemeLabelHi = content;
                            else govtSchemeLabelEn = content;
                        }
                        break;
                    case "summary":
                        if (isHi) summaryHi = (summaryHi.isEmpty() ? "" : summaryHi + " ") + content;
                        else summaryEn = (summaryEn.isEmpty() ? "" : summaryEn + " ") + content;
                        break;
                }
            }
        }

        // Ensure lists are not empty
        if (dosEn.isEmpty()) {
            dosEn.add("Monitor crop health daily.");
            dosHi.add("फसल के स्वास्थ्य की दैनिक निगरानी करें।");
        }
        if (dontsEn.isEmpty()) {
            dontsEn.add("Avoid over-watering.");
            dontsHi.add("अत्यधिक सिंचाई से बचें।");
        }
        if (fertilizersEn.isEmpty()) {
            fertilizersEn.add("Apply organic compost.");
            fertilizersHi.add("जैविक खाद का प्रयोग करें।");
        }

        return new DiseaseResult(
                cropNameEn, cropNameHi, healthStatusEn, healthStatusHi, healthScore,
                diseaseNameEn, diseaseNameHi, scientificName, introductionEn, introductionHi,
                dosEn, dosHi, dontsEn, dontsHi, fertilizersEn, fertilizersHi,
                survivalChance, summaryEn, summaryHi, govtSchemeUrl, govtSchemeLabelEn, govtSchemeLabelHi
        );
    }

    private boolean isHindi(String text) {
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c >= 0x0900 && c <= 0x097F) {
                return true;
            }
        }
        return false;
    }

    private int inferHealthScore(String status) {
        String lower = status.toLowerCase();
        if (lower.contains("healthy") || lower.contains("स्वस्थ")) return 90;
        if (lower.contains("mild") || lower.contains("हल्का")) return 70;
        if (lower.contains("moderate") || lower.contains("मध्यम")) return 50;
        if (lower.contains("severe") || lower.contains("गंभीर")) return 30;
        if (lower.contains("critical") || lower.contains("अत्यंत")) return 15;
        return 50;
    }

    private int extractNumber(String text) {
        Matcher m = Pattern.compile("\\d+").matcher(text);
        if (m.find()) {
            try {
                return Integer.parseInt(m.group());
            } catch (NumberFormatException e) {
                return -1;
            }
        }
        return -1;
    }
}
