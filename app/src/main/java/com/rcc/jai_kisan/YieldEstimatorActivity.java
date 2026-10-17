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
    // Container view captured for PDF rendering
    private View resultPdfContainer;
    private TextView tvYieldPerAcre, tvTotalYield, tvYieldSummary;

    // Mapping logic matching Fertilizer Calculator
    private final Map<String, Map<String, Double>> regionUnitMap = new HashMap<>();

    private static final String[] CROPS = {
            "Wheat / गेहूं", "Rice / चावल", "Maize / मक्का", "Cotton / कपास",
            "Sugarcane / गन्ना", "Soybean / सोयाबीन", "Mustard / सरसों", "Potato / आलू",
            "Onion / प्याज", "Chili / मिर्च"
    };

    // Avg Yields in Quintals per Acre
    private static final double[] AVG_YIELDS = {18.5, 20.0, 22.0, 10.0, 320.0, 8.5, 7.2, 85.0, 70.0, 15.0};

    private static final String[] STATES = {
            "Andhra Pradesh / आंध्र प्रदेश", "Arunachal Pradesh / अरुणाचल प्रदेश", "Assam / असम", "Bihar / बिहार", "Chhattisgarh / छत्तीसगढ़", "Goa / गोवा", "Gujarat / गुजरात", "Haryana / हरियाणा", "Himachal Pradesh / हिमाचल प्रदेश", "Jharkhand / झारखंड", "Karnataka / कर्नाटक", "Kerala / केरल", "Madhya Pradesh / मध्य प्रदेश", "Maharashtra / महाराष्ट्र", "Manipur / मणिपुर", "Meghalaya / मेघालय", "Mizoram / मिजोरम", "Nagaland / नागालैंड", "Odisha / ओडिशा", "Punjab / पंजाब", "Rajasthan / राजस्थान", "Sikkim / सिक्किम", "Tamil Nadu / तमिलनाडु", "Telangana / तेलंगाना", "Tripura / त्रिपुरा", "Uttar Pradesh / उत्तर प्रदेश", "Uttarakhand / उत्तराखंड", "West Bengal / पश्चिम बंगाल", "Delhi / दिल्ली", "Jammu & Kashmir / जम्मू और कश्मीर", "Ladakh / लद्दाख", "Puducherry / पुडुचेरी", "Andaman & Nicobar / अंडमान और निकोबार"
    };

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

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        ImageButton btnClose = findViewById(R.id.btn_close);
        if (btnClose != null) btnClose.setOnClickListener(v -> finish());

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
        }

        // Regional logic trigger
        stateDropdown.setOnItemClickListener((parent, view, position, id) -> {
            String selectedState = defaultStates[position];
            loadUnitsForState(selectedState);
        });

        findViewById(R.id.btn_estimate).setOnClickListener(v -> calculateYield());
        findViewById(R.id.btn_share).setOnClickListener(v -> shareResult());

        // Keep fields empty for user selection. No auto-fill or auto-execute.
        if (false) {
            stateDropdown.setText("Uttar Pradesh / उत्तर प्रदेश", false);
            loadUnitsForState("Uttar Pradesh / उत्तर प्रदेश");
            areaInput.setText("1");
            calculateYield();
        }
    }

    private void initializeRegionalUnits() {
        // Shared Universal Units
        Map<String, Double> universal = new HashMap<>();
        universal.put("Acre / एकड़", 1.0);
        universal.put("Hectare / हेक्टेयर", 2.47);
        universal.put("Sq. Ft / वर्ग फुट", 0.0000229);

        // Regional Definitions
        Map<String, Double> north = new HashMap<>(universal);
        north.put("Bigha / बीघा", 0.625);
        north.put("Kanal / कनाल", 0.125);
        north.put("Biswa / बिस्वा", 0.031);
        north.put("Killa / किल्ला", 1.0);

        Map<String, Double> south = new HashMap<>(universal);
        south.put("Cent / सेंट", 0.01);
        south.put("Guntha / गुंठा", 0.025);
        south.put("Ankanam / अंकनम", 0.0016);

        Map<String, Double> east = new HashMap<>(universal);
        east.put("Katha / कट्ठा", 0.031);
        east.put("Decimal / डेसीमल", 0.01);
        east.put("Chatak / छटाक", 0.004);

        Map<String, Double> west = new HashMap<>(universal);
        west.put("Vigha / विघा", 0.4);
        west.put("Guntha / गुंठा", 0.025);

        regionUnitMap.put("NORTH", north);
        regionUnitMap.put("SOUTH", south);
        regionUnitMap.put("EAST", east);
        regionUnitMap.put("WEST", west);
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
            Snackbar.make(rootView, "Please fill all fields / कृपया पूरी जानकारी दें", Snackbar.LENGTH_SHORT).show();
            return;
        }

        // Determine region again for calculation factor
        String region = "NORTH";
        if (state.contains("Tamil") || state.contains("Karnataka") || state.contains("Kerala") || state.contains("Andhra") || state.contains("Telangana")) region = "SOUTH";
        else if (state.contains("Bengal") || state.contains("Bihar") || state.contains("Assam") || state.contains("Odisha")) region = "EAST";
        else if (state.contains("Gujarat") || state.contains("Maharashtra") || state.contains("Goa")) region = "WEST";

        double conversion = regionUnitMap.get(region).get(unit);
        double acres = Double.parseDouble(areaStr) * conversion;

        // Get index for crop yield
        int idx = -1;
        for (int i = 0; i < CROPS.length; i++) {
            if (CROPS[i].equalsIgnoreCase(crop)) {
                idx = i;
                break;
            }
        }

        double avgAcre;
        if (idx != -1) {
            avgAcre = AVG_YIELDS[idx];
        } else {
            // General baseline average yield for dynamic/custom crops (e.g. apple or pine)
            avgAcre = 15.0;
        }

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

        // Bilingual result display
        tvYieldPerAcre.setText(String.format(Locale.getDefault(),
                "Avg Yield / औसत पैदावार: %.1f Quintals/Acre", adjustedAvgAcre));

        tvTotalYield.setText(String.format(Locale.getDefault(),
                "Total Estimate / कुल अनुमान: %.1f Quintals (क्विंटल)", total));

        String cropNameHi = crop.contains("/") ? crop.split("/")[1].trim() : crop;
        String stateNameHi = state.contains("/") ? state.split("/")[1].trim() : state;

        String summary = String.format(Locale.getDefault(),
                "Estimation for %s on %.2f Acres area. Based on %s regional averages.",
                cropNameHi, acres, stateNameHi);

        if (survivalChance != -1 || healthScore != -1) {
            summary += String.format(Locale.getDefault(),
                    "\n\n🤖 Real-time AI Insight:\nYield estimate is adjusted by a factor of %.2fx due to crop health (%d/100) and survival chance (%d%%).\n"
                    + "(एआई पैदावार इनसाइट: फसल के स्वास्थ्य स्कोर (%d/100) और जीवित रहने की संभावना (%d%%) के कारण पैदावार के अनुमान को %.2f गुना समायोजित किया गया है।)",
                    yieldFactor, healthScore, survivalChance, healthScore, survivalChance, yieldFactor);
        }

        tvYieldSummary.setText(summary);

        resultSection.setVisibility(View.VISIBLE);
        resultSection.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
    }

    private void shareResult() {
        if (resultSection.getVisibility() != View.VISIBLE) {
            Snackbar.make(rootView, "Please estimate first / \u092a\u0939\u0932\u0947 \u0905\u0928\u0941\u092e\u093e\u0928 \u0932\u0917\u093e\u090f\u0902", Snackbar.LENGTH_SHORT).show();
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
                "    body { font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; color: #333; margin: 20px; }\n" +
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
                "<div class=\"footer\">\n" +
                "    " + footerText + "\n" +
                "</div>\n" +
                "\n" +
                "</body>\n" +
                "</html>";

        HtmlToPdfManager.generatePdfFromHtml(this, htmlContent, "JaiKisan_Yield_Report");
    }
}