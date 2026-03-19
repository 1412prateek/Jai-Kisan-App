package com.rcc.jai_kisan;

import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class YieldEstimatorActivity extends AppCompatActivity {

    private View rootView;
    private AutoCompleteTextView cropDropdown, stateDropdown, unitDropdown;
    private TextInputEditText areaInput;
    private View resultSection;
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

        tvYieldPerAcre = findViewById(R.id.tv_yield_per_acre);
        tvTotalYield = findViewById(R.id.tv_total_yield);
        tvYieldSummary = findViewById(R.id.tv_yield_summary);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        ImageButton btnClose = findViewById(R.id.btn_close);
        if (btnClose != null) btnClose.setOnClickListener(v -> finish());

        // Adapters
        cropDropdown.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, CROPS));
        stateDropdown.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, STATES));

        // Regional logic trigger
        stateDropdown.setOnItemClickListener((parent, view, position, id) -> {
            String selectedState = STATES[position];
            loadUnitsForState(selectedState);
        });

        findViewById(R.id.btn_estimate).setOnClickListener(v -> calculateYield());
        findViewById(R.id.btn_share).setOnClickListener(v -> shareResult());
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
        int idx = 0;
        for (int i = 0; i < CROPS.length; i++) {
            if (CROPS[i].equals(crop)) idx = i;
        }

        double avgAcre = AVG_YIELDS[idx];
        double total = avgAcre * acres;

        // Bilingual result display
        tvYieldPerAcre.setText(String.format(Locale.getDefault(),
                "Avg Yield / औसत पैदावार: %.1f Quintals/Acre", avgAcre));

        tvTotalYield.setText(String.format(Locale.getDefault(),
                "Total Estimate / कुल अनुमान: %.1f Quintals (क्विंटल)", total));

        tvYieldSummary.setText(String.format(Locale.getDefault(),
                "Estimation for %s on %.2f Acres area. Based on %s regional averages.",
                crop.split("/")[1].trim(), acres, state.split("/")[1].trim()));

        resultSection.setVisibility(View.VISIBLE);
        resultSection.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
    }

    private void shareResult() {
        String msg = "🌾 Jai Kisan Yield Estimate\n" +
                tvYieldPerAcre.getText().toString() + "\n" +
                tvTotalYield.getText().toString() + "\n" +
                "— Jai Kisan App";
        android.content.Intent i = new android.content.Intent(android.content.Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(android.content.Intent.EXTRA_TEXT, msg);
        startActivity(android.content.Intent.createChooser(i, "Share Estimate"));
    }
}