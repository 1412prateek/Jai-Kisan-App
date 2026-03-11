package com.rcc.jai_kisan;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

public class CostCalculatorActivity extends AppCompatActivity {

    private TextInputEditText inputSeed, inputLabor, inputFuel;
    private TextView tvTotal;
    private View rootView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cost_calculator);

        rootView = findViewById(R.id.root_view);
        inputSeed = findViewById(R.id.input_seed_cost);
        inputLabor = findViewById(R.id.input_labor_cost);
        inputFuel = findViewById(R.id.input_fuel_cost);
        tvTotal = findViewById(R.id.tv_total_cost_result);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        findViewById(R.id.btn_calculate_cost).setOnClickListener(v -> calculateTotal());
    }

    private void calculateTotal() {
        try {
            double seed = Double.parseDouble(inputSeed.getText().toString());
            double labor = Double.parseDouble(inputLabor.getText().toString());
            double fuel = Double.parseDouble(inputFuel.getText().toString());

            double total = seed + labor + fuel;
            tvTotal.setVisibility(View.VISIBLE);
            tvTotal.setText("Total Cost / कुल लागत: ₹" + String.format("%.2f", total));

        } catch (NumberFormatException e) {
            Snackbar.make(rootView, "Enter valid numbers / मान्य अंक दर्ज करें", Snackbar.LENGTH_SHORT).show();
        }
    }
}