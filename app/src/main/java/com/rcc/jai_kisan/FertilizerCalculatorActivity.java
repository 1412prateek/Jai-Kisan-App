package com.rcc.jai_kisan;

import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Locale;

/**
 * FertilizerCalculatorActivity
 *
 * Allows the user to select a crop and enter land area (in acres),
 * then calculates the recommended N, P, K fertilizer dosage.
 *
 * Design rules enforced:
 * - Toolbar: #E07854 (terracotta), NOT colorPrimary
 * - Screen background: #E5E5D8 (set in layout XML)
 * - Calculate button: pill-shape (50dp), match_parent, #2E7D32
 * - Validation error: Snackbar (NOT Toast or AlertDialog)
 * - Results: slide-down within same screen (NOT new Activity)
 */
public class FertilizerCalculatorActivity extends AppCompatActivity {

    // Views
    private View rootView;
    private AutoCompleteTextView cropDropdown;
    private TextInputEditText areaInput;
    private LinearLayout resultSection;
    private TextView tvNitrogen, tvPhosphorus, tvPotassium;
    private TextView tvTotalSummary, tvTotalValues;

    // ─── Fertilizer NPK rates per acre (kg/acre) ─────────────────────────────
    // Source: ICAR baseline recommendations for major Indian crops
    // Format: { N, P, K } per acre
    private static final double[][] NPK_RATES = {
            // Wheat / गेहूं
            { 55.0, 30.0, 15.0 },
            // Rice / चावल
            { 50.0, 25.0, 25.0 },
            // Maize / मक्का
            { 60.0, 30.0, 20.0 },
            // Cotton / कपास
            { 50.0, 25.0, 25.0 },
            // Sugarcane / गन्ना
            { 80.0, 35.0, 40.0 },
            // Soybean / सोयाबीन
            { 20.0, 40.0, 20.0 },
            // Mustard / सरसों
            { 45.0, 20.0, 15.0 },
            // Potato / आलू
            { 75.0, 40.0, 75.0 }
    };

    // Bilingual crop names — MUST match dropdown order exactly
    private static final String[] CROPS = {
            "Wheat / गेहूं",
            "Rice / चावल",
            "Maize / मक्का",
            "Cotton / कपास",
            "Sugarcane / गन्ना",
            "Soybean / सोयाबीन",
            "Mustard / सरसों",
            "Potato / आलू"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fertilizer_calculator);

        // Bind views
        rootView = findViewById(R.id.root_view);
        cropDropdown = findViewById(R.id.dropdown_crop);
        areaInput = findViewById(R.id.input_area);
        resultSection = findViewById(R.id.result_section);
        tvNitrogen = findViewById(R.id.tv_nitrogen);
        tvPhosphorus = findViewById(R.id.tv_phosphorus);
        tvPotassium = findViewById(R.id.tv_potassium);
        tvTotalSummary = findViewById(R.id.tv_total_summary);
        tvTotalValues = findViewById(R.id.tv_total_values);

        // Toolbar — back arrow calls finish()
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Card close button (×) — also calls finish()
        ImageButton btnClose = findViewById(R.id.btn_close);
        btnClose.setOnClickListener(v -> finish());

