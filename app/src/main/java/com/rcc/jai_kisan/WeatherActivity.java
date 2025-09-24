package com.rcc.jai_kisan;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

import javax.net.ssl.HttpsURLConnection;

public class WeatherActivity extends AppCompatActivity {

    TextView temperature, description, humidity, wind, location;
    ImageView weatherIcon;
    Button btnDetectLocation;
    Spinner spinnerState, spinnerCity;

    String API_KEY = "1cce38c2c68cf1fb412a1c2cb29c40e7";
    private FusedLocationProviderClient fusedLocationClient;

    Map<String, Integer> stateToCitiesMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        temperature = findViewById(R.id.tvTemperature);
        description = findViewById(R.id.tvDescription);
        humidity = findViewById(R.id.tvHumidity);
        wind = findViewById(R.id.tvWind);
        location = findViewById(R.id.tvLocation);
        weatherIcon = findViewById(R.id.ivWeatherIcon);

        btnDetectLocation = findViewById(R.id.btnDetectLocation);
        spinnerState = findViewById(R.id.spinnerState);
        spinnerCity = findViewById(R.id.citySpinner);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        fetchWeather("Delhi"); // Default

        setupStateCitySpinners();

        btnDetectLocation.setOnClickListener(v -> detectLocation());
    }

    private void setupStateCitySpinners() {
        // 🔹 Populate States
        ArrayAdapter<CharSequence> stateAdapter = ArrayAdapter.createFromResource(
                this, R.array.states_array, android.R.layout.simple_spinner_item);
        stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerState.setAdapter(stateAdapter);

        // 🔹 Handle state selection dynamically
        spinnerState.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                String selectedState = parent.getItemAtPosition(position).toString();

                // Convert state name to array name
                String arrayName = selectedState.toLowerCase()
                        .replace(" & ", "_and_")
                        .replace(" and ", "_and_")
                        .replace("-", "_")
                        .replace(" ", "_")
                        + "_cities";

                int cityArrayId = getResources().getIdentifier(arrayName, "array", getPackageName());

                if (cityArrayId != 0) {
                    ArrayAdapter<CharSequence> cityAdapter = ArrayAdapter.createFromResource(
                            WeatherActivity.this, cityArrayId, android.R.layout.simple_spinner_item);
                    cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerCity.setAdapter(cityAdapter);
                } else {
                    spinnerCity.setAdapter(null);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerCity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                String city = parent.getItemAtPosition(position).toString();
                fetchWeather(city);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void detectLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                getCityFromLocation(location);
            } else {
                Toast.makeText(this, "Location not available", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(this, "Unable to detect city", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void fetchWeather(String city) {
        new Thread(() -> {
            try {
                String urlString = "https://api.openweathermap.org/data/2.5/weather?q=" + city +
                        "&appid=" + API_KEY + "&units=metric";
                URL url = new URL(urlString);
                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

                Scanner scanner = new Scanner(connection.getInputStream());
                StringBuilder result = new StringBuilder();
                while (scanner.hasNext()) result.append(scanner.nextLine());

                JSONObject json = new JSONObject(result.toString());

                String temp = json.getJSONObject("main").getString("temp") + "°C";
                String descEng = json.getJSONArray("weather").getJSONObject(0).getString("description");
                String icon = json.getJSONArray("weather").getJSONObject(0).getString("icon");
                String humidityValue = json.getJSONObject("main").getString("humidity");
                String windSpeedValue = json.getJSONObject("wind").getString("speed");
                String humid = "Humidity: " + humidityValue + "% / आर्द्रता: " + humidityValue + "%";
                String windSpeed = "Wind: " + windSpeedValue + " m/s / पवन गति: " + windSpeedValue + " m/s";
                String cityName = json.getString("name");

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

                String descHindi = weatherTranslations.getOrDefault(descEng.toLowerCase(), "");

                runOnUiThread(() -> {
                    temperature.setText(temp);
                    description.setText(descEng + (descHindi.isEmpty() ? "" : " / " + descHindi));
                    humidity.setText(humid);
                    wind.setText(windSpeed);
                    location.setText(cityName);
                    Picasso.get().load("https://openweathermap.org/img/wn/" + icon + "@2x.png").into(weatherIcon);
                });

            } catch (Exception e) {
                runOnUiThread(() -> temperature.setText("Error fetching data"));
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            detectLocation();
        } else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
        }
    }
}
