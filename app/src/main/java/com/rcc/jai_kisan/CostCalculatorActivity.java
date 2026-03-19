package com.rcc.jai_kisan;

import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import java.util.Locale;

public class CostCalculatorActivity extends AppCompatActivity {

    private TextInputEditText inputSeed, inputFertilizer, inputLabor, inputOther, inputYield, inputPrice;
    private TextView tvNetProfit, tvMargin, tvBreakEven, tvAdviceTitle, tvAdviceDesc;
    // Added tvDisclaimer reference
    private TextView tvDisclaimer;
    private View resultSection, rootView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cost_calculator);

        rootView = findViewById(R.id.root_view);
        inputSeed = findViewById(R.id.input_seed);
        inputFertilizer = findViewById(R.id.input_fertilizer);
        inputLabor = findViewById(R.id.input_labor);
        inputOther = findViewById(R.id.input_other);
        inputYield = findViewById(R.id.input_yield);
        inputPrice = findViewById(R.id.input_price);

        resultSection = findViewById(R.id.result_section);
        tvNetProfit = findViewById(R.id.tv_net_profit);
        tvMargin = findViewById(R.id.tv_margin);
        tvBreakEven = findViewById(R.id.tv_break_even);
        tvAdviceTitle = findViewById(R.id.tv_advice_title);
        tvAdviceDesc = findViewById(R.id.tv_advice_desc);
        // Initialize disclaimer
        tvDisclaimer = findViewById(R.id.tv_disclaimer);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        findViewById(R.id.btn_calculate_cost).setOnClickListener(v -> calculateFinancials());

        findViewById(R.id.btn_share).setOnClickListener(v -> shareReport());

    }

    private void calculateFinancials() {
        try {
            double totalCost = getVal(inputSeed) + getVal(inputFertilizer) + getVal(inputLabor) + getVal(inputOther);
            double yield = getVal(inputYield);
            double price = getVal(inputPrice);

            if (totalCost == 0 || yield == 0) {
                Snackbar.make(rootView, "Enter all details / पूरी जानकारी दें", Snackbar.LENGTH_SHORT).show();
                return;
            }

            double revenue = yield * price;
            double profit = revenue - totalCost;
            double margin = (profit / totalCost) * 100;
            double breakEvenPrice = totalCost / yield;

            displayAdvancedResults(profit, margin, breakEvenPrice);

        } catch (Exception e) {
            Snackbar.make(rootView, "Invalid Input / गलत प्रविष्टि", Snackbar.LENGTH_SHORT).show();
        }
    }

    private double getVal(TextInputEditText input) {
        String s = input.getText().toString().trim();
        return s.isEmpty() ? 0 : Double.parseDouble(s);
    }

    private void displayAdvancedResults(double profit, double margin, double breakEven) {
        tvNetProfit.setText(String.format(Locale.getDefault(), "%s:\n₹%.0f",
                profit >= 0 ? "Net Profit / शुद्ध लाभ" : "Loss / हानि", Math.abs(profit)));
        tvNetProfit.setTextColor(profit >= 0 ? 0xFF2E7D32 : 0xFFC62828);

        tvMargin.setText(String.format(Locale.getDefault(), "Profit Margin / लाभ मार्जिन: %.1f%%", margin));
        tvBreakEven.setText(String.format(Locale.getDefault(), "Break-Even Price / सुरक्षित भाव: ₹%.0f / Quintal", breakEven));

        if (margin < 5) {
            tvAdviceTitle.setText("⚠️ Warning / चेतावनी");
            tvAdviceTitle.setTextColor(0xFFC62828);
            tvAdviceDesc.setText("Profit margin is very low. High risk detected.\nमार्जिन बहुत कम है। जोखिम अधिक है।");
        } else if (margin <= 15) {
            tvAdviceTitle.setText("🟡 Medium Risk / मध्यम जोखिम");
            tvAdviceTitle.setTextColor(0xFFF9A825);
            tvAdviceDesc.setText("Safe harvest but monitor costs closely.\nसुरक्षित पैदावार है लेकिन खर्चों पर ध्यान दें।");
        } else {
            tvAdviceTitle.setText("✅ Good Crop Choice / सही फसल चुनाव");
            tvAdviceTitle.setTextColor(0xFF2E7D32);
            tvAdviceDesc.setText("Excellent profit potential based on your inputs.\nआपके इनपुट के आधार पर उत्कृष्ट लाभ की संभावना।");
        }

        // Ensure disclaimer is visible when results are shown
        if (tvDisclaimer != null) {
            tvDisclaimer.setVisibility(View.VISIBLE);
        }

        resultSection.setVisibility(View.VISIBLE);
        resultSection.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
    }


    // Inside onCreate, add this line after your other findViewById calls:

    // Add this new method at the bottom of the class (before the last closing brace):
    private void shareReport() {
        String profitStr = tvNetProfit.getText().toString();
        String marginStr = tvMargin.getText().toString();
        String breakEvenStr = tvBreakEven.getText().toString();
        String advice = tvAdviceDesc.getText().toString();

        // Prevent sharing if results aren't calculated yet
        if (profitStr.isEmpty()) {
            Snackbar.make(rootView, "Please calculate first / पहले गणना करें", Snackbar.LENGTH_SHORT).show();
            return;
        }

        String shareMessage = "📊 *Jai Kisan - Financial Report*\n\n" +
                profitStr + "\n" +
                marginStr + "\n" +
                breakEvenStr + "\n\n" +
                "💡 *Advice:* " + advice + "\n\n" +
                "Calculated via Jai Kisan App";

        android.content.Intent shareIntent = new android.content.Intent(android.content.Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareMessage);
        startActivity(android.content.Intent.createChooser(shareIntent, "Share via"));
    }
}