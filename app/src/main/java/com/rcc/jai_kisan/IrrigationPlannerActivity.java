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
    private MaterialToolbar toolbar;
    private TextView tvTitleIrrigationPro, tvTitleSelectCrop, tvTitleSelectSoil, tvTitleSelectMethod, tvTitleIrrigationAdvice, tvDisclaimer;
    private com.google.android.material.textfield.TextInputLayout layoutDropdownCrop, layoutDropdownSoil, layoutDropdownMethod;
    private com.google.android.material.button.MaterialButton btnCalculateIrrigation, btnShare;

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

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        ImageButton btnClose = findViewById(R.id.btn_close);
        if (btnClose != null) btnClose.setOnClickListener(v -> finish());

        tvTitleIrrigationPro = findViewById(R.id.tv_title_irrigation_pro);
        tvTitleSelectCrop = findViewById(R.id.tv_title_select_crop);
        layoutDropdownCrop = findViewById(R.id.layout_dropdown_crop);
        tvTitleSelectSoil = findViewById(R.id.tv_title_select_soil);
        layoutDropdownSoil = findViewById(R.id.layout_dropdown_soil);
        tvTitleSelectMethod = findViewById(R.id.tv_title_select_method);
        layoutDropdownMethod = findViewById(R.id.layout_dropdown_method);
        btnCalculateIrrigation = findViewById(R.id.btn_calculate_irrigation);
        tvTitleIrrigationAdvice = findViewById(R.id.tv_title_irrigation_advice);
        btnShare = findViewById(R.id.btn_share);
        tvDisclaimer = findViewById(R.id.tv_disclaimer);

        // Adapters
        String[] soils = LanguageManager.getLocalizedSoils(this);
        String[] methods = LanguageManager.getLocalizedMethods(this);
        soilDropdown.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, soils));
        methodDropdown.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, methods));

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

        if (btnCalculateIrrigation != null) btnCalculateIrrigation.setOnClickListener(v -> calculateIrrigation());
        if (btnShare != null) btnShare.setOnClickListener(v -> shareSchedule());
        // AdMob banner
        AdManager.loadBanner(this, (android.widget.FrameLayout) findViewById(R.id.adBannerContainer));

        localizeUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        localizeUI();
    }

    private void localizeUI() {
        if (toolbar != null) {
            toolbar.setTitle(LanguageManager.getDual(this, "Irrigation Planner", "irrigation_planner"));
        }
        if (tvTitleIrrigationPro != null) {
            tvTitleIrrigationPro.setText(LanguageManager.getDual(this, "Irrigation Pro", "irrigation_planner"));
        }
        if (tvTitleSelectCrop != null) {
            tvTitleSelectCrop.setText("1. " + LanguageManager.getDual(this, "Select Crop", "select_crop"));
        }
        if (layoutDropdownCrop != null) {
            layoutDropdownCrop.setHint(LanguageManager.getDual(this, "Crop Type", "crop"));
        }
        if (tvTitleSelectSoil != null) {
            tvTitleSelectSoil.setText("2. " + LanguageManager.getDual(this, "Select Soil", "soil_type"));
        }
        if (layoutDropdownSoil != null) {
            layoutDropdownSoil.setHint(LanguageManager.getDual(this, "Soil Type", "soil_type"));
        }
        if (tvTitleSelectMethod != null) {
            tvTitleSelectMethod.setText("3. " + LanguageManager.getDual(this, "Method", "irrigation_method"));
        }
        if (layoutDropdownMethod != null) {
            layoutDropdownMethod.setHint(LanguageManager.getDual(this, "Method", "irrigation_method"));
        }
        if (btnCalculateIrrigation != null) {
            btnCalculateIrrigation.setText(LanguageManager.getDual(this, "Plan Irrigation", "generate_plan"));
        }
        if (tvTitleIrrigationAdvice != null) {
            tvTitleIrrigationAdvice.setText(LanguageManager.getDual(this, "Irrigation Advice", "irrigation_report"));
        }
        if (btnShare != null) {
            btnShare.setText(LanguageManager.getDual(this, "Share Schedule", "share_report"));
        }
        if (tvDisclaimer != null) {
            if (LanguageManager.isEnglishOnly(this)) {
                tvDisclaimer.setText("* Disclaimer: These results are automated suggestions based on general data. Actual field conditions vary. Consult a local agriculture specialist before making final decisions.");
            } else {
                tvDisclaimer.setText("* Disclaimer: These results are automated suggestions based on general data. Actual field conditions vary. Consult a local agriculture specialist before making final decisions.\n\n"
                        + LanguageManager.getRegional(this, "disclaimer_body", "अस्वीकरण: ये परिणाम सामान्य डेटा पर आधारित स्वचालित सुझाव हैं। वास्तविक स्थिति भिन्न हो सकती है।"));
            }
        }
    }

    private void calculateIrrigation() {
        String crop = cropDropdown.getText().toString();
        String soil = soilDropdown.getText().toString();
        String method = methodDropdown.getText().toString();

        if (crop.isEmpty() || soil.isEmpty() || method.isEmpty()) {
            Snackbar.make(rootView, LanguageManager.getDual(this, "Please fill all fields", "select_crop"), Snackbar.LENGTH_SHORT).show();
            return;
        }

        // 1. Calculate Base Interval (Days) based on Soil Type
        int interval = 10;
        String soilLower = soil.toLowerCase();
        if (soilLower.contains("sandy")) interval = 7;
        else if (soilLower.contains("clay")) interval = 18;
        else if (soilLower.contains("black")) interval = 15;

        // 2. Adjust based on Method
        String methodLower = method.toLowerCase();
        if (methodLower.contains("drip")) interval -= 3;
        else if (methodLower.contains("sprinkler")) interval -= 1;

        // 3. Define Dynamic Critical Growth Stages and Tips
        String critical = LanguageManager.getLocalizedIrrigationStage(this, crop);
        String tip = LanguageManager.getLocalizedIrrigationTip(this, crop);

        // Add real-time AI insights based on crop health
        int healthScore = getIntent().getIntExtra("health_score", -1);
        if (healthScore != -1) {
            if (LanguageManager.isEnglishOnly(this)) {
                String aiRec = "\n\n🤖 Real-time AI Health Recommendation:\nCrop health score is " + healthScore + "/100. "
                        + (healthScore < 60 ? "Reduce water quantity per cycle but irrigate more frequently to avoid root stress and fungus spread." : "Crop is in stable condition. Continue normal watering schedule.");
                tip = tip + aiRec;
            } else {
                String aiRec = "\n\n🤖 Real-time AI Health Recommendation:\nCrop health score is " + healthScore + "/100. "
                        + (healthScore < 60 ? "Reduce water quantity per cycle but irrigate more frequently to avoid root stress and fungus spread." : "Crop is in stable condition. Continue normal watering schedule.")
                        + "\n(" + LanguageManager.getRegional(this, "ai_advisory", "एआई सिंचाई सलाह") + ": " + healthScore + "/100)";
                tip = tip + aiRec;
            }
        }

        displayResults(interval, critical, tip);
    }

    private void displayResults(int interval, String critical, String tip) {
        String intervalText;
        if (LanguageManager.isEnglishOnly(this)) {
            intervalText = String.format(Locale.getDefault(), "Irrigation Interval: Every %d days", interval);
        } else {
            intervalText = String.format(Locale.getDefault(), "Irrigation Interval: Every %d days\n%s: %d %s",
                    interval,
                    LanguageManager.getRegional(this, "irrigation_interval", "सिंचाई का अंतराल"),
                    interval,
                    LanguageManager.getRegional(this, "days", "दिन"));
        }
        tvInterval.setText(intervalText);

        tvCriticalStage.setText(LanguageManager.getDual(this, "Critical Stage", "critical_stage") + ":\n" + critical);

        tvExpertTip.setText(LanguageManager.getDual(this, "Expert Tip", "expert_tip") + ":\n" + tip);

        resultSection.setVisibility(View.VISIBLE);
        resultSection.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
    }

    private void shareSchedule() {
        if (resultSection.getVisibility() != View.VISIBLE) {
            Snackbar.make(rootView, LanguageManager.getDual(this, "Please plan first", "generate_plan"), Snackbar.LENGTH_SHORT).show();
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
