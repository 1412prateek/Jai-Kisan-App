package com.rcc.jai_kisan;

import android.os.Bundle;
import android.util.Log;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class MandiPricesActivity extends AppCompatActivity {

    Spinner spinnerState, spinnerCity, spinnerCrop, spinnerSortField, spinnerSortOrder;
    MaterialButton btnDateFrom, btnDateTo, btnReset, btnGetPrices;
    TextView tvPriceResults;

    final String API_KEY = "579b464db66ec23bdd000001b83385380c804b697dd2203bf51007bc";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mandi_prices); // Ensure XML file name matches

        // 🔹 Initialize Views
        spinnerState = findViewById(R.id.spinnerState);
        spinnerCity = findViewById(R.id.spinnerCity);
        spinnerCrop = findViewById(R.id.spinnerCrop);
        spinnerSortField = findViewById(R.id.spinnerSortField);
        spinnerSortOrder = findViewById(R.id.spinnerSortOrder);

        btnDateFrom = findViewById(R.id.btnDateFrom);
        btnDateTo = findViewById(R.id.btnDateTo);
        btnReset = findViewById(R.id.btnReset);
        btnGetPrices = findViewById(R.id.btnGetPrices);

        tvPriceResults = findViewById(R.id.tvPriceResults);

        // 🔹 Populate State Spinner
        ArrayAdapter<CharSequence> stateAdapter = ArrayAdapter.createFromResource(
                this, R.array.states_array, android.R.layout.simple_spinner_item);
        stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerState.setAdapter(stateAdapter);

        // 🔹 Populate Crop Spinner
        String[] crops = {"Wheat", "Rice", "Onion", "Potato", "Tomato", "Maize"};
        ArrayAdapter<String> cropAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, crops);
        cropAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCrop.setAdapter(cropAdapter);

        // 🔹 Populate Sort Fields
        String[] sortFields = {"arrival_date", "min_price", "max_price", "modal_price"};
        ArrayAdapter<String> sortFieldAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, sortFields);
        sortFieldAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSortField.setAdapter(sortFieldAdapter);

        // 🔹 Populate Sort Order
        String[] sortOrders = {"asc", "desc"};
        ArrayAdapter<String> sortOrderAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, sortOrders);
        sortOrderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSortOrder.setAdapter(sortOrderAdapter);

        // 🔹 Load cities based on selected state
        spinnerState.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                String selectedState = spinnerState.getSelectedItem().toString();

                // Convert to lowercase, replace spaces and special chars for array name
                String arrayName = selectedState.toLowerCase()
                        .replace(" & ", "_")
                        .replace(" and ", "_")
                        .replace(" ", "_")
                        .replace("-", "_")
                        + "_cities";

                int citiesArrayId = getResources().getIdentifier(arrayName, "array", getPackageName());

                if (citiesArrayId != 0) {
                    ArrayAdapter<CharSequence> cityAdapter = ArrayAdapter.createFromResource(
                            MandiPricesActivity.this, citiesArrayId, android.R.layout.simple_spinner_item);
                    cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerCity.setAdapter(cityAdapter);
                } else {
                    spinnerCity.setAdapter(null);
                    Log.e("CITY_SPINNER", "No city array found for: " + arrayName);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // 🔹 Date Picker Buttons
        btnDateFrom.setOnClickListener(v -> showDatePicker(btnDateFrom));
        btnDateTo.setOnClickListener(v -> showDatePicker(btnDateTo));

        // 🔹 Reset Button
        btnReset.setOnClickListener(v -> {
            spinnerState.setSelection(0);
            spinnerCity.setAdapter(null);
            spinnerCrop.setSelection(0);
            spinnerSortField.setSelection(0);
            spinnerSortOrder.setSelection(0);
            btnDateFrom.setText("From");
            btnDateTo.setText("To");
            tvPriceResults.setText("Prices will be shown here.\nयहाँ कीमतें दिखाई जाएँगी।");
        });

        // 🔹 Fetch Prices Button
        btnGetPrices.setOnClickListener(v -> {
            String city = spinnerCity.getSelectedItem() != null
                    ? spinnerCity.getSelectedItem().toString() : "";
            String crop = spinnerCrop.getSelectedItem() != null
                    ? spinnerCrop.getSelectedItem().toString() : "";

            if (city.isEmpty() || crop.isEmpty()) {
                tvPriceResults.setText("⚠️ Please select both City and Crop.");
                return;
            }

            fetchPrices(city, crop);
        });
    }

    // 🔹 MaterialDatePicker Helper
    private void showDatePicker(MaterialButton button) {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Date")
                .build();
        datePicker.show(getSupportFragmentManager(), "date_picker");
        datePicker.addOnPositiveButtonClickListener(selection ->
                button.setText(datePicker.getHeaderText())
        );
    }

    // 🔹 Fetch prices from API
    private void fetchPrices(String city, String crop) {
        new Thread(() -> {
            try {
                String baseUrl = "https://api.data.gov.in/resource/9ef84268-d588-465a-a308-a864a43d0070";

                // ✅ Encode parameters to handle spaces like "Sri Ganganagar"
                String encodedCity = java.net.URLEncoder.encode(city.trim(), "UTF-8");
                String encodedCrop = java.net.URLEncoder.encode(crop.trim(), "UTF-8");

                String queryUrl = baseUrl +
                        "?api-key=" + API_KEY +
                        "&format=json" +
                        "&filters[district]=" + encodedCity +
                        "&filters[commodity]=" + encodedCrop;

                Log.d("API_URL", queryUrl);

                HttpURLConnection conn = (HttpURLConnection) new URL(queryUrl).openConnection();
                conn.setRequestMethod("GET");

                InputStream is = conn.getInputStream();
                Scanner scanner = new Scanner(is);
                StringBuilder builder = new StringBuilder();
                while (scanner.hasNext()) builder.append(scanner.nextLine());

                JSONObject response = new JSONObject(builder.toString());
                JSONArray records = response.getJSONArray("records");

                if (records.length() == 0) {
                    runOnUiThread(() -> tvPriceResults.setText("No data available for selected filters."));
                    return;
                }

                StringBuilder resultText = new StringBuilder();
                for (int i = 0; i < records.length(); i++) {
                    JSONObject item = records.getJSONObject(i);
                    resultText.append("📅 Date / तिथि: ").append(item.optString("arrival_date")).append("\n")
                            .append("🏬 Market / मंडी: ").append(item.optString("market")).append("\n")
                            .append("💰 Min / न्यूनतम: ₹").append(item.optString("min_price")).append(" | ")
                            .append("Max / अधिकतम: ₹").append(item.optString("max_price")).append(" | ")
                            .append("Modal / औसत: ₹").append(item.optString("modal_price")).append("\n\n");

                }

                runOnUiThread(() -> tvPriceResults.setText(resultText.toString()));

            } catch (Exception e) {
                Log.e("API_ERROR", "Error: ", e);
                runOnUiThread(() -> tvPriceResults.setText("Error fetching data."));
            }
        }).start();
    }
}

