package com.rcc.jai_kisan;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

public class IrrigationPlannerActivity extends AppCompatActivity {

    private AutoCompleteTextView dropdownCrop, dropdownSoil, dropdownMethod;
    private LinearLayout resultSection;
    private TextView tvResult;
    private View rootView;

    private String[] crops = {"Wheat / गेहूं", "Rice / धान", "Maize / मक्का", "Sugarcane / गन्ना"};
    private String[] soils = {"Clay / चिकनी मिट्टी", "Loamy / दोमट मिट्टी", "Sandy / रेतीली मिट्टी"};
    private String[] methods = {"Drip / ड्रिप", "Sprinkler / फव्वारा", "Flood / बाढ़ सिंचाई"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_irrigation_planner);

        rootView = findViewById(R.id.root_view);
        dropdownCrop = findViewById(R.id.dropdown_crop);
        dropdownSoil = findViewById(R.id.dropdown_soil);
        dropdownMethod = findViewById(R.id.dropdown_method);
        resultSection = findViewById(R.id.result_section);
        tvResult = findViewById(R.id.tv_irrigation_result);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Setup Dropdowns
        dropdownCrop.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, crops));
        dropdownSoil.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, soils));
        dropdownMethod.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, methods));

        findViewById(R.id.btn_calculate_irrigation).setOnClickListener(v -> calculateIrrigation());
    }

    private void calculateIrrigation() {
        String crop = dropdownCrop.getText().toString();
        String soil = dropdownSoil.getText().toString();

        if (crop.isEmpty() || soil.isEmpty()) {
            Snackbar.make(rootView, "Please select all fields / कृपया सभी चुनें", Snackbar.LENGTH_SHORT).show();
            return;
        }

        // Simple Logic: Base days based on soil, modified by crop
        int days = 7; // Default
        if (soil.contains("Clay")) days = 10;
        if (soil.contains("Sandy")) days = 4;
        if (crop.contains("Sugarcane") || crop.contains("Rice")) days -= 2;

        resultSection.setVisibility(View.VISIBLE);
        tvResult.setText("Recommended: Water every " + days + " days.\nअनुशंसा: हर " + days + " दिनों में सिंचाई करें।");
    }
}