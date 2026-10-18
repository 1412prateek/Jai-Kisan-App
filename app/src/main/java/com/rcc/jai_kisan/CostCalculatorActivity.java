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
    private com.google.android.material.textfield.TextInputLayout layoutSeed, layoutFertilizer, layoutLabor, layoutOther, layoutYield, layoutPrice;
    private TextView tvTitleProductionCosts, tvTitleExpectedHarvest;
    private com.google.android.material.button.MaterialButton btnCalculateCost, btnShare;
    private TextView tvNetProfit, tvMargin, tvBreakEven, tvAdviceTitle, tvAdviceDesc;
    private TextView tvDisclaimer;
    private View resultSection, rootView;
    private View resultPdfContainer;
    private MaterialToolbar toolbar;

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

        layoutSeed = findViewById(R.id.layout_seed);
        layoutFertilizer = findViewById(R.id.layout_fertilizer);
        layoutLabor = findViewById(R.id.layout_labor);
        layoutOther = findViewById(R.id.layout_other);
        layoutYield = findViewById(R.id.layout_yield);
        layoutPrice = findViewById(R.id.layout_price);

        tvTitleProductionCosts = findViewById(R.id.tv_title_production_costs);
        tvTitleExpectedHarvest = findViewById(R.id.tv_title_expected_harvest);
        btnCalculateCost = findViewById(R.id.btn_calculate_cost);
        btnShare = findViewById(R.id.btn_share);

        resultSection = findViewById(R.id.result_section);
        resultPdfContainer = resultSection;
        tvNetProfit = findViewById(R.id.tv_net_profit);
        tvMargin = findViewById(R.id.tv_margin);
        tvBreakEven = findViewById(R.id.tv_break_even);
        tvAdviceTitle = findViewById(R.id.tv_advice_title);
        tvAdviceDesc = findViewById(R.id.tv_advice_desc);
        tvDisclaimer = findViewById(R.id.tv_disclaimer);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        if (btnCalculateCost != null) {
            btnCalculateCost.setOnClickListener(v -> calculateFinancials());
        }
        if (btnShare != null) {
            btnShare.setOnClickListener(v -> shareReport());
        }
        // AdMob banner
        AdManager.loadBanner(this, (android.widget.FrameLayout) findViewById(R.id.adBannerContainer));

        // Dynamic linking with AI Photo Expert results
        String cropEn = getIntent().getStringExtra("crop_name_en");
        String cropHi = getIntent().getStringExtra("crop_name_hi");
        int survivalChance = getIntent().getIntExtra("survival_chance", -1);
        int healthScore = getIntent().getIntExtra("health_score", -1);

        if (cropEn != null && !cropEn.trim().isEmpty()) {
            toolbar.setSubtitle(LanguageManager.formatDual(this, cropEn.trim(), cropHi != null ? cropHi.trim() : ""));
        }

        if (cropEn != null && !cropEn.trim().isEmpty() && survivalChance != -1) {
            if (LanguageManager.isEnglishOnly(this)) {
                aiAdvice = "\n\n🤖 Real-time AI Insight:\n• Crop health score is " + (healthScore != -1 ? healthScore : "N/A") + "/100. "
                        + "Survival chance is " + survivalChance + "%.\n"
                        + "• We recommend adjusting your inputs and harvest expectations according to the crop status.";
            } else {
                LanguageManager.LangItem lItem = LanguageManager.getSelectedLanguageItem(this);
                aiAdvice = "\n\n🤖 Real-time AI Insight (" + lItem.englishName + "):\n• Crop health score: " + (healthScore != -1 ? healthScore : "N/A") + "/100, Survival chance: " + survivalChance + "%.\n"
                        + "• " + LanguageManager.getRegional(this, "safe_harvest_tip", "फसल स्वास्थ्य के अनुसार अपनी लागत और पैदावार की उम्मीदों को समायोजित करें।");
            }
        }

        localizeUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        localizeUI();
    }

    private void localizeUI() {
        if (toolbar != null) {
            toolbar.setTitle(LanguageManager.getDual(this, "Financial Pro", "cost_calculator"));
        }
        if (tvTitleProductionCosts != null) {
            tvTitleProductionCosts.setText("1. " + LanguageManager.getDual(this, "Production Costs", "production_costs"));
        }
        if (layoutSeed != null) {
            layoutSeed.setHint(LanguageManager.getDual(this, "Seed", "seed") + " (₹)");
        }
        if (layoutFertilizer != null) {
            layoutFertilizer.setHint(LanguageManager.getDual(this, "Fertilizer & Pesticide", "fertilizer") + " (₹)");
        }
        if (layoutLabor != null) {
            layoutLabor.setHint(LanguageManager.getDual(this, "Labor & Machinery", "labor") + " (₹)");
        }
        if (layoutOther != null) {
            layoutOther.setHint(LanguageManager.getDual(this, "Transport & Others", "other") + " (₹)");
        }
        if (tvTitleExpectedHarvest != null) {
            tvTitleExpectedHarvest.setText("2. " + LanguageManager.getDual(this, "Expected Harvest", "expected_harvest"));
        }
        if (layoutYield != null) {
            layoutYield.setHint(LanguageManager.getDual(this, "Total Yield", "total_yield") + " (Quintals)");
        }
        if (layoutPrice != null) {
            layoutPrice.setHint(LanguageManager.getDual(this, "Selling Price", "price") + " (₹ per Q)");
        }
        if (btnCalculateCost != null) {
            btnCalculateCost.setText(LanguageManager.getButtonLines(this, "Financial Analysis", "calculate_financials"));
        }
        if (btnShare != null) {
            btnShare.setText(LanguageManager.getButtonLines(this, "Share Report", "share_report"));
        }
        if (tvDisclaimer != null) {
            tvDisclaimer.setText(LanguageManager.getAppDisclaimer(this));
        }
    }

    private void calculateFinancials() {
        try {
            double totalCost = getVal(inputSeed) + getVal(inputFertilizer) + getVal(inputLabor) + getVal(inputOther);
            double yield = getVal(inputYield);
            double price = getVal(inputPrice);

            if (totalCost == 0 || yield == 0) {
                Snackbar.make(rootView, LanguageManager.getDual(this, "Enter all details", "please_fill_all"), Snackbar.LENGTH_SHORT).show();
                return;
            }

            double revenue = yield * price;
            double profit = revenue - totalCost;
            double margin = (profit / totalCost) * 100;
            double breakEvenPrice = totalCost / yield;

            displayAdvancedResults(profit, margin, breakEvenPrice);

        } catch (Exception e) {
            Snackbar.make(rootView, LanguageManager.getDual(this, "Invalid Input", "invalid_input"), Snackbar.LENGTH_SHORT).show();
        }
    }

    private double getVal(TextInputEditText input) {
        String s = input.getText().toString().trim();
        return s.isEmpty() ? 0 : Double.parseDouble(s);
    }

    private void displayAdvancedResults(double profit, double margin, double breakEven) {
        String profitLabel = profit >= 0 
                ? LanguageManager.getDual(this, "Net Profit", "net_profit") 
                : LanguageManager.getDual(this, "Loss", "loss");
        tvNetProfit.setText(String.format(Locale.getDefault(), "%s:\n₹%.0f", profitLabel, Math.abs(profit)));
        tvNetProfit.setTextColor(profit >= 0 ? 0xFF2E7D32 : 0xFFC62828);

        String marginLabel = LanguageManager.getDual(this, "Profit Margin", "profit_margin");
        tvMargin.setText(String.format(Locale.getDefault(), "%s: %.1f%%", marginLabel, margin));

        String breakEvenLabel = LanguageManager.getDual(this, "Break-Even Price", "break_even");
        tvBreakEven.setText(String.format(Locale.getDefault(), "%s: ₹%.0f / Quintal", breakEvenLabel, breakEven));

        if (margin < 5) {
            tvAdviceTitle.setText("⚠️ " + LanguageManager.getDual(this, "Warning", "warning_risk"));
            tvAdviceTitle.setTextColor(0xFFC62828);
            String tip = LanguageManager.formatDual(this, "Profit margin is very low. High risk detected.", LanguageManager.getRegional(this, "low_margin_tip", "मार्जिन बहुत कम है। जोखिम अधिक है।"));
            tvAdviceDesc.setText(tip + aiAdvice);
        } else if (margin <= 15) {
            tvAdviceTitle.setText("🟡 " + LanguageManager.getDual(this, "Medium Risk", "medium_risk"));
            tvAdviceTitle.setTextColor(0xFFF9A825);
            String tip = LanguageManager.formatDual(this, "Safe harvest but monitor costs closely.", LanguageManager.getRegional(this, "safe_harvest_tip", "सुरक्षित पैदावार है लेकिन खर्चों पर ध्यान दें।"));
            tvAdviceDesc.setText(tip + aiAdvice);
        } else {
            tvAdviceTitle.setText("✅ " + LanguageManager.getDual(this, "Good Crop Choice", "good_crop_choice"));
            tvAdviceTitle.setTextColor(0xFF2E7D32);
            String tip = LanguageManager.formatDual(this, "Excellent profit potential based on your inputs.", LanguageManager.getRegional(this, "excellent_margin_tip", "आपके इनपुट के आधार पर उत्कृष्ट लाभ की संभावना।"));
            tvAdviceDesc.setText(tip + aiAdvice);
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
                "    @page { size: A4; margin: 10mm 8mm; }\n" +
                "    body { font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; color: #333; margin: 0; padding: 0; }\n" +
                "    h1, h2, h3, h4, .section-title { page-break-after: avoid; break-after: avoid; }\n" +
                "    .meta-box, .results-table, .summary-box, .insight-container, .pdf-disclaimer { page-break-inside: avoid; break-inside: avoid; }\n" +
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
                LanguageManager.getPdfDisclaimerHtml(this) +
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
