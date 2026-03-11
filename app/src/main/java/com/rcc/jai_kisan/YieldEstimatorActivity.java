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
 * YieldEstimatorActivity
 *
 * Allows the user to select a crop and enter land area (in acres),
 * then estimates the expected yield in quintals.
 *
 * Design rules enforced:
 * - Toolbar: #E07854 (terracotta), NOT colorPrimary
 * - Screen background: #E5E5D8 (set in layout XML)
 * - Estimate button: pill-shape (50dp), match_parent, #1565C0 (Royal Blue)
 * - Validation error: Snackbar (NOT Toast or AlertDialog)
 * - Results: slide-down within same screen (NOT new Activity)
 */
public class YieldEstimatorActivity extends AppCompatActivity {

    // Views
    private View rootView;
    private AutoCompleteTextView cropDropdown;
    private TextInputEditText areaInput;
    private LinearLayout resultSection;
    private TextView tvYieldPerAcre, tvTotalYield, tvYieldSummary;

    // ─── Baseline yield in quintals/acre ─────────────────────────────────────
    // Source: ICAR / Ministry of Agriculture average yield data
    private static final double[] BASE_YIELD_QUINTALS_PER_ACRE = {
            16.0, // Wheat / गेहूं
            18.0, // Rice / चावल
            20.0, // Maize / मक्का
            10.0, // Cotton / कपास (seed cotton)
            280.0, // Sugarcane / गन्ना (high volume crop)
            8.0, // Soybean / सोयाबीन
            7.0, // Mustard / सरसों
            75.0 // Potato / आलू
    };

    // Bilingual crop names — MUST match NPK_RATES order in
    // FertilizerCalculatorActivity
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
        setContentView(R.layout.activity_yield_estimator);

        // Bind views
        rootView = findViewById(R.id.root_view);
        cropDropdown = findViewById(R.id.dropdown_crop);
        areaInput = findViewById(R.id.input_area);
        resultSection = findViewById(R.id.result_section);
        tvYieldPerAcre = findViewById(R.id.tv_yield_per_acre);
        tvTotalYield = findViewById(R.id.tv_total_yield);
        tvYieldSummary = findViewById(R.id.tv_yield_summary);

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

        // Estimate button
        MaterialButton btnEstimate = findViewById(R.id.btn_estimate);
        btnEstimate.setOnClickListener(v -> estimateYield());

        // Share button
        MaterialButton btnShare = findViewById(R.id.btn_share);
        btnShare.setOnClickListener(v -> shareResult());
    }

    /**
     * Validates inputs and triggers yield estimation.
     * Shows Snackbar on validation failure — NOT Toast or AlertDialog.
     */
    private void estimateYield() {
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

        // Look up base yield
        int cropIndex = getCropIndex(crop);
        double baseYield = (cropIndex >= 0) ? BASE_YIELD_QUINTALS_PER_ACRE[cropIndex] : 15.0;
        double totalYield = baseYield * area;

        showYieldResult(baseYield, totalYield, area, crop);
    }

    /**
     * Returns the index of the selected crop in the BASE_YIELD array.
     */
    private int getCropIndex(String selectedCrop) {
        for (int i = 0; i < CROPS.length; i++) {
            if (selectedCrop.equalsIgnoreCase(CROPS[i]))
                return i;
        }
        return -1;
    }

    /**
     * Populates and animates the result section into view.
     * Result expands WITHIN the same screen — does NOT open a new Activity.
     */
    private void showYieldResult(double baseYield, double totalYield,
            double area, String crop) {
        tvYieldPerAcre.setText(String.format(Locale.getDefault(),
                "%.1f quintals", baseYield));
        tvTotalYield.setText(String.format(Locale.getDefault(),
                "%.1f quintals", totalYield));
        tvYieldSummary.setText(String.format(Locale.getDefault(),
                "%s on %.1f acres\n%.1f quintals total estimated",
                crop, area, totalYield));

        // Animate slide-down if not already visible
        if (resultSection.getVisibility() != View.VISIBLE) {
            resultSection.setVisibility(View.VISIBLE);
            Animation slideDown = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
            slideDown.setDuration(350);
            resultSection.startAnimation(slideDown);
        }
    }

    private void shareResult() {
        String crop = cropDropdown.getText().toString();
        String areaStr = areaInput.getText() != null ? areaInput.getText().toString() : "0";
        String perAcre = tvYieldPerAcre.getText().toString();
        String total = tvTotalYield.getText().toString();

        String message = "🌾 Yield Estimate / उपज अनुमान\n" +
                "Crop / फसल: " + crop + "\n" +
                "Area / क्षेत्र: " + areaStr + " acres\n\n" +
                "Yield per Acre: " + perAcre + "\n" +
                "Total Yield: " + total + "\n\n" +
                "— Jai Kisan App";

        android.content.Intent shareIntent = new android.content.Intent(android.content.Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, message);
        startActivity(android.content.Intent.createChooser(shareIntent, "Share / साझा करें"));
    }
}
