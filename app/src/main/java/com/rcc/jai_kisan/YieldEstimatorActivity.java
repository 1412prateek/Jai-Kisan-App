package com.rcc.jai_kisan;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class YieldEstimatorActivity extends BaseActivity {

    private View rootView;
    private AutoCompleteTextView cropDropdown, stateDropdown, unitDropdown;
    private TextInputEditText areaInput;
    private View resultSection;
    private View resultPdfContainer;
    private TextView tvYieldPerAcre, tvTotalYield, tvYieldSummary;
    private MaterialToolbar toolbar;
    private TextView tvTitleYieldPro, tvTitleSelectCrop, tvTitleSelectState, tvTitleLandArea, tvTitleEstimatedYield, tvDisclaimer;
    private com.google.android.material.textfield.TextInputLayout layoutDropdownCrop, layoutDropdownState, layoutDropdownUnit, layoutInputArea;
    private com.google.android.material.button.MaterialButton btnEstimate, btnShare;

    // Mapping logic matching Fertilizer Calculator
    private final Map<String, Map<String, Double>> regionUnitMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yield_estimator);

        initializeRegionalUnits();

        rootView = findViewById(R.id.root_view);
        cropDropdown = findViewById(R.id.dropdown_crop);
        stateDropdown = findViewById(R.id.dropdown_state);
        unitDropdown = findViewById(R.id.dropdown_unit);
        areaInput = findViewById(R.id.input_area);
        resultSection = findViewById(R.id.result_section);
        resultPdfContainer = resultSection;

        tvYieldPerAcre = findViewById(R.id.tv_yield_per_acre);
        tvTotalYield = findViewById(R.id.tv_total_yield);
        tvYieldSummary = findViewById(R.id.tv_yield_summary);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        ImageButton btnClose = findViewById(R.id.btn_close);
        if (btnClose != null) btnClose.setOnClickListener(v -> finish());

        tvTitleYieldPro = findViewById(R.id.tv_title_yield_pro);
        tvTitleSelectCrop = findViewById(R.id.tv_title_select_crop);
        layoutDropdownCrop = findViewById(R.id.layout_dropdown_crop);
        tvTitleSelectState = findViewById(R.id.tv_title_select_state);
        layoutDropdownState = findViewById(R.id.layout_dropdown_state);
        tvTitleLandArea = findViewById(R.id.tv_title_land_area);
        layoutDropdownUnit = findViewById(R.id.layout_dropdown_unit);
        layoutInputArea = findViewById(R.id.layout_input_area);
        btnEstimate = findViewById(R.id.btn_estimate);
        tvTitleEstimatedYield = findViewById(R.id.tv_title_estimated_yield);
        btnShare = findViewById(R.id.btn_share);
        tvDisclaimer = findViewById(R.id.tv_disclaimer);

        // Adapters
        String[] defaultStates = LanguageManager.getLocalizedStates(this);
        stateDropdown.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, defaultStates));

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
        } else if (!cropList.isEmpty()) {
            cropDropdown.setText(cropList.get(0), false);
        }

        // Regional logic trigger
        if (defaultStates.length > 0) {
            stateDropdown.setText(defaultStates[0], false);
            loadUnitsForState(defaultStates[0]);
        }
        stateDropdown.setOnItemClickListener((parent, view, position, id) -> {
            String selectedState = defaultStates[position];
            loadUnitsForState(selectedState);
        });

        if (btnEstimate != null) btnEstimate.setOnClickListener(v -> calculateYield());
        if (btnShare != null) btnShare.setOnClickListener(v -> shareResult());

        localizeUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        localizeUI();
    }

    private void localizeUI() {
        if (toolbar != null) {
            toolbar.setTitle(LanguageManager.getDual(this, "Yield Estimator", "yield_estimator"));
        }
        if (tvTitleYieldPro != null) {
            tvTitleYieldPro.setText(LanguageManager.getDual(this, "Yield Pro", "yield_estimator"));
        }
        if (tvTitleSelectCrop != null) {
            tvTitleSelectCrop.setText("1. " + LanguageManager.getDual(this, "Select Crop", "select_crop"));
        }
        if (cropDropdown != null) {
            cropDropdown.setHint(null);
        }
        if (layoutDropdownCrop != null) {
            layoutDropdownCrop.setHint(null);
        }
        if (tvTitleSelectState != null) {
            tvTitleSelectState.setText("2. " + LanguageManager.getDual(this, "Select State", "select_state"));
        }
        if (stateDropdown != null) {
            stateDropdown.setHint(null);
        }
        if (layoutDropdownState != null) {
            layoutDropdownState.setHint(null);
        }
        if (tvTitleLandArea != null) {
            tvTitleLandArea.setText("3. " + LanguageManager.getDual(this, "Land Area", "land_area"));
        }
        if (unitDropdown != null) {
            unitDropdown.setHint(null);
        }
        if (layoutDropdownUnit != null) {
            layoutDropdownUnit.setHint(LanguageManager.getDual(this, "Unit", "unit"));
        }
        if (layoutInputArea != null) {
            layoutInputArea.setHint(LanguageManager.getDual(this, "Area", "area"));
        }
        if (btnEstimate != null) {
            btnEstimate.setText(LanguageManager.getButtonLines(this, "Estimate Yield", "estimate_yield"));
        }
        if (tvTitleEstimatedYield != null) {
            tvTitleEstimatedYield.setText(LanguageManager.getDual(this, "Estimated Yield", "yield_report"));
        }
        if (btnShare != null) {
            btnShare.setText(LanguageManager.getButtonLines(this, "Share Advice", "share_report"));
        }
        if (tvDisclaimer != null) {
            tvDisclaimer.setText(LanguageManager.getAppDisclaimer(this));
        }
    }

    private void initializeRegionalUnits() {
        regionUnitMap.put("NORTH", LanguageManager.getRegionalUnits(this, "NORTH"));
        regionUnitMap.put("SOUTH", LanguageManager.getRegionalUnits(this, "SOUTH"));
        regionUnitMap.put("EAST", LanguageManager.getRegionalUnits(this, "EAST"));
        regionUnitMap.put("WEST", LanguageManager.getRegionalUnits(this, "WEST"));
    }

    private void loadUnitsForState(String state) {
        String region = "NORTH"; // Default
        if (state.contains("Tamil") || state.contains("Karnataka") || state.contains("Kerala") || state.contains("Andhra") || state.contains("Telangana")) region = "SOUTH";
        else if (state.contains("Bengal") || state.contains("Bihar") || state.contains("Assam") || state.contains("Odisha")) region = "EAST";
        else if (state.contains("Gujarat") || state.contains("Maharashtra") || state.contains("Goa")) region = "WEST";

        Map<String, Double> units = regionUnitMap.get(region);
        if (units != null) {
            String[] unitNames = units.keySet().toArray(new String[0]);
            unitDropdown.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, unitNames));
            unitDropdown.setText(unitNames[0], false);
        }
    }

    private void calculateYield() {
        String crop = cropDropdown.getText().toString();
        String state = stateDropdown.getText().toString();
        String unit = unitDropdown.getText().toString();
        String areaStr = areaInput.getText() != null ? areaInput.getText().toString() : "";

        if (crop.isEmpty() || state.isEmpty() || unit.isEmpty() || areaStr.isEmpty()) {
            Snackbar.make(rootView, LanguageManager.getDual(this, "Please fill all fields", "select_crop"), Snackbar.LENGTH_SHORT).show();
            return;
        }

        // Determine region again for calculation factor
        String region = "NORTH";
        if (state.contains("Tamil") || state.contains("Karnataka") || state.contains("Kerala") || state.contains("Andhra") || state.contains("Telangana")) region = "SOUTH";
        else if (state.contains("Bengal") || state.contains("Bihar") || state.contains("Assam") || state.contains("Odisha")) region = "EAST";
        else if (state.contains("Gujarat") || state.contains("Maharashtra") || state.contains("Goa")) region = "WEST";

        Map<String, Double> units = regionUnitMap.get(region);
        Double conversion = units != null ? units.get(unit) : null;
        if (conversion == null) conversion = 1.0;
        double acres = Double.parseDouble(areaStr) * conversion;

        // Baseline yield in Quintals per Acre
        String cropLower = crop.toLowerCase();
        double avgAcre = 15.0;
        if (cropLower.contains("wheat")) avgAcre = 18.5;
        else if (cropLower.contains("rice") || cropLower.contains("paddy")) avgAcre = 20.0;
        else if (cropLower.contains("maize") || cropLower.contains("corn")) avgAcre = 22.0;
        else if (cropLower.contains("cotton")) avgAcre = 10.0;
        else if (cropLower.contains("sugarcane")) avgAcre = 320.0;
        else if (cropLower.contains("soybean") || cropLower.contains("soya")) avgAcre = 8.5;
        else if (cropLower.contains("mustard")) avgAcre = 7.2;
        else if (cropLower.contains("potato")) avgAcre = 85.0;
        else if (cropLower.contains("onion")) avgAcre = 70.0;
        else if (cropLower.contains("chili") || cropLower.contains("chilli")) avgAcre = 15.0;

        // Adjust yield based on crop health and survival chance from intent
        int survivalChance = getIntent().getIntExtra("survival_chance", -1);
        int healthScore = getIntent().getIntExtra("health_score", -1);
        double yieldFactor = 1.0;

        if (survivalChance != -1 || healthScore != -1) {
            double sFactor = (survivalChance != -1 ? survivalChance : 100) / 100.0;
            double hFactor = (healthScore != -1 ? healthScore : 100) / 100.0;
            yieldFactor = (sFactor + hFactor) / 2.0;
        }

        double adjustedAvgAcre = avgAcre * yieldFactor;
        double total = adjustedAvgAcre * acres;

        // Dynamic result display
        String avgYieldLabel = LanguageManager.getDual(this, "Avg Yield", "avg_yield");
        String quintalsAcre = LanguageManager.getDual(this, "Quintals/Acre", "quintals_acre");
        tvYieldPerAcre.setText(String.format(Locale.getDefault(), "%s: %.1f %s", avgYieldLabel, adjustedAvgAcre, quintalsAcre));

        String totalEstLabel = LanguageManager.getDual(this, "Total Estimate", "total_yield");
        String quintalsUnit = LanguageManager.getDual(this, "Quintals", "quintals");
        tvTotalYield.setText(String.format(Locale.getDefault(), "%s: %.1f %s", totalEstLabel, total, quintalsUnit));

        String cropDisplay = crop.contains("/") && LanguageManager.isEnglishOnly(this) ? crop.split("/")[0].trim() : crop;
        String stateDisplay = state.contains("/") && LanguageManager.isEnglishOnly(this) ? state.split("/")[0].trim() : state;

        String summary = String.format(Locale.getDefault(),
                "Estimation for %s on %.2f Acres area. Based on %s regional averages.",
                cropDisplay, acres, stateDisplay);

        if (survivalChance != -1 || healthScore != -1) {
            if (LanguageManager.isEnglishOnly(this)) {
                summary += String.format(Locale.getDefault(),
                        "\n\n🤖 Real-time AI Insight:\nYield estimate is adjusted by a factor of %.2fx due to crop health (%d/100) and survival chance (%d%%).",
                        yieldFactor, healthScore, survivalChance);
            } else {
                summary += String.format(Locale.getDefault(),
                        "\n\n🤖 Real-time AI Insight:\nYield estimate is adjusted by a factor of %.2fx due to crop health (%d/100) and survival chance (%d%%).\n"
                        + "(" + LanguageManager.getRegional(this, "ai_advisory", "एआई पैदावार इनसाइट") + ": %d/100, %d%% -> %.2fx)",
                        yieldFactor, healthScore, survivalChance, healthScore, survivalChance, yieldFactor);
            }
        }

        tvYieldSummary.setText(summary);

        resultSection.setVisibility(View.VISIBLE);
        resultSection.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
    }

    private void shareResult() {
        if (resultSection.getVisibility() != View.VISIBLE) {
            Snackbar.make(rootView, LanguageManager.getDual(this, "Please estimate first", "estimate_yield"), Snackbar.LENGTH_SHORT).show();
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
        String crop = cropDropdown.getText().toString();
        String state = stateDropdown.getText().toString();
        String unit = unitDropdown.getText().toString();
        String areaVal = areaInput.getText().toString();

        StringBuilder sb = new StringBuilder();
        sb.append("*").append(LanguageManager.getDual(this, "Jai Kisan - Crop Yield Report", "yield_report")).append("*\n");
        sb.append("----------------------------------\n");
        sb.append(LanguageManager.getDual(this, "Crop", "crop")).append(": ").append(crop).append("\n");
        sb.append(LanguageManager.getDual(this, "State", "state")).append(": ").append(state).append("\n");
        sb.append(LanguageManager.getDual(this, "Land Area", "land_area")).append(": ").append(areaVal).append(" ").append(unit).append("\n\n");

        sb.append("*").append(LanguageManager.getDual(this, "Yield Estimation", "yield_report")).append(":*\n");
        sb.append("- ").append(tvYieldPerAcre.getText().toString()).append("\n");
        sb.append("- ").append(tvTotalYield.getText().toString()).append("\n\n");

        sb.append("*").append(LanguageManager.getDual(this, "Summary & Insights", "ai_advisory")).append(":*\n");
        sb.append(tvYieldSummary.getText().toString()).append("\n\n");

        sb.append(LanguageManager.getRegional(this, "generated_via", "Generated via Jai Kisan App"));

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, sb.toString());
        startActivity(Intent.createChooser(shareIntent, LanguageManager.getDual(this, "Share via", "share_report")));
    }

    private void shareAsPdf() {
        String crop = cropDropdown.getText().toString();
        String state = stateDropdown.getText().toString();
        String unit = unitDropdown.getText().toString();
        String areaVal = areaInput.getText().toString();

        String appTitle = LanguageManager.getDual(this, "Jai Kisan", "jai_kisan");
        String reportTitle = LanguageManager.getDual(this, "Yield Estimation Report", "yield_report");
        String cropLabel = LanguageManager.getDual(this, "Crop", "crop");
        String stateLabel = LanguageManager.getDual(this, "State", "state");
        String landAreaLabel = LanguageManager.getDual(this, "Land Area", "land_area");
        String aiInsightsLabel = LanguageManager.getDual(this, "AI Insights & Summary", "ai_advisory");
        String footerText = LanguageManager.getRegional(this, "generated_via", "Generated via Jai Kisan App &bull; Supporting Indian Agriculture");

        String htmlContent = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "<meta charset=\"UTF-8\">\n" +
                "<style>\n" +
                "    @page { size: A4; margin: 10mm 8mm; }\n" +
                "    body { font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; color: #333; margin: 0; padding: 0; }\n" +
                "    h1, h2, h3, h4, .section-title { page-break-after: avoid; break-after: avoid; }\n" +
                "    .meta-box, .summary-box, .insight-container, .pdf-disclaimer { page-break-inside: avoid; break-inside: avoid; }\n" +
                "    .header { border-bottom: 3px solid #2E7D32; padding-bottom: 10px; margin-bottom: 20px; display: flex; justify-content: space-between; align-items: center; }\n" +
                "    .app-title { color: #2E7D32; font-size: 28px; font-weight: bold; margin: 0; }\n" +
                "    .report-title { font-size: 14px; text-transform: uppercase; color: #666; letter-spacing: 1px; }\n" +
                "    \n" +
                "    .meta-box { background: #F5F5F5; border: 1px solid #E0E0E0; border-radius: 6px; padding: 12px; margin-bottom: 20px; display: grid; grid-template-columns: repeat(3, 1fr); gap: 10px; }\n" +
                "    .meta-item { font-size: 13px; }\n" +
                "    .meta-item strong { color: #2E7D32; display: block; margin-bottom: 2px; }\n" +
                "\n" +
                "    .summary-box { background: #E8F5E9; border-left: 4px solid #2E7D32; padding: 15px; border-radius: 4px; margin-bottom: 25px; font-size: 14px; color: #1B5E20; }\n" +
                "    .summary-box strong { font-size: 16px; display: block; margin-bottom: 5px; }\n" +
                "    \n" +
                "    .insight-container { border-top: 2px dashed #DDD; padding-top: 15px; }\n" +
                "    .lang-block { font-size: 12px; line-height: 1.6; }\n" +
                "    .lang-block h3 { color: #2E7D32; font-size: 14px; margin-top: 0; margin-bottom: 8px; border-bottom: 1px solid #DDD; padding-bottom: 4px; }\n" +
                "    \n" +
                "    .footer { text-align: center; font-size: 11px; color: #999; margin-top: 30px; border-top: 1px solid #EEE; padding-top: 10px; }\n" +
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
                "    <div class=\"meta-item\"><strong>" + stateLabel + "</strong>" + state + "</div>\n" +
                "    <div class=\"meta-item\"><strong>" + landAreaLabel + "</strong>" + areaVal + " " + unit + "</div>\n" +
                "</div>\n" +
                "\n" +
                "<div class=\"summary-box\">\n" +
                "    <strong>" + tvTotalYield.getText().toString() + "</strong>\n" +
                "    " + tvYieldPerAcre.getText().toString() + "\n" +
                "</div>\n" +
                "\n" +
                "<div class=\"insight-container\">\n" +
                "    <div class=\"lang-block\">\n" +
                "        <h3>" + aiInsightsLabel + "</h3>\n" +
                "        <p style=\"white-space: pre-line;\">" + tvYieldSummary.getText().toString() + "</p>\n" +
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

        HtmlToPdfManager.generatePdfFromHtml(this, htmlContent, "JaiKisan_Yield_Report");
    }
}