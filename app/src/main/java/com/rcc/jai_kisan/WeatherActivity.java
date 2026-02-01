package com.rcc.jai_kisan;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;

import org.json.JSONObject;

import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.HttpsURLConnection;

public class WeatherActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    TextView tvTemperature, tvDescription, tvHumidity, tvWind, tvLocation;
    ImageView ivWeatherIcon;
    Button btnDetectLocation;
    AutoCompleteTextView spinnerState, citySpinner;
    MaterialCardView cardResult;

    String API_KEY = "1cce38c2c68cf1fb412a1c2cb29c40e7";
    private FusedLocationProviderClient fusedLocationClient;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        executorService = Executors.newSingleThreadExecutor();

        // --- Setup Toolbar ---
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // --- Find Views ---
        tvTemperature = findViewById(R.id.tvTemperature);
        tvDescription = findViewById(R.id.tvDescription);
        tvHumidity = findViewById(R.id.tvHumidity);
        tvWind = findViewById(R.id.tvWind);
        tvLocation = findViewById(R.id.tvLocation);
        ivWeatherIcon = findViewById(R.id.ivWeatherIcon);
        btnDetectLocation = findViewById(R.id.btnDetectLocation);
        spinnerState = findViewById(R.id.spinnerState);
        citySpinner = findViewById(R.id.citySpinner);
        cardResult = findViewById(R.id.cardResult);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        setupStateCitySpinners();

        btnDetectLocation.setOnClickListener(v -> detectLocation());

        // Fetch weather for a default location on startup
        fetchWeather("Delhi");
    }

    private void setupStateCitySpinners() {
        ArrayAdapter<CharSequence> stateAdapter = ArrayAdapter.createFromResource(
                this, R.array.states_array, android.R.layout.simple_dropdown_item_1line);
        spinnerState.setAdapter(stateAdapter);

        spinnerState.setOnItemClickListener((parent, view, position, id) -> {
            String selectedStateFull = (String) parent.getItemAtPosition(position);
            // "Bihar / बिहार" -> "Bihar"
            String selectedStateEnglish = selectedStateFull.split("/")[0].trim();

            citySpinner.setText("", false);
            citySpinner.setAdapter(null);

            String arrayName = selectedStateEnglish.toLowerCase()
                    .replace(" & ", "_")
                    .replace(" and ", "_")
                    .replace("-", "_")
                    .replace(" ", "_") + "_cities";

            int cityArrayId = getResources().getIdentifier(arrayName, "array", getPackageName());
            if (cityArrayId != 0) {
                ArrayAdapter<CharSequence> cityAdapter = ArrayAdapter.createFromResource(
                        WeatherActivity.this, cityArrayId, android.R.layout.simple_dropdown_item_1line);
                citySpinner.setAdapter(cityAdapter);
            }
        });

        citySpinner.setOnItemClickListener((parent, view, position, id) -> {
            String cityFull = (String) parent.getItemAtPosition(position);
            // "Bhagalpur / भागलपुर" -> "Bhagalpur"
            String cityEnglish = cityFull.split("/")[0].trim();
            fetchWeather(cityEnglish);
        });
    }

    private void detectLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                getCityFromLocation(location);
            } else {
                Toast.makeText(this, "Location not available. Please enable GPS.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void getCityFromLocation(Location loc) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            Address address = geocoder.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1).get(0);
            String cityName = address.getLocality();
            if (cityName != null) {
                fetchWeather(cityName);
            } else {
                Toast.makeText(this, "Unable to detect city from your location.", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error getting city name.", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchWeather(String city) {
        String cleanCity = city.trim();
        executorService.execute(() -> {
            try {
                String urlString = "https://api.openweathermap.org/data/2.5/weather?q=" + cleanCity + "&appid=" + API_KEY + "&units=metric";
                URL url = new URL(urlString);
                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                Scanner scanner = new Scanner(connection.getInputStream());
                StringBuilder result = new StringBuilder();
                while (scanner.hasNext()) result.append(scanner.nextLine());

                JSONObject json = new JSONObject(result.toString());

                String temp = json.getJSONObject("main").getString("temp") + "°C";
                String descEng = json.getJSONArray("weather").getJSONObject(0).getString("description");
                String iconCode = json.getJSONArray("weather").getJSONObject(0).getString("icon");
                String humidityValue = json.getJSONObject("main").getString("humidity");
                String windSpeedValue = json.getJSONObject("wind").getString("speed");

                String cityName = json.getString("name");
                String country = json.getJSONObject("sys").getString("country");
                String locationText = cityName + ", " + country;

                Map<String, String> weatherTranslations = new HashMap<>();
                weatherTranslations.put("clear sky", "साफ आसमान");
                weatherTranslations.put("few clouds", "थोड़े बादल");
                weatherTranslations.put("scattered clouds", "छितरे बादल");
                weatherTranslations.put("broken clouds", "टूटे बादल");
                weatherTranslations.put("overcast clouds", "बादलों से ढका");
                weatherTranslations.put("shower rain", "तेज़ बारिश");
                weatherTranslations.put("rain", "बारिश");
                weatherTranslations.put("light rain", "हल्की बारिश");
                weatherTranslations.put("moderate rain", "मध्यम बारिश");
                weatherTranslations.put("thunderstorm", "आंधी-तूफान");
                weatherTranslations.put("snow", "बर्फबारी");
                weatherTranslations.put("mist", "धुंध");
                weatherTranslations.put("haze", "धुंध");

                String descHindi = weatherTranslations.getOrDefault(descEng.toLowerCase(), "");
                String fullDescription = descEng.substring(0, 1).toUpperCase() + descEng.substring(1) + (descHindi.isEmpty() ? "" : " / " + descHindi);

                runOnUiThread(() -> {
                    cardResult.setVisibility(View.VISIBLE);
                    tvTemperature.setText(temp);
                    tvDescription.setText(fullDescription);
                    tvHumidity.setText(humidityValue + "%");
                    tvWind.setText(windSpeedValue + " m/s");
                    tvLocation.setText(locationText);

                    // Set the icon using the improved logic
                    setWeatherIcon(iconCode);
                });

            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(WeatherActivity.this, "Could not fetch weather for " + cleanCity, Toast.LENGTH_SHORT).show());
                e.printStackTrace();
            }
        });
    }

    // ✅ FIXED ICON LOGIC: Uses your custom icons for Day/Night and specific conditions
    private void setWeatherIcon(String iconCode) {
        int iconResId = R.drawable.ic_weather_cloud; // Default fallback

        // 1. Clear Sky (01)
        if (iconCode.contains("01")) {
            if (iconCode.contains("n")) {
                iconResId = R.drawable.ic_weather_moon; // Night -> Moon
            } else {
                iconResId = R.drawable.ic_weather_sun;  // Day -> Sun
            }
        }
        // 2. Few Clouds (02) - Distinct Day/Night icons
        else if (iconCode.contains("02")) {
            if (iconCode.contains("n")) {
                iconResId = R.drawable.ic_weather_partly_cloudy_night; // Night Clouds
            } else {
                iconResId = R.drawable.ic_weather_partly_cloudy_day;   // Day Clouds
            }
        }
        // 3. Scattered/Broken Clouds (03, 04)
        else if (iconCode.contains("03") || iconCode.contains("04")) {
            iconResId = R.drawable.ic_weather_cloud;
        }
        // 4. Rain (09, 10)
        else if (iconCode.contains("09") || iconCode.contains("10")) {
            iconResId = R.drawable.ic_weather_rain;
        }
        // 5. Thunderstorm (11)
        else if (iconCode.contains("11")) {
            iconResId = R.drawable.ic_weather_thunderstorm;
        }
        // 6. Snow (13)
        else if (iconCode.contains("13")) {
            iconResId = R.drawable.ic_weather_snow;
        }
        // 7. Mist / Fog (50)
        else if (iconCode.contains("50")) {
            iconResId = R.drawable.ic_weather_mist;
        }

        ivWeatherIcon.setImageResource(iconResId);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                detectLocation();
            } else {
                Toast.makeText(this, "Permission denied. Please select a city manually.", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}