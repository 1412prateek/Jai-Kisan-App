package com.rcc.jai_kisan;

import static android.content.Intent.getIntent;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import java.io.File;
import java.util.Locale;

public class CostCalculatorActivity extends BaseActivity {

    private TextInputEditText inputSeed, inputFertilizer, inputLabor, inputOther, inputYield, inputPrice;
    private TextView tvNetProfit, tvMargin, tvBreakEven, tvAdviceTitle, tvAdviceDesc;
    private TextView tvDisclaimer;
    private View resultSection, rootView;
    private View resultPdfContainer;

    // Class variable to hold the dynamic AI advice
    private String aiAdvice = "";

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
        resultPdfContainer = resultSection;
        tvNetProfit = findViewById(R.id.tv_net_profit);
        tvMargin = findViewById(R.id.tv_margin);
        tvBreakEven = findViewById(R.id.tv_break_even);
        tvAdviceTitle = findViewById(R.id.tv_advice_title);
        tvAdviceDesc = findViewById(R.id.tv_advice_desc);
        tvDisclaimer = findViewById(R.id.tv_disclaimer);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        findViewById(R.id.btn_calculate_cost).setOnClickListener(v -> calculateFinancials());
        findViewById(R.id.btn_share).setOnClickListener(v -> shareReport());
        // AdMob banner
        AdManager.loadBanner(this, (android.widget.FrameLayout) findViewById(R.id.adBannerContainer));

        // Dynamic linking with AI Photo Expert results
        String cropEn = getIntent().getStringExtra("crop_name_en");
        String cropHi = getIntent().getStringExtra("crop_name_hi");
        int survivalChance = getIntent().getIntExtra("survival_chance", -1);
        int healthScore = getIntent().getIntExtra("health_score", -1);

        if (cropEn != null && !cropEn.trim().isEmpty()) {
            toolbar.setSubtitle(cropEn + (cropHi != null && !cropHi.trim().isEmpty() ? " / " + cropHi.trim() : ""));
        }