        // Setup crop dropdown (ExposedDropdownMenu)
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                CROPS);
        cropDropdown.setAdapter(adapter);

        // Calculate button
        MaterialButton btnCalculate = findViewById(R.id.btn_calculate);
        btnCalculate.setOnClickListener(v -> calculateFertilizer());

        // Share button
        MaterialButton btnShare = findViewById(R.id.btn_share);
        btnShare.setOnClickListener(v -> shareResult());
    }

    /**
     * Validates inputs and triggers fertilizer calculation.
     * Shows Snackbar on validation failure — NOT Toast or AlertDialog.
     */
    private void calculateFertilizer() {
        String crop = cropDropdown.getText().toString().trim();
        String areaStr = areaInput.getText() != null
                ? areaInput.getText().toString().trim()
                : "";

        // FIXED: Replaced R.string.error_fill_fields with hardcoded text
        if (crop.isEmpty() || areaStr.isEmpty()) {
            Snackbar.make(rootView,
                    "Please fill all fields / कृपया सभी फ़ील्ड भरें",
                    Snackbar.LENGTH_SHORT).show();
            return;
        }

        double area;
        try {
            area = Double.parseDouble(areaStr);
            if (area <= 0)
                throw new NumberFormatException("Area must be positive");
        } catch (NumberFormatException e) {
            Snackbar.make(rootView,
                    "Please enter a valid area / कृपया वैध क्षेत्रफल दर्ज करें",
                    Snackbar.LENGTH_SHORT).show();
            return;
        }

        // Get NPK rates for selected crop
        int cropIndex = getCropIndex(crop);
        double[] rates = (cropIndex >= 0) ? NPK_RATES[cropIndex] : new double[] { 40.0, 20.0, 15.0 };

        double nitrogen = rates[0] * area;
        double phosphorus = rates[1] * area;
        double potassium = rates[2] * area;

        showResultCard(nitrogen, phosphorus, potassium, rates, area);
    }

    /**
     * Returns the index of the selected crop in the NPK_RATES array.
     * Matches by prefix since the full bilingual string is shown.
     */
    private int getCropIndex(String selectedCrop) {
        for (int i = 0; i < CROPS.length; i++) {
            if (selectedCrop.equalsIgnoreCase(CROPS[i]))
                return i;
        }
        return -1; // fallback
    }

    /**
     * Populates and animates the result section into view.
     * Result expands WITHIN the same screen — does NOT open a new Activity.
     */
    private void showResultCard(double nitrogen, double phosphorus,
            double potassium, double[] rates, double area) {
        // Populate result TextViews
        tvNitrogen.setText(String.format(Locale.getDefault(),
                "%s kg/acre → %.1f kg", formatDouble(rates[0]), nitrogen));
        tvPhosphorus.setText(String.format(Locale.getDefault(),
                "%s kg/acre → %.1f kg", formatDouble(rates[1]), phosphorus));
        tvPotassium.setText(String.format(Locale.getDefault(),
                "%s kg/acre → %.1f kg", formatDouble(rates[2]), potassium));

        tvTotalSummary.setText(String.format(Locale.getDefault(),
                "Total for %.1f acres / %.1f एकड़ के लिए:", area, area));
        tvTotalValues.setText(String.format(Locale.getDefault(),
                "N: %.1f kg   P: %.1f kg   K: %.1f kg",
                nitrogen, phosphorus, potassium));

        // Animate slide-down if not already visible
        if (resultSection.getVisibility() != View.VISIBLE) {
            resultSection.setVisibility(View.VISIBLE);
            Animation slideDown = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
            slideDown.setDuration(350);
            resultSection.startAnimation(slideDown);
        }
    }

    private String formatDouble(double value) {
        if (value == (long) value)
            return String.valueOf((long) value);
        return String.valueOf(value);
    }

    private void shareResult() {
        String crop = cropDropdown.getText().toString();
        String areaStr = areaInput.getText() != null ? areaInput.getText().toString() : "0";
        String n = tvNitrogen.getText().toString();
        String p = tvPhosphorus.getText().toString();
        String k = tvPotassium.getText().toString();

        String message = "🌾 Fertilizer Recommendation / उर्वरक अनुशंसा\n" +
                "Crop / फसल: " + crop + "\n" +
                "Area / क्षेत्र: " + areaStr + " acres\n\n" +
                "N: " + n + "\nP: " + p + "\nK: " + k + "\n\n" +
                "— Jai Kisan App";

        android.content.Intent shareIntent = new android.content.Intent(android.content.Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, message);
        startActivity(android.content.Intent.createChooser(shareIntent, "Share / साझा करें"));
    }
}
