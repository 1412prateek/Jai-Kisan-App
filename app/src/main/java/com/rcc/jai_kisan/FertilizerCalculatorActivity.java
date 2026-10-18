package com.rcc.jai_kisan;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FertilizerCalculatorActivity extends BaseActivity {

    private View rootView;
    private AutoCompleteTextView cropDropdown, stateDropdown, unitDropdown;
    private TextInputEditText areaInput;
    private com.google.android.material.textfield.TextInputLayout layoutDropdownCrop, layoutDropdownState, layoutDropdownUnit, layoutInputArea;
    private TextView tvTitleFertilizerPro, tvTitleSelectCrop, tvTitleSelectState, tvTitleLandArea;
    private TextView tvTitleRecommendedFertilizer, tvTitleShoppingList, tvTitleLiquidOption, tvDisclaimer;
    private com.google.android.material.button.MaterialButton btnCalculate, btnShare;
    private MaterialToolbar toolbar;
    private View resultSection;
    private TextView tvUreaBags, tvDapBags, tvMopBags;
    private TextView tvNRow, tvPRow, tvKRow, tvNanoUrea;

    private final Map<String, Map<String, Double>> regionUnitMap = new HashMap<>();

    // Nutrient data per acre: {N, P, K}
    private static final double[][] NPK_BASE_RATES = {
            {55.0, 30.0, 15.0}, {50.0, 25.0, 25.0}, {60.0, 30.0, 20.0}, {50.0, 25.0, 25.0},
            {80.0, 35.0, 40.0}, {20.0, 40.0, 20.0}, {45.0, 20.0, 15.0}, {75.0, 40.0, 75.0},
            {40.0, 20.0, 20.0}, {35.0, 25.0, 25.0}
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fertilizer_calculator);

        initializeRegionalUnits();

        rootView = findViewById(R.id.root_view);
        cropDropdown = findViewById(R.id.dropdown_crop);
        stateDropdown = findViewById(R.id.dropdown_state);
        unitDropdown = findViewById(R.id.dropdown_unit);
        areaInput = findViewById(R.id.input_area);
        layoutDropdownCrop = findViewById(R.id.layout_dropdown_crop);
        layoutDropdownState = findViewById(R.id.layout_dropdown_state);
        layoutDropdownUnit = findViewById(R.id.layout_dropdown_unit);
        layoutInputArea = findViewById(R.id.layout_input_area);

        tvTitleFertilizerPro = findViewById(R.id.tv_title_fertilizer_pro);
        tvTitleSelectCrop = findViewById(R.id.tv_title_select_crop);
        tvTitleSelectState = findViewById(R.id.tv_title_select_state);
        tvTitleLandArea = findViewById(R.id.tv_title_land_area);
        tvTitleRecommendedFertilizer = findViewById(R.id.tv_title_recommended_fertilizer);
        tvTitleShoppingList = findViewById(R.id.tv_title_shopping_list);
        tvTitleLiquidOption = findViewById(R.id.tv_title_liquid_option);
        tvDisclaimer = findViewById(R.id.tv_disclaimer);

        btnCalculate = findViewById(R.id.btn_calculate);
        btnShare = findViewById(R.id.btn_share);

        resultSection = findViewById(R.id.result_section);

        tvUreaBags = findViewById(R.id.tv_urea_bags);
        tvDapBags = findViewById(R.id.tv_dap_bags);
        tvMopBags = findViewById(R.id.tv_mop_bags);

        tvNRow = findViewById(R.id.tv_n_row);
        tvPRow = findViewById(R.id.tv_p_row);
        tvKRow = findViewById(R.id.tv_k_row);
        tvNanoUrea = findViewById(R.id.tv_nano_urea);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        ImageButton btnClose = findViewById(R.id.btn_close);
        if (btnClose != null) btnClose.setOnClickListener(v -> finish());

        // 1. Populate Crops (including dynamic passed crop)
        String[] defaultCrops = LanguageManager.getLocalizedCrops(this);

        String cropEn = getIntent().getStringExtra("crop_name_en");
        String cropHi = getIntent().getStringExtra("crop_name_hi");

        List<String> cropList = new ArrayList<>(java.util.Arrays.asList(defaultCrops));
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

        // 2. Populate States
        String[] allStates = LanguageManager.getLocalizedStates(this);
        stateDropdown.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, allStates));
        if (allStates.length > 0) {
            stateDropdown.setText(allStates[0], false);
            loadUnitsForState(allStates[0]);
        }

        stateDropdown.setOnItemClickListener((parent, view, position, id) -> loadUnitsForState(allStates[position]));

        if (btnCalculate != null) {
            btnCalculate.setOnClickListener(v -> calculateProFertilizer());
        }
        if (btnShare != null) {
            btnShare.setOnClickListener(v -> shareResult());
        }
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
            toolbar.setTitle(LanguageManager.getDual(this, "Fertilizer Calculator", "fertilizer_calculator"));
        }
        if (tvTitleFertilizerPro != null) {
            tvTitleFertilizerPro.setText(LanguageManager.getDual(this, "Fertilizer Pro", "fertilizer_calculator"));
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
        if (btnCalculate != null) {
            btnCalculate.setText(LanguageManager.getButtonLines(this, "Calculate Requirements", "calculate_requirements"));
        }
        if (tvTitleRecommendedFertilizer != null) {
            tvTitleRecommendedFertilizer.setText(LanguageManager.getDual(this, "Recommended Fertilizer", "recommended_fertilizer"));
        }
        if (tvTitleShoppingList != null) {
            tvTitleShoppingList.setText(LanguageManager.getDual(this, "Shopping List (Bags):", "shopping_list"));
        }
        if (tvTitleLiquidOption != null) {
            tvTitleLiquidOption.setText(LanguageManager.getDual(this, "Liquid Option (Nano Urea):", "liquid_option"));
        }
        if (btnShare != null) {
            btnShare.setText(LanguageManager.getButtonLines(this, "Share Result", "share_report"));
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
        String region = "NORTH";
        if (state.contains("Tamil") || state.contains("Karnataka") || state.contains("Kerala") || state.contains("Andhra") || state.contains("Telangana"))
            region = "SOUTH";
        else if (state.contains("Bengal") || state.contains("Bihar") || state.contains("Assam") || state.contains("Odisha"))
            region = "EAST";
        else if (state.contains("Maharashtra") || state.contains("Gujarat") || state.contains("Goa"))
            region = "WEST";

        Map<String, Double> units = regionUnitMap.get(region);
        if (units != null) {
            String[] names = units.keySet().toArray(new String[0]);
            unitDropdown.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, names));
            unitDropdown.setText(names[0], false);
        }
    }

    private void calculateProFertilizer() {
        String crop = cropDropdown.getText().toString();
        String state = stateDropdown.getText().toString();
        String unit = unitDropdown.getText().toString();
        String areaVal = areaInput.getText().toString();

        if (crop.isEmpty() || state.isEmpty() || unit.isEmpty() || areaVal.isEmpty()) {
            Snackbar.make(rootView, LanguageManager.getDual(this, "Please fill all info", "please_fill_all"), Snackbar.LENGTH_SHORT).show();
            return;
        }

        // Get conversion factor
        String region = "NORTH";
        if (state.contains("Tamil") || state.contains("Karnataka") || state.contains("Kerala") || state.contains("Andhra"))
            region = "SOUTH";
        else if (state.contains("Bengal") || state.contains("Bihar") || state.contains("Assam"))
            region = "EAST";

        double factor = 1.0;
        Map<String, Double> unitMap = regionUnitMap.get(region);
        if (unitMap != null && unitMap.containsKey(unit)) {
            factor = unitMap.get(unit);
        } else {
            if (unit.contains("Hectare")) factor = 2.47;
            else if (unit.contains("Bigha")) factor = 0.625;
            else if (unit.contains("Kanal")) factor = 0.125;
            else if (unit.contains("Guntha")) factor = 0.025;
            else if (unit.contains("Cent")) factor = 0.01;
            else if (unit.contains("Katha")) factor = 0.031;
            else if (unit.contains("Decimal")) factor = 0.01;
        }

        double acres = Double.parseDouble(areaVal) * factor;

        // Find crop index
        int idx = -1;
        for (int i = 0; i < LanguageManager.DEFAULT_CROPS.length; i++) {
            if (crop.toLowerCase().contains(LanguageManager.DEFAULT_CROPS[i].en.toLowerCase())) {
                idx = i;
                break;
            }
        }

        double[] rates;
        if (idx != -1) {
            rates = NPK_BASE_RATES[idx];
        } else {
            // General safe default rates for dynamic/new crops
            rates = new double[]{50.0, 25.0, 25.0};
        }

        double netN = rates[0] * acres;
        double netP = rates[1] * acres;
        double netK = rates[2] * acres;

        // Bag Conversion
        double dapKg = netP / 0.46;
        double ureaKg = Math.max(0, (netN - (dapKg * 0.18)) / 0.46);
        double mopKg = netK / 0.60;

        // Nano Urea: 1 bottle (500ml) replaces 1 bag of Urea (approx 45kg N-content equivalent)
        int nanoBottles = (int) Math.ceil(ureaKg / 45.0);

        displayResults(netN, netP, netK, ureaKg, dapKg, mopKg, nanoBottles, rates);
    }

    private void displayResults(double n, double p, double k, double u, double d, double m, int nano, double[] base) {
        // 1. NPK Summary Row Style
        tvNRow.setText(String.format(Locale.getDefault(), "%s:   %.0f kg/acre → %.1f kg", LanguageManager.getDual(this, "Nitrogen (N)", "nitrogen"), base[0], n));
        tvPRow.setText(String.format(Locale.getDefault(), "%s:   %.0f kg/acre → %.1f kg", LanguageManager.getDual(this, "Phosphorus (P)", "phosphorus"), base[1], p));
        tvKRow.setText(String.format(Locale.getDefault(), "%s:   %.0f kg/acre → %.1f kg", LanguageManager.getDual(this, "Potassium (K)", "potassium"), base[2], k));

        // 2. Shopping List (Bags + Loose)
        tvUreaBags.setText(formatBagString("Urea", "यूरिया", u));
        tvDapBags.setText(formatBagString("DAP", "डीएपी", d));
        tvMopBags.setText(formatBagString("MOP", "एमओपी", m));

        // 3. Nano Urea
        tvNanoUrea.setText(formatNanoUreaString(nano));

        resultSection.setVisibility(View.VISIBLE);
        resultSection.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
    }

    private String formatBagString(String enLabel, String hiLabel, double totalKg) {
        int bags = (int) (totalKg / 50);
        int loose = (int) (totalKg % 50);
        String label = LanguageManager.formatDual(this, enLabel, hiLabel);
        return String.format(Locale.getDefault(), "%s: %d Bags + %d kg loose", label, bags, loose);
    }

    private String formatNanoUreaString(int nano) {
        return String.format(Locale.getDefault(), "%d Bottle(s) of 500ml\n(%s %d %s)",
                nano,
                LanguageManager.getDual(this, "Can replace", "liquid_option"),
                nano,
                LanguageManager.getDual(this, "bag(s) of conventional Urea", "urea"));
    }

    private void shareResult() {
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
        String state = stateDropdown.getText().toString();
        String unit = unitDropdown.getText().toString();
        String areaVal = areaInput.getText().toString();

        StringBuilder sb = new StringBuilder();
        sb.append("*").append(LanguageManager.getDual(this, "Jai Kisan - Fertilizer Report", "recommended_fertilizer")).append("*\n");
        sb.append("----------------------------------\n");
        sb.append(LanguageManager.getDual(this, "Crop", "crop")).append(": ").append(crop).append("\n");
        sb.append(LanguageManager.getDual(this, "State", "state")).append(": ").append(state).append("\n");
        sb.append(LanguageManager.getDual(this, "Land Area", "land_area")).append(": ").append(areaVal).append(" ").append(unit).append("\n\n");

        sb.append("*").append(LanguageManager.getDual(this, "Recommended Nutrition", "recommended_fertilizer")).append(":*\n");
        sb.append("- ").append(tvNRow.getText().toString()).append("\n");
        sb.append("- ").append(tvPRow.getText().toString()).append("\n");
        sb.append("- ").append(tvKRow.getText().toString()).append("\n\n");

        sb.append("*").append(LanguageManager.getDual(this, "Shopping List (Bags)", "shopping_list")).append(":*\n");
        sb.append("- ").append(tvUreaBags.getText().toString()).append("\n");
        sb.append("- ").append(tvDapBags.getText().toString()).append("\n");
        sb.append("- ").append(tvMopBags.getText().toString()).append("\n\n");

        sb.append("*").append(LanguageManager.getDual(this, "Liquid Option (Nano)", "liquid_option")).append(":*\n");
        sb.append("- ").append(tvNanoUrea.getText().toString()).append("\n\n");

        ArrayList<String> suggestions = getIntent().getStringArrayListExtra("fertilizer_suggestions");
        if (suggestions != null && !suggestions.isEmpty()) {
            sb.append("*").append(LanguageManager.getDual(this, "AI Sprout Insights", "ai_advisory")).append(":*\n");
            for (String s : suggestions) {
                sb.append("• ").append(s).append("\n");
            }
            sb.append("\n");
        }
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

        StringBuilder aiInsightsList = new StringBuilder();
        ArrayList<String> suggestions = getIntent().getStringArrayListExtra("fertilizer_suggestions");
        if (suggestions != null && !suggestions.isEmpty()) {
            for (String s : suggestions) {
                aiInsightsList.append("<li>").append(s).append("</li>");
            }
        }

        String appTitle = LanguageManager.getDual(this, "Jai Kisan", "jai_kisan");
        String reportTitle = LanguageManager.getDual(this, "Fertilizer Recommendation Report", "recommended_fertilizer");
        String cropLabel = LanguageManager.getDual(this, "Crop", "crop");
        String stateLabel = LanguageManager.getDual(this, "State", "state");
        String landAreaLabel = LanguageManager.getDual(this, "Land Area", "land_area");
        String recommendedFertLabel = LanguageManager.getDual(this, "Recommended Fertilizer", "recommended_fertilizer");
        String shoppingListLabel = LanguageManager.getDual(this, "Shopping List (Bags)", "shopping_list");
        String liquidOptionLabel = LanguageManager.getDual(this, "Liquid Option (Nano Urea)", "liquid_option");
        String aiInsightsLabel = LanguageManager.getDual(this, "AI Sprout Insights", "ai_advisory");
        String footerText = LanguageManager.getRegional(this, "generated_via", "Generated via Jai Kisan App &bull; Supporting Indian Agriculture");

        String htmlContent = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "<meta charset=\"UTF-8\">\n" +
                "<style>\n" +
                "    @page { size: A4; margin: 10mm 8mm; }\n" +
                "    body { font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; color: #333; margin: 0; padding: 0; }\n" +
                "    h1, h2, h3, h4, .section-title { page-break-after: avoid; break-after: avoid; }\n" +
                "    .meta-box, .results-table, .nano-box, .insight-container, .pdf-disclaimer { page-break-inside: avoid; break-inside: avoid; }\n" +
                "    .header { border-bottom: 3px solid #1B5E20; padding-bottom: 10px; margin-bottom: 20px; display: flex; justify-content: space-between; align-items: center; }\n" +
                "    .app-title { color: #1B5E20; font-size: 28px; font-weight: bold; margin: 0; }\n" +
                "    .report-title { font-size: 14px; text-transform: uppercase; color: #666; letter-spacing: 1px; }\n" +
                "    \n" +
                "    .meta-box { background: #F5F5F5; border: 1px solid #E0E0E0; border-radius: 6px; padding: 12px; margin-bottom: 20px; display: grid; grid-template-columns: repeat(3, 1fr); gap: 10px; }\n" +
                "    .meta-item { font-size: 13px; }\n" +
                "    .meta-item strong { color: #1B5E20; display: block; margin-bottom: 2px; }\n" +
                "\n" +
                "    .results-table { width: 100%; border-collapse: collapse; margin-bottom: 20px; }\n" +
                "    .results-table th { background: #1B5E20; color: white; text-align: left; padding: 10px; font-size: 14px; }\n" +
                "    .results-table td { border: 1px solid #E0E0E0; padding: 12px; font-size: 13px; vertical-align: top; }\n" +
                "    .results-table tr:nth-child(even) { background: #FBFBFB; }\n" +
                "\n" +
                "    .nano-box { background: #E8F5E9; border-left: 4px solid #2E7D32; padding: 12px; border-radius: 4px; margin-bottom: 25px; font-size: 13px; }\n" +
                "    \n" +
                "    .insight-container { border-top: 2px dashed #DDD; padding-top: 15px; }\n" +
                "    .lang-block { font-size: 12px; line-height: 1.6; }\n" +
                "    .lang-block h3 { color: #1B5E20; font-size: 14px; margin-top: 0; margin-bottom: 8px; border-bottom: 1px solid #DDD; padding-bottom: 4px; }\n" +
                "    .lang-block ul { margin: 0; padding-left: 18px; }\n" +
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
                "<table class=\"results-table\">\n" +
                "    <thead>\n" +
                "        <tr>\n" +
                "            <th style=\"width: 50%;\">" + recommendedFertLabel + "</th>\n" +
                "            <th style=\"width: 50%;\">" + shoppingListLabel + "</th>\n" +
                "        </tr>\n" +
                "    </thead>\n" +
                "    <tbody>\n" +
                "        <tr>\n" +
                "            <td>" + tvNRow.getText().toString() + "</td>\n" +
                "            <td>" + tvUreaBags.getText().toString() + "</td>\n" +
                "        </tr>\n" +
                "        <tr>\n" +
                "            <td>" + tvPRow.getText().toString() + "</td>\n" +
                "            <td>" + tvDapBags.getText().toString() + "</td>\n" +
                "        </tr>\n" +
                "        <tr>\n" +
                "            <td>" + tvKRow.getText().toString() + "</td>\n" +
                "            <td>" + tvMopBags.getText().toString() + "</td>\n" +
                "        </tr>\n" +
                "    </tbody>\n" +
                "</table>\n" +
                "\n" +
                "<div class=\"nano-box\">\n" +
                "    <strong>" + liquidOptionLabel + ":</strong><br>\n" +
                "    " + tvNanoUrea.getText().toString() + "\n" +
                "</div>\n" +
                "\n" +
                (aiInsightsList.length() > 0 ? 
                "<div class=\"insight-container\">\n" +
                "    <div class=\"lang-block\">\n" +
                "        <h3>" + aiInsightsLabel + "</h3>\n" +
                "        <ul>\n" +
                "            " + aiInsightsList.toString() + "\n" +
                "        </ul>\n" +
                "    </div>\n" +
                "</div>\n" : "") +
                "\n" +
                LanguageManager.getPdfDisclaimerHtml(this) +
                "\n" +
                "<div class=\"footer\">\n" +
                "    " + footerText + "\n" +
                "</div>\n" +
                "\n" +
                "</body>\n" +
                "</html>";

        HtmlToPdfManager.generatePdfFromHtml(this, htmlContent, "JaiKisan_Fertilizer_Report");
    }
}