        if (cropEn != null && !cropEn.trim().isEmpty() && survivalChance != -1) {
            // Keep input fields empty for the user to fill. No pre-filling.

            // Construct beautifully bilingual AI Insight
            aiAdvice = "\n\n\uD83E\uDDFE Real-time AI Insight:\nâ€¢ Crop health score is " + (healthScore != -1 ? healthScore : "N/A") + "/100. "
                    + "Survival chance is " + survivalChance + "%.\n"
                    + "â€¢ We recommend adjusting your inputs and harvest expectations according to the crop status.\n\n"
                    + "\uD83E\uDDFE \u0935\u093e\u0938\u094d\u0924\u0935\u093f\u0915 \u0938\u092e\u092f \u090f\u0906\u0908 \u0907\u0928\u0938\u093e\u0907\u091f:\nâ€¢ \u092b\u0938\u0932 \u0938\u094d\u0935\u093e\u0938\u094d\u0925\u094d\u092f \u0938\u094d\u0915\u094b\u0930 " + (healthScore != -1 ? healthScore : "--") + "/100 \u0939\u0948\u0964 "
                    + "\u091c\u0940\u0935\u093f\u0924 \u0930\u0939\u0928\u0947 \u0915\u0940 \u0938\u0902\u092d\u093e\u0935\u0928\u093e " + survivalChance + "% \u0939\u0948\u0964\n"
                    + "\u2022 \u0939\u092e \u0905\u0928\u0941\u0936\u0902\u0938\u093e \u0915\u0930\u0924\u0947 \u0939\u0948\u0902 \u0915\u093f \u0906\u092a \u092b\u0938\u0932 \u0915\u0940 \u0938\u094d\u0925\u093f\u0924\u093f \u0915\u0947 \u0905\u0928\u0941\u0938\u093e\u0930 \u0905\u092a\u0928\u0940 \u0932\u093e\u0917\u0924 \u0914\u0930 \u092a\u0948\u0926\u093e\u0935\u093e\u0930 \u0915\u0940 \u0909\u092e\u094d\u092e\u0940\u0926\u094b\u0902 \u0915\u094b \u0938\u092e\u093e\u092f\u094b\u091c\u093f\u0924 \u0915\u0930\u0947\u0902\u0964";
        }
    }

    private void calculateFinancials() {
        try {
            double totalCost = getVal(inputSeed) + getVal(inputFertilizer) + getVal(inputLabor) + getVal(inputOther);
            double yield = getVal(inputYield);
            double price = getVal(inputPrice);

            if (totalCost == 0 || yield == 0) {
                Snackbar.make(rootView, "Enter all details / \u092a\u0942\u0930\u0940 \u091c\u093e\u0928\u0915\u093e\u0930\u0940 \u0926\u0947\u0902", Snackbar.LENGTH_SHORT).show();
                return;
            }

            double revenue = yield * price;
            double profit = revenue - totalCost;
            double margin = (profit / totalCost) * 100;
            double breakEvenPrice = totalCost / yield;

            displayAdvancedResults(profit, margin, breakEvenPrice);

        } catch (Exception e) {
            Snackbar.make(rootView, "Invalid Input / \u0917\u0932\u0924 \u092a\u094d\u0930\u0935\u093f\u0937\u094d\u091f\u093f", Snackbar.LENGTH_SHORT).show();
        }
    }

    private double getVal(TextInputEditText input) {
        String s = input.getText().toString().trim();
        return s.isEmpty() ? 0 : Double.parseDouble(s);
    }

    private void displayAdvancedResults(double profit, double margin, double breakEven) {
        tvNetProfit.setText(String.format(Locale.getDefault(), "%s:\n\u20B9%.0f",
                profit >= 0 ? "Net Profit / \u0936\u0941\u0926\u094d\u0927 \u0932\u093e\u092d" : "Loss / \u0939\u093e\u0928\u093f", Math.abs(profit)));
        tvNetProfit.setTextColor(profit >= 0 ? 0xFF2E7D32 : 0xFFC62828);

        tvMargin.setText(String.format(Locale.getDefault(), "Profit Margin / \u0932\u093e\u092d \u092e\u093e\u0930\u094d\u091c\u093f\u0928: %.1f%%", margin));
        tvBreakEven.setText(String.format(Locale.getDefault(), "Break-Even Price / \u0938\u0941\u0930\u0915\u094d\u0937\u093f\u0924 \u092d\u093e\u0935: \u20B9%.0f / Quintal", breakEven));

        if (margin < 5) {
            tvAdviceTitle.setText("\u26A0\uFE0F Warning / \u091a\u0947\u0924\u093e\u0935\u0928\u0940");
            tvAdviceTitle.setTextColor(0xFFC62828);
            tvAdviceDesc.setText("Profit margin is very low. High risk detected.\n\u092e\u093e\u0930\u094d\u091c\u093f\u0928 \u092c\u0939\u0941\u0924 \u0915\u092e \u0939\u0948\u0964 \u091c\u094b\u0916\u093f\u092e \u0905\u0927\u093f\u0915 \u0939\u0948\u0964" + aiAdvice);
        } else if (margin <= 15) {
            tvAdviceTitle.setText("\uD83D\uDFE1 Medium Risk / \u092e\u0927\u094d\u092f\u092e \u091c\u094b\u0916\u093f\u092e");
            tvAdviceTitle.setTextColor(0xFFF9A825);
            tvAdviceDesc.setText("Safe harvest but monitor costs closely.\n\u0938\u0941\u0930\u0915\u094d\u0937\u093f\u0924 \u092a\u0948\u0926\u093e\u0935\u093e\u0930 \u0939\u0948 \u0932\u0947\u0915\u093f\u0928 \u0916\u0930\u094d\u091a\u094b\u0902 \u092a\u0930 \u0927\u094d\u092f\u093e\u0928 \u0926\u0947\u0902\u0964" + aiAdvice);
        } else {
            tvAdviceTitle.setText("\u2705 Good Crop Choice / \u0938\u0939\u0940 \u092b\u0938\u0932 \u091a\u0941\u0928\u093e\u0935");
            tvAdviceTitle.setTextColor(0xFF2E7D32);
            tvAdviceDesc.setText("Excellent profit potential based on your inputs.\n\u0906\u092a\u0915\u0947 \u0907\u0928\u092a\u0941\u091f \u0915\u0947 \u0905\u0927\u093e\u0930 \u092a\u0930 \u0909\u0924\u094d\u0915\u0943\u0937\u094d\u091f \u0932\u093e\u092d \u0915\u0940 \u0938\u0902\u092d\u093e\u0935\u0928\u093e\u0964" + aiAdvice);
        }

        if (tvDisclaimer != null) {
            tvDisclaimer.setVisibility(View.VISIBLE);
        }

        resultSection.setVisibility(View.VISIBLE);
        resultSection.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
    }

    private void shareReport() {
        String profitStr = tvNetProfit.getText().toString();
        if (profitStr.isEmpty()) {
            Snackbar.make(rootView, "Please calculate first / \u092a\u093count\u0947 \u0915\u0930\u0947\u0902", Snackbar.LENGTH_SHORT).show();
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
        String cropEn = getIntent().getStringExtra("crop_name_en");
        String cropHi = getIntent().getStringExtra("crop_name_hi");
        String crop = LanguageManager.getLocalizedCropName(this, cropEn, cropHi);

        StringBuilder sb = new StringBuilder();
        sb.append("*").append(LanguageManager.getDual(this, "Jai Kisan - Financial Report", "financial_pro_report")).append("*\n");
        sb.append("----------------------------------\n");
        sb.append(LanguageManager.getDual(this, "Crop", "crop")).append(": ").append(crop).append("\n\n");

        sb.append("*").append(LanguageManager.getDual(this, "Production Costs", "cost_breakdown")).append(":*\n");
        sb.append("- ").append(LanguageManager.getDual(this, "Seed", "seed")).append(": ₹").append(inputSeed.getText().toString()).append("\n");
        sb.append("- ").append(LanguageManager.getDual(this, "Fertilizer", "fertilizer")).append(": ₹").append(inputFertilizer.getText().toString()).append("\n");
        sb.append("- ").append(LanguageManager.getDual(this, "Labor", "labor")).append(": ₹").append(inputLabor.getText().toString()).append("\n");
        sb.append("- ").append(LanguageManager.getDual(this, "Other", "other")).append(": ₹").append(inputOther.getText().toString()).append("\n\n");

        sb.append("*").append(LanguageManager.getDual(this, "Harvest Estimation", "harvest_estimation")).append(":*\n");
        sb.append("- ").append(LanguageManager.getDual(this, "Total Yield", "total_yield")).append(": ").append(inputYield.getText().toString()).append(" Quintals\n");
        sb.append("- ").append(LanguageManager.getDual(this, "Price", "price")).append(": ₹").append(inputPrice.getText().toString()).append(" / Quintal\n\n");

        sb.append("*").append(LanguageManager.getDual(this, "Financial Analysis", "net_profit")).append(":*\n");
        sb.append("- ").append(tvNetProfit.getText().toString()).append("\n");
        sb.append("- ").append(tvMargin.getText().toString()).append("\n");
        sb.append("- ").append(tvBreakEven.getText().toString()).append("\n\n");

        sb.append("*").append(LanguageManager.getDual(this, "Advisory & Insights", "ai_advisory")).append(":*\n");
        sb.append(tvAdviceTitle.getText().toString()).append("\n");
        sb.append(tvAdviceDesc.getText().toString()).append("\n\n");

        sb.append(LanguageManager.getRegional(this, "generated_via", "Generated via Jai Kisan App"));

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, sb.toString());
        startActivity(Intent.createChooser(shareIntent, LanguageManager.getDual(this, "Share via", "share_report")));
    }

    private void shareAsPdf() {
        String cropEn = getIntent().getStringExtra("crop_name_en");
        String cropHi = getIntent().getStringExtra("crop_name_hi");
        String crop = LanguageManager.getLocalizedCropName(this, cropEn, cropHi);

        double totalCost = getVal(inputSeed) + getVal(inputFertilizer) + getVal(inputLabor) + getVal(inputOther);
        double yield = getVal(inputYield);
        double price = getVal(inputPrice);
        double revenue = yield * price;

        String appTitle = LanguageManager.getDual(this, "Jai Kisan", "jai_kisan");
        String reportTitle = LanguageManager.getDual(this, "Financial Pro Report", "financial_pro_report");
        String cropLabel = LanguageManager.getDual(this, "Crop", "crop");
        String totalCostLabel = LanguageManager.getDual(this, "Total Cost", "total_cost");
        String costBreakdownLabel = LanguageManager.getDual(this, "Cost Breakdown", "cost_breakdown");
        String harvestLabel = LanguageManager.getDual(this, "Harvest Estimation", "harvest_estimation");
        String seedLabel = LanguageManager.getDual(this, "Seed", "seed");
        String fertLabel = LanguageManager.getDual(this, "Fertilizer", "fertilizer");
        String laborLabel = LanguageManager.getDual(this, "Labor", "labor");
        String otherLabel = LanguageManager.getDual(this, "Other", "other");
        String yieldLabel = LanguageManager.getDual(this, "Total Yield", "total_yield");
        String priceLabel = LanguageManager.getDual(this, "Price", "price");
        String revLabel = LanguageManager.getDual(this, "Total Revenue", "total_revenue");
        String footerText = LanguageManager.getRegional(this, "generated_via", "Generated via Jai Kisan App &bull; Supporting Indian Agriculture");

        String htmlContent = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "<meta charset=\"UTF-8\">\n" +
                "<style>\n" +
                "    body { font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; color: #333; margin: 20px; }\n" +
                "    .header { border-bottom: 3px solid #E64A19; padding-bottom: 10px; margin-bottom: 20px; display: flex; justify-content: space-between; align-items: center; }\n" +
                "    .app-title { color: #E64A19; font-size: 28px; font-weight: bold; margin: 0; }\n" +
                "    .report-title { font-size: 14px; text-transform: uppercase; color: #666; letter-spacing: 1px; }\n" +
                "    \n" +
                "    .meta-box { background: #F5F5F5; border: 1px solid #E0E0E0; border-radius: 6px; padding: 12px; margin-bottom: 20px; display: grid; grid-template-columns: repeat(2, 1fr); gap: 10px; }\n" +
                "    .meta-item { font-size: 13px; }\n" +
                "    .meta-item strong { color: #E64A19; display: block; margin-bottom: 2px; }\n" +
                "\n" +
                "    .results-table { width: 100%; border-collapse: collapse; margin-bottom: 20px; }\n" +
                "    .results-table th { background: #E64A19; color: white; text-align: left; padding: 10px; font-size: 14px; }\n" +
                "    .results-table td { border: 1px solid #E0E0E0; padding: 12px; font-size: 13px; vertical-align: top; }\n" +
                "    .results-table tr:nth-child(even) { background: #FBFBFB; }\n" +
                "\n" +
                "    .summary-box { background: #FFF3E0; border-left: 4px solid #E64A19; padding: 12px; border-radius: 4px; margin-bottom: 25px; font-size: 13px; }\n" +
                "    \n" +
                "    .insight-container { border-top: 2px dashed #DDD; padding-top: 15px; }\n" +
                "    .lang-block { font-size: 12px; line-height: 1.6; }\n" +
                "    .lang-block h3 { color: #E64A19; font-size: 14px; margin-top: 0; margin-bottom: 8px; border-bottom: 1px solid #DDD; padding-bottom: 4px; }\n" +
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
                "    <div class=\"meta-item\"><strong>" + totalCostLabel + "</strong>&#8377;" + String.format(Locale.getDefault(), "%.0f", totalCost) + "</div>\n" +
                "</div>\n" +
                "\n" +
                "<table class=\"results-table\">\n" +
                "    <thead>\n" +
                "        <tr>\n" +
                "            <th style=\"width: 50%;\">" + costBreakdownLabel + "</th>\n" +
                "            <th style=\"width: 50%;\">" + harvestLabel + "</th>\n" +
                "        </tr>\n" +
                "    </thead>\n" +
                "    <tbody>\n" +
                "        <tr>\n" +
                "            <td>\n" +
                "                &bull; " + seedLabel + ": &#8377;" + inputSeed.getText().toString() + "<br>\n" +
                "                &bull; " + fertLabel + ": &#8377;" + inputFertilizer.getText().toString() + "<br>\n" +
                "                &bull; " + laborLabel + ": &#8377;" + inputLabor.getText().toString() + "<br>\n" +
                "                &bull; " + otherLabel + ": &#8377;" + inputOther.getText().toString() + "\n" +
                "            </td>\n" +
                "            <td>\n" +
                "                &bull; " + yieldLabel + ": " + inputYield.getText().toString() + " Q<br>\n" +
                "                &bull; " + priceLabel + ": &#8377;" + inputPrice.getText().toString() + " / Q<br>\n" +
                "                &bull; " + revLabel + ": &#8377;" + String.format(Locale.getDefault(), "%.0f", revenue) + "\n" +
                "            </td>\n" +
                "        </tr>\n" +
                "    </tbody>\n" +
                "</table>\n" +
                "\n" +
                "<div class=\"summary-box\">\n" +
                "    <strong>" + tvNetProfit.getText().toString() + "</strong><br>\n" +
                "    " + tvMargin.getText().toString() + "<br>\n" +
                "    " + tvBreakEven.getText().toString() + "\n" +
                "</div>\n" +
                "\n" +
                "<div class=\"insight-container\">\n" +
                "    <div class=\"lang-block\">\n" +
                "        <h3>" + tvAdviceTitle.getText().toString() + "</h3>\n" +
                "        <p style=\"white-space: pre-line;\">" + tvAdviceDesc.getText().toString() + "</p>\n" +
                "    </div>\n" +
                "</div>\n" +
                "\n" +
                "<div class=\"footer\">\n" +
                "    " + footerText + "\n" +
                "</div>\n" +
                "\n" +
                "</body>\n" +
                "</html>";

        HtmlToPdfManager.generatePdfFromHtml(this, htmlContent, "JaiKisan_Cost_Report");
    }
}
