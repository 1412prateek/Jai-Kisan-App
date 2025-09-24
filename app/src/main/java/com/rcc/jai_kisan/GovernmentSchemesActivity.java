package com.rcc.jai_kisan;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GovernmentSchemesActivity extends AppCompatActivity {

    private AutoCompleteTextView spinnerYear, spinnerPeriod;
    private TextView tvBeneficiaryResult;

    // Map<Year, Map<Period, Count>>
    private final Map<String, Map<String, String>> beneficiaryMap = new LinkedHashMap<>();
    private final Map<String, List<String>> yearToPeriods = new LinkedHashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_government_schemes);

        spinnerYear = findViewById(R.id.spinnerYear);
        spinnerPeriod = findViewById(R.id.spinnerPeriod);
        tvBeneficiaryResult = findViewById(R.id.tvBeneficiaryResult);

        parseJsonAndSetupSpinners();
    }

    private void parseJsonAndSetupSpinners() {
        try {
            // Read JSON from raw folder
            InputStream is = getResources().openRawResource(R.raw.pmkisan);
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();

            String jsonString = new String(buffer, StandardCharsets.UTF_8);
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray dataArray = jsonObject.getJSONArray("data");

            // Parse JSON and store in maps
            for (int i = 0; i < dataArray.length(); i++) {
                JSONArray record = dataArray.getJSONArray(i);
                String year = record.getString(0).trim();
                String period = record.getString(1).trim();
                String count = record.getString(2).trim();

                if (!beneficiaryMap.containsKey(year)) {
                    beneficiaryMap.put(year, new LinkedHashMap<>());
                    yearToPeriods.put(year, new ArrayList<>());
                }
                beneficiaryMap.get(year).put(period, count);
                yearToPeriods.get(year).add(period);
            }

            // ✅ Year Spinner Setup
            List<String> yearList = new ArrayList<>(beneficiaryMap.keySet());
            ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_dropdown_item_1line, yearList);
            spinnerYear.setAdapter(yearAdapter);

            // On selecting year
            spinnerYear.setOnItemClickListener((parent, view, position, id) -> {
                String selectedYear = yearList.get(position);
                List<String> periodList = yearToPeriods.get(selectedYear);

                if (periodList == null) periodList = new ArrayList<>();

                ArrayAdapter<String> periodAdapter = new ArrayAdapter<>(this,
                        android.R.layout.simple_dropdown_item_1line, periodList);
                spinnerPeriod.setAdapter(periodAdapter);

                // Reset and open dropdown
                spinnerPeriod.setText("", false);
                spinnerPeriod.showDropDown();

                tvBeneficiaryResult.setText("Select a period to view beneficiaries.\nअवधि चुनें।");
            });

            // ✅ On selecting period
            spinnerPeriod.setOnItemClickListener((parent, view, position, id) -> {
                String selectedYear = spinnerYear.getText().toString().trim();
                String selectedPeriod = spinnerPeriod.getText().toString().trim();

                if (beneficiaryMap.containsKey(selectedYear)) {
                    String count = beneficiaryMap.get(selectedYear).get(selectedPeriod);
                    if (count != null) {
                        tvBeneficiaryResult.setText(
                                "Beneficiary Count: " + count +
                                        "\nलाभार्थी संख्या: " + count
                        );
                    } else {
                        tvBeneficiaryResult.setText(
                                "No data found for this selection.\nइस चयन के लिए डेटा उपलब्ध नहीं है।"
                        );
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            tvBeneficiaryResult.setText("Error loading data / डेटा लोड करने में त्रुटि");
        }


        // 1️⃣ PM-Kisan
        TextView linkPmKisan = findViewById(R.id.linkPmKisan);
        linkPmKisan.setOnClickListener(v -> {
            String url = "https://pmkisan.gov.in/";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        });

// 2️⃣   Kisan Credit Card
        TextView linkKcc = findViewById(R.id.linkKcc);
        linkKcc.setOnClickListener(v -> {
            String url = "https://www.myscheme.gov.in/schemes/kcc";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        });

// 3️⃣ PM Fasal Bima
        TextView linkFasalBima = findViewById(R.id.linkFasalBima);
        linkFasalBima.setOnClickListener(v -> {
            String url = "https://pmfby.gov.in/";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        });

// 4️⃣ Soil Health Card
        TextView linkSoilCard = findViewById(R.id.linkSoilCard);
        linkSoilCard.setOnClickListener(v -> {
            String url = "https://soilhealth.dac.gov.in/";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        });

// 5️⃣ PM Krishi Sinchai
        TextView linkSinchai = findViewById(R.id.linkSinchai);
        linkSinchai.setOnClickListener(v -> {
            String url = "https://pmksy.gov.in/";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        });

// 6️⃣ e-NAM
        TextView linkENam = findViewById(R.id.linkENam);
        linkENam.setOnClickListener(v -> {
            String url = "https://enam.gov.in/";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        });

// 7️⃣ Paramparagat Krishi Vikas Yojana
        TextView linkParamparagat = findViewById(R.id.linkParamparagat);
        linkParamparagat.setOnClickListener(v -> {
            String url = "https://www.myscheme.gov.in/schemes/pkvy";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        });

// 8️⃣ PM-KUSUM
        TextView linkPmKusum = findViewById(R.id.linkPmKusum);
        linkPmKusum.setOnClickListener(v -> {
            String url = "https://mnre.gov.in/pm-kusum/";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        });

// 9️⃣ Rashtriya Krishi Vikas Yojana
        TextView linkRkvy = findViewById(R.id.linkRkvy);
        linkRkvy.setOnClickListener(v -> {
            String url = "https://rkvy.nic.in/";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        });

// 🔟 National Food Security Mission
        TextView linkNfsm = findViewById(R.id.linkNfsm);
        linkNfsm.setOnClickListener(v -> {
            String url = "https://nfsm.gov.in/";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        });


    }
}
