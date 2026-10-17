package com.rcc.jai_kisan;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.util.Locale;

public class IrrigationPlannerActivity extends BaseActivity {

    private View rootView;
    private AutoCompleteTextView cropDropdown, soilDropdown, methodDropdown;
    private View resultSection;
    private View resultPdfContainer;
    private TextView tvInterval, tvCriticalStage, tvExpertTip;

    private static final String[] CROPS = {
            "Wheat / \u0917\u0947\u0939\u0942\u0902", "Rice / \u092d\u093e\u0928", "Maize / \u092e\u0915\u094d\u0915\u093e", "Sugarcane / \u0917\u0928\u094d\u0928\u093e",
            "Cotton / \u0915\u092a\u093e\u0938", "Soybean / \u0938\u094b\u092f\u093e\u092c\u0940\u0928", "Mustard / \u0938\u0930\u0938\u094b\u0902", "Potato / \u0906\u0932\u0942",
            "Millets / \u092c\u093e\u091c\u0930\u093e", "Pulses / \u0926\u093e\u0932\u0947\u0902"
    };

    private static final String[] SOILS = {
            "Alluvial / \u091c\u0932\u094b\u0922\u093c", "Black / \u0915\u093e\u0932\u0940 \u092e\u093f\u091f\u094d\u091f\u0940", "Red / \u0932\u093e\u0932 \u092e\u093f\u091f\u094d\u091f\u0940",
            "Laterite / \u0932\u0948\u091f\u0947\u0930\u093e\u0907\u091f", "Sandy / \u0930\u0947\u0924\u0940\u0932\u0940 \u092e\u093f\u091f\u094d\u091f\u0940", "Clay / \u091a\u093f\u0915\u0928\u0940 \u092e\u093f\u091f\u094d\u091f\u0940"
    };

    private static final String[] METHODS = {
            "Flood / \u0938\u0924\u0939 \u0938\u093f\u0902\u091a\u093e\u0908", "Drip / \u0921\u094d\u0930\u093f\u092a (\u091f\u092a\u0915)", "Sprinkler / \u092b\u0935\u094d\u0935\u093e\u0930\u093e",
            "Furrow / \u0915\u0942\u0902\u0921\u093c \u0938\u093f\u0902\u091a\u093e\u0908", "Basin / \u0925\u093e\u0932\u093e \u0938\u093f\u0902\u091a\u093e\u0908"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_irrigation_planner);

        rootView = findViewById(R.id.root_view);
        cropDropdown = findViewById(R.id.dropdown_crop);
        soilDropdown = findViewById(R.id.dropdown_soil);
        methodDropdown = findViewById(R.id.dropdown_method);
        resultSection = findViewById(R.id.result_section);
        resultPdfContainer = resultSection;

        tvInterval = findViewById(R.id.tv_interval);
        tvCriticalStage = findViewById(R.id.tv_critical_stage);
        tvExpertTip = findViewById(R.id.tv_expert_tip);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        ImageButton btnClose = findViewById(R.id.btn_close);
        if (btnClose != null) btnClose.setOnClickListener(v -> finish());

        // Adapters
        soilDropdown.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, SOILS));
        methodDropdown.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, METHODS));

        // Dynamic crop dropdown selection and insertion
        String[] defaultCrops = LanguageManager.getLocalizedCrops(this);
        String cropEn = getIntent().getStringExtra("crop_name_en");
        String cropHi = getIntent().getStringExtra("crop_name_hi");

        java.util.List<String> cropList = new java.util.ArrayList<>(java.util.Arrays.asList(defaultCrops));
        String selectedCropString = null;

        if (cropEn != null && !cropEn.trim().isEmpty()) {
            selectedCropString = LanguageManager.getLocalizedCropName(this, cropEn, cropHi);
            boolean found = false;
            for (String c : defaultCrops) {
                if (c.toLowerCase().contains(cropEn.trim().toLowerCase())) {
                    selectedCropString = c;
                    found = true;
                    break;
                }
            }
            if (!found) {
                cropList.add(selectedCropString);
            }
        }

        cropDropdown.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, cropList.toArray(new String[0])));

        if (selectedCropString != null) {
            cropDropdown.setText(selectedCropString, false);
        }

        findViewById(R.id.btn_calculate_irrigation).setOnClickListener(v -> calculateIrrigation());
        findViewById(R.id.btn_share).setOnClickListener(v -> shareSchedule());
        // AdMob banner
        AdManager.loadBanner(this, (android.widget.FrameLayout) findViewById(R.id.adBannerContainer));

        // Keep fields empty for user selection. No auto-fill or auto-execute.
    }

    private void calculateIrrigation() {
        String crop = cropDropdown.getText().toString();
        String soil = soilDropdown.getText().toString();
        String method = methodDropdown.getText().toString();

        if (crop.isEmpty() || soil.isEmpty() || method.isEmpty()) {
            Snackbar.make(rootView, "Please fill all fields / \u0915\u094d\u0930\u092a\u092f\u093e \u092a\u0942\u0930\u0940 \u091c\u093e\u0928\u0915\u093e\u0930\u0940 \u0926\u0947\u0902", Snackbar.LENGTH_SHORT).show();
            return;
        }

        // 1. Calculate Base Interval (Days) based on Soil Type
        int interval = 10;
        if (soil.contains("Sandy")) interval = 7;      // Sandy dries fast
        else if (soil.contains("Clay")) interval = 18;  // Clay holds water
        else if (soil.contains("Black")) interval = 15; // Black soil holds moisture well

        // 2. Adjust based on Method
        if (method.contains("Drip")) interval -= 3; // Drip needs very frequent small amounts
        else if (method.contains("Sprinkler")) interval -= 1;

        // 3. Define Bilingual Critical Growth Stages and Tips
        String critical;
        String tip;

        if (crop.contains("Wheat")) {
            critical = "CRI Stage (21 days) & Flowering.\n\u0939\u0930 \u091a\u0930\u0923 (21 \u0926\u093f\u0928) \u0914\u0930 \u092b\u0942\u0932 \u0906\u0928\u0947 \u0915\u093e \u0938\u092e\u092f\u0964";
            tip = "Avoid water stress during the CRI stage to ensure root growth.\n\u091c\u092c\u0924\u0915 \u091c\u095c\u094b\u0902 \u0915\u0947 \u0935\u093f\u0915\u093e\u0938 \u0915\u0947 \u0932\u093f\u090f \u0938\u0940\u0906\u0930\u0906\u0908 \u091a\u0930\u0923 \u0915\u0947 \u0926\u094c\u0930\u093e\u0928 \u092a\u093e\u0928\u0940 \u0915\u0940 \u0915\u092e\u0940 \u092a\u0930 \u0927\u094d\u092f\u093e\u0928 \u0926\u0947\u0902\u0964";
        } else if (crop.contains("Rice")) {
            interval = 4; // Paddy needs frequent water
            critical = "Tillering to Grain Filling.\n\u0915\u0932\u094d\u0932\u0947 \u092b\u0942\u091f\u0928\u0947 \u0938\u0947 \u0926\u093e\u0928\u093e \u092d\u0930\u0928\u0947 \u0924\u0915\u0964";
            tip = "Keep 5cm water level for flood method.\n\u0938\u0924\u0939 \u0938\u093f\u0902\u091a\u093e\u0908 \u0915\u0947 \u0932\u093f\u090f \u096b \u0938\u0947\u092e\u0940 \u092a\u093e\u0928\u0940 \u0915\u093e \u0938\u094d\u0924\u0930 \u092c\u0928\u093e\u090f \u0930\u0916\u0947\u0902\u0964";
        } else if (crop.contains("Sugarcane")) {
            interval += 2;
            critical = "Formative Stage (60-130 Days).\n\u092a\u094d\u0930\u093e\u0930\u0902\u092d\u093f\u0915 \u0935\u093f\u0915\u093e\u0938 \u091a\u0930\u0923 (\u096c\u0962-\u0967\u0969\u0962 \u0926\u093f\u0928)\u0964";
            tip = "Deep irrigation is required for long intervals.\n\u0932\u0902\u092c\u0947 \u0905\u0902\u0924\u0930\u093e\u0932 \u0915\u0947 \u0932\u093f\u090f \u0917\u0939\u0930\u0940 \u0938\u093f\u0902\u091a\u093e\u0908 \u0915\u094d\u0930\u0940 \u0906\u0935\u0936\u094d\u092f\u0915\u0924\u093e \u0939\u094b\u0924\u0940 \u0939\u0948\u0964";
        } else if (crop.contains("Potato")) {
            interval = Math.max(5, interval - 2);
            critical = "Stolon formation & Tuberization.\n\u0938\u094d\u091f\u094b\u0932\u0928 \u092c\u0928\u0928\u093e \u0914\u0930 \u0915\u0902\u0926 \u092c\u0928\u0928\u093e\u0964";
            tip = "Light but frequent irrigation is best for tubers.\n\u0915\u0902\u0926\u094b\u0902 \u0915\u0947 \u0932\u093f\u090f \u0939\u0932\u094d\u0915\u0940 \u0932\u0947\u0915\u093f\u0928 \u092c\u093e\u0930-\u092c\u093e\u0930 \u0938\u093f\u0902\u091a\u093e\u0908 \u0938\u092c\u0938\u0947 \u0905\u091a\u094d\u091b\u0940 \u0939\u094b\u0924\u0940 \u0939\u0948\u0964";
        } else {
            critical = "Active Vegetative Growth.\n\u0938\u0915\u094d\u0930\u093f\u092f \u0935\u0928\u0938\u094d\u092a\u0924\u093f \u0935\u093f\u0915\u093e\u0938 \u0915\u093e \u091a\u0930\u0923\u0964";
            tip = "Monitor soil moisture at 15cm depth.\n\u0967\u096b \u0938\u0947\u092e\u0940 \u0915\u0940 \u0917\u0939\u0930\u093e\u0938\u094d \u092a\u0930 \u092e\u093f\u091f\u094d\u091f\u0940 \u0915\u0940 \u0928\u092e\u0940 \u0915\u0940 \u0928\u093f\u0917\u0930\u093e\u0928\u0940 \u0915\u0930\u0947\u0902\u0964";
        }

        // Add real-time AI insights based on crop health
        int healthScore = getIntent().getIntExtra("health_score", -1);
        if (healthScore != -1) {
            String aiRecommendation = "\n\n\uD83E\uDDFE Real-time AI Health Recommendation:\nCrop health score is " + healthScore + "/100. "
                    + (healthScore < 60 ? "Reduce water quantity per cycle but irrigate more frequently to avoid root stress and fungus spread." : "Crop is in stable condition. Continue normal watering schedule.")
                    + "\n\n\uD83E\uDDFE \u0935\u093e\u0938\u094d\u0924\u0935\u093f\u0915 \u0938\u092e\u092f \u090f\u0906\u0908 \u0938\u093f\u0902\u091a\u093e\u0908 \u0938\u0932\u093e\u0939:\n\u092b\u0938\u0932 \u0915\u093e \u0938\u094d\u0935\u093e\u0938\u094d\u0925\u094d\u092f \u0938\u094d\u0915\u094b\u0930 " + healthScore + "/100 \u0939\u0948\u0964 "
                    + (healthScore < 60 ? "\u091c\u095c \u0924\u0928\u093e\u0935 \u0914\u0930 \u0915\u0935\u0915 \u092b\u0948\u0932\u0928\u0947 \u0938\u0947 \u092c\u091a\u0928\u0947 \u0915\u0930\u0947\u0902 \u092a\u093e\u0928\u0940 \u0915\u0940 \u092e\u093e\u0924\u094d\u0930\u093e \u0915\u092e \u0915\u0930\u0947\u0902 \u0932\u0947\u0915\u093f\u0928 \u0905\u0927\u093f\u0915 \u092e\u093e\u0924\u094d\u0930\u093e \u092e\u0947\u0902 \u0938\u093f\u0902\u091a\u093e\u0908 \u0915\u0930\u0947\u0902\u0964" : "\u092b\u0938\u0932 \u0938\u094d\u0925\u093f\u0930 \u0938\u094d\u0925\u093f\u0924\u093f \u092e\u0947\u0902 \u0939\u0948\u0964 \u0938\u093e\u092e\u093e\u0928\u094d\u092f \u0938\u093f\u0902\u091a\u093e\u0908 \u091c\u093e\u0930\u0940 \u0930\u0916\u0947\u0902\u0964") + ")";
            tip = tip + aiRecommendation;
        }

        displayResults(interval, critical, tip);
    }

    private void displayResults(int interval, String critical, String tip) {
        tvInterval.setText(String.format(Locale.getDefault(),
                "Irrigation Interval: Every %d days\n\u0938\u093f\u0902\u091a\u093e\u0908 \u0915\u093e \u0905\u0902\u0924\u0930\u093e\u0932: \u0939\u0930 %d \u0926\u093f\u0928\u094b\u0902 \u092e\u0947\u0902", interval, interval));

        tvCriticalStage.setText("Critical Stage / \u092e\u0939\u0924\u094d\u0935\u092a\u0942\u0930\u094d\u0923 \u091a\u0930\u0923:\n" + critical);

        tvExpertTip.setText("Expert Tip / \u0935\u093f\u0936\u0947\u0937\u091c\u094d\u091e \u0938\u0932\u093e\u0939:\n" + tip);

        resultSection.setVisibility(View.VISIBLE);
        resultSection.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
    }

    private void shareSchedule() {
        if (resultSection.getVisibility() != View.VISIBLE) {
            Snackbar.make(rootView, "Please plan first / पहले योजना बनाएं", Snackbar.LENGTH_SHORT).show();
            return;
        }

        AdManager.onPdfExportClick(this, () -> ShareHelper.showShareDialog(this, new ShareHelper.ShareCallback() {
            @Override
            public void onShareText() {
                shareAsText();
            }

            @Override
            public void onSharePdf() {
                shareAsPdf();
            }
        }));
    }

    private void shareAsText() {
        String crop = cropDropdown.getText().toString();
        String soil = soilDropdown.getText().toString();
        String method = methodDropdown.getText().toString();

        StringBuilder sb = new StringBuilder();
        sb.append("*").append(LanguageManager.getDual(this, "Jai Kisan - Irrigation Plan", "irrigation_report")).append("*\n");
        sb.append("----------------------------------\n");
        sb.append(LanguageManager.getDual(this, "Crop", "crop")).append(": ").append(crop).append("\n");
        sb.append(LanguageManager.getDual(this, "Soil Type", "soil_type")).append(": ").append(soil).append("\n");
        sb.append(LanguageManager.getDual(this, "Irrigation Method", "irrigation_method")).append(": ").append(method).append("\n\n");

        sb.append("*").append(LanguageManager.getDual(this, "Irrigation Schedule", "irrigation_report")).append(":*\n");
        sb.append("- ").append(tvInterval.getText().toString()).append("\n\n");

        sb.append("*").append(LanguageManager.getDual(this, "Critical Growth Stage", "critical_stage")).append(":*\n");
        sb.append(tvCriticalStage.getText().toString()).append("\n\n");

        sb.append("*").append(LanguageManager.getDual(this, "Expert Advice", "expert_tip")).append(":*\n");
        sb.append(tvExpertTip.getText().toString()).append("\n\n");

        sb.append(LanguageManager.getRegional(this, "generated_via", "Generated via Jai Kisan App"));

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, sb.toString());
        startActivity(Intent.createChooser(shareIntent, LanguageManager.getDual(this, "Share via", "share_report")));
    }

    private void shareAsPdf() {
        String crop = cropDropdown.getText().toString();
        String soil = soilDropdown.getText().toString();
        String method = methodDropdown.getText().toString();

        String appTitle = LanguageManager.getDual(this, "Jai Kisan", "jai_kisan");
        String reportTitle = LanguageManager.getDual(this, "Irrigation Schedule Report", "irrigation_report");
        String cropLabel = LanguageManager.getDual(this, "Crop", "crop");
        String soilLabel = LanguageManager.getDual(this, "Soil Type", "soil_type");
        String methodLabel = LanguageManager.getDual(this, "Irrigation Method", "irrigation_method");
        String criticalStageLabel = LanguageManager.getDual(this, "Critical Growth Stage", "critical_stage");
        String expertAdvisoryLabel = LanguageManager.getDual(this, "Expert Advisory & AI Insights", "ai_advisory");
        String footerText = LanguageManager.getRegional(this, "generated_via", "Generated via Jai Kisan App &bull; Supporting Indian Agriculture");

        String htmlContent = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "<meta charset=\"UTF-8\">\n" +
                "<style>\n" +
                "    body { font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; color: #333; margin: 20px; }\n" +
                "    .header { border-bottom: 3px solid #0288D1; padding-bottom: 10px; margin-bottom: 20px; display: flex; justify-content: space-between; align-items: center; }\n" +
                "    .app-title { color: #0288D1; font-size: 28px; font-weight: bold; margin: 0; }\n" +
                "    .report-title { font-size: 14px; text-transform: uppercase; color: #666; letter-spacing: 1px; }\n" +
                "    \n" +
                "    .meta-box { background: #F5F5F5; border: 1px solid #E0E0E0; border-radius: 6px; padding: 12px; margin-bottom: 20px; display: grid; grid-template-columns: repeat(3, 1fr); gap: 10px; }\n" +
                "    .meta-item { font-size: 13px; }\n" +
                "    .meta-item strong { color: #0288D1; display: block; margin-bottom: 2px; }\n" +
                "\n" +
                "    .summary-box { background: #E1F5FE; border-left: 4px solid #0288D1; padding: 15px; border-radius: 4px; margin-bottom: 25px; font-size: 14px; font-weight: bold; color: #01579B; }\n" +
                "    \n" +
                "    .details-table { width: 100%; border-collapse: collapse; margin-bottom: 20px; }\n" +
                "    .details-table th { background: #0288D1; color: white; text-align: left; padding: 10px; font-size: 14px; }\n" +
                "    .details-table td { border: 1px solid #E0E0E0; padding: 12px; font-size: 13px; vertical-align: top; line-height: 1.5; }\n" +
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
                "    <div class=\"meta-item\"><strong>" + cropLabel + "</strong>" + crop + "</div>\n" +
                "    <div class=\"meta-item\"><strong>" + soilLabel + "</strong>" + soil + "</div>\n" +
                "    <div class=\"meta-item\"><strong>" + methodLabel + "</strong>" + method + "</div>\n" +
                "</div>\n" +
                "\n" +
                "<div class=\"summary-box\">\n" +
                "    " + tvInterval.getText().toString().replace("\n", "<br>") + "\n" +
                "</div>\n" +
                "\n" +
                "<table class=\"details-table\">\n" +
                "    <thead>\n" +
                "        <tr>\n" +
                "            <th style=\"width: 50%;\">" + criticalStageLabel + "</th>\n" +
                "            <th style=\"width: 50%;\">" + expertAdvisoryLabel + "</th>\n" +
                "        </tr>\n" +
                "    </thead>\n" +
                "    <tbody>\n" +
                "        <tr>\n" +
                "            <td style=\"white-space: pre-line;\">" + tvCriticalStage.getText().toString() + "</td>\n" +
                "            <td style=\"white-space: pre-line;\">" + tvExpertTip.getText().toString() + "</td>\n" +
                "        </tr>\n" +
                "    </tbody>\n" +
                "</table>\n" +
                "\n" +
                "<div class=\"footer\">\n" +
                "    " + footerText + "\n" +
                "</div>\n" +
                "\n" +
                "</body>\n" +
                "</html>";

        HtmlToPdfManager.generatePdfFromHtml(this, htmlContent, "JaiKisan_Irrigation_Report");
    }
}
