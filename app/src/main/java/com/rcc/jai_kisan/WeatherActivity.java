package com.rcc.jai_kisan;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;

import org.json.JSONObject;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.HttpsURLConnection;

public class WeatherActivity extends BaseActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private TextView tvTemperature, tvDescription, tvHumidity, tvWind, tvLocation, tvFeelsLike, tvPressure, tvVisibility;
    private ImageView ivWeatherIcon;
    private Button btnDetectLocation;
    private AutoCompleteTextView spinnerState, citySpinner;
    private MaterialCardView cardResult, cardFarmingAdvisory;
    private FrameLayout heroWeatherContainer;
    private ScrollView weatherScrollView;
    private View layoutWeatherLoading;
    private ImageButton btnRefreshWeather;

    // Advisory views
    private TextView tvAdvisoryTitle, tvSprayingAdvice, tvIrrigationAdvice;
    private ImageView ivSprayingStatus, ivIrrigationStatus;

    private MaterialToolbar toolbar;
    private com.google.android.material.textfield.TextInputLayout layoutSpinnerState, layoutCitySpinner;
    private TextView tvOrDivider;
    private TextView tvHumidityLabel, tvWindLabel, tvPressureLabel, tvVisibilityLabel;

    private String lastQueriedCity = "Delhi";
    private ObjectAnimator iconFloatAnimator;

    private final String API_KEY = "1cce38c2c68cf1fb412a1c2cb29c40e7";
    private FusedLocationProviderClient fusedLocationClient;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        executorService = Executors.newSingleThreadExecutor();

        // --- Setup Toolbar ---
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // --- Find Views ---
        weatherScrollView = findViewById(R.id.weatherScrollView);
        tvTemperature = findViewById(R.id.tvTemperature);
        tvFeelsLike = findViewById(R.id.tvFeelsLike);
        tvDescription = findViewById(R.id.tvDescription);
        tvHumidity = findViewById(R.id.tvHumidity);
        tvWind = findViewById(R.id.tvWind);
        tvPressure = findViewById(R.id.tvPressure);
        tvVisibility = findViewById(R.id.tvVisibility);
        tvLocation = findViewById(R.id.tvLocation);
        ivWeatherIcon = findViewById(R.id.ivWeatherIcon);
        btnDetectLocation = findViewById(R.id.btnDetectLocation);
        spinnerState = findViewById(R.id.spinnerState);
        citySpinner = findViewById(R.id.citySpinner);
        cardResult = findViewById(R.id.cardResult);
        cardFarmingAdvisory = findViewById(R.id.cardFarmingAdvisory);
        heroWeatherContainer = findViewById(R.id.heroWeatherContainer);
        layoutWeatherLoading = findViewById(R.id.layoutWeatherLoading);
        btnRefreshWeather = findViewById(R.id.btnRefreshWeather);

        layoutSpinnerState = findViewById(R.id.layout_spinner_state);
        layoutCitySpinner = findViewById(R.id.layout_city_spinner);
        tvOrDivider = findViewById(R.id.tvOrDivider);

        tvHumidityLabel = findViewById(R.id.tvHumidityLabel);
        tvWindLabel = findViewById(R.id.tvWindLabel);
        tvPressureLabel = findViewById(R.id.tvPressureLabel);
        tvVisibilityLabel = findViewById(R.id.tvVisibilityLabel);

        tvAdvisoryTitle = findViewById(R.id.tvAdvisoryTitle);
        tvSprayingAdvice = findViewById(R.id.tvSprayingAdvice);
        tvIrrigationAdvice = findViewById(R.id.tvIrrigationAdvice);
        ivSprayingStatus = findViewById(R.id.ivSprayingStatus);
        ivIrrigationStatus = findViewById(R.id.ivIrrigationStatus);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        setupStateCitySpinners();

        btnDetectLocation.setOnClickListener(v -> detectLocation());

        if (btnRefreshWeather != null) {
            btnRefreshWeather.setOnClickListener(v -> {
                btnRefreshWeather.animate().rotationBy(360f).setDuration(500).start();
                fetchWeather(lastQueriedCity);
            });
        }

        setupIconBreathingAnimation();

        localizeUI();

        AdManager.loadAdaptiveBanner(this, findViewById(R.id.adBannerContainer));

        // Fetch weather for a default location on startup
        fetchWeather("Delhi");
    }

    private void setupIconBreathingAnimation() {
        if (ivWeatherIcon != null) {
            PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1.0f, 1.08f, 1.0f);
            PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1.0f, 1.08f, 1.0f);
            PropertyValuesHolder translateY = PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, 0f, -8f, 0f);

            iconFloatAnimator = ObjectAnimator.ofPropertyValuesHolder(ivWeatherIcon, scaleX, scaleY, translateY);
            iconFloatAnimator.setDuration(2400);
            iconFloatAnimator.setRepeatCount(ObjectAnimator.INFINITE);
            iconFloatAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            iconFloatAnimator.start();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        localizeUI();
    }

    private void localizeUI() {
        if (toolbar != null) {
            toolbar.setTitle(LanguageManager.getDual(this, "Weather Info", "weather_title"));
        }
        if (btnDetectLocation != null) {
            btnDetectLocation.setText(LanguageManager.getDual(this, "Detect My Location", "detect_location"));
        }
        if (layoutSpinnerState != null) {
            layoutSpinnerState.setHint(LanguageManager.getDual(this, "State", "state"));
        }
        if (layoutCitySpinner != null) {
            layoutCitySpinner.setHint(LanguageManager.getDual(this, "City", "city"));
        }
        if (tvOrDivider != null) {
            if (LanguageManager.isEnglishOnly(this)) {
                tvOrDivider.setText("OR SELECT MANUALLY");
            } else {
                tvOrDivider.setText("OR SELECT MANUALLY / या " + LanguageManager.getRegional(this, "or", "चुनें"));
            }
        }
        if (tvHumidityLabel != null) {
            tvHumidityLabel.setText(LanguageManager.getDual(this, "Humidity", "humidity"));
        }
        if (tvWindLabel != null) {
            tvWindLabel.setText(LanguageManager.getDual(this, "Wind", "wind"));
        }
        if (tvPressureLabel != null) {
            tvPressureLabel.setText(LanguageManager.getDual(this, "Pressure", "weather"));
        }
        if (tvVisibilityLabel != null) {
            tvVisibilityLabel.setText(LanguageManager.getDual(this, "Visibility", "weather"));
        }
        if (tvAdvisoryTitle != null) {
            tvAdvisoryTitle.setText(LanguageManager.getDual(this, "Farming Impact & Advice", "irrigation_title"));
        }
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
            String selectedCityFull = (String) parent.getItemAtPosition(position);
            String selectedCityEnglish = selectedCityFull.split("/")[0].trim();
            fetchWeather(selectedCityEnglish);
        });
    }

    private void detectLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        if (layoutWeatherLoading != null) layoutWeatherLoading.setVisibility(View.VISIBLE);

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                getCityFromLocation(location);
            } else {
                if (layoutWeatherLoading != null) layoutWeatherLoading.setVisibility(View.GONE);
                Toast.makeText(this, "Could not get current location. Ensure GPS is on.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getCityFromLocation(Location loc) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1);
            if (addresses != null && !addresses.isEmpty()) {
                String cityName = addresses.get(0).getLocality();
                if (cityName == null || cityName.isEmpty()) {
                    cityName = addresses.get(0).getSubAdminArea();
                }
                if (cityName != null && !cityName.isEmpty()) {
                    fetchWeather(cityName);
                } else {
                    if (layoutWeatherLoading != null) layoutWeatherLoading.setVisibility(View.GONE);
                    Toast.makeText(this, "City name not found from location.", Toast.LENGTH_SHORT).show();
                }
            } else {
                if (layoutWeatherLoading != null) layoutWeatherLoading.setVisibility(View.GONE);
                Toast.makeText(this, "No address found for location.", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            if (layoutWeatherLoading != null) layoutWeatherLoading.setVisibility(View.GONE);
            e.printStackTrace();
            Toast.makeText(this, "Error getting city name.", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchWeather(String city) {
        String cleanCity = city.trim();
        lastQueriedCity = cleanCity;

        runOnUiThread(() -> {
            if (layoutWeatherLoading != null) layoutWeatherLoading.setVisibility(View.VISIBLE);
        });

        executorService.execute(() -> {
            try {
                String urlString = "https://api.openweathermap.org/data/2.5/weather?q=" + cleanCity + "&appid=" + API_KEY + "&units=metric";
                URL url = new URL(urlString);
                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                Scanner scanner = new Scanner(connection.getInputStream());
                StringBuilder result = new StringBuilder();
                while (scanner.hasNext()) result.append(scanner.nextLine());

                JSONObject json = new JSONObject(result.toString());

                double tempDouble = json.getJSONObject("main").getDouble("temp");
                double feelsLikeDouble = json.getJSONObject("main").optDouble("feels_like", tempDouble);
                int humidityInt = json.getJSONObject("main").getInt("humidity");
                int pressureInt = json.getJSONObject("main").optInt("pressure", 1013);
                int visibilityInt = json.optInt("visibility", 10000);

                double windSpeedDouble = json.getJSONObject("wind").getDouble("speed");
                // Convert m/s to km/h for farmers
                double windSpeedKmH = Math.round(windSpeedDouble * 3.6 * 10.0) / 10.0;

                String descEng = json.getJSONArray("weather").getJSONObject(0).getString("description");
                String iconCode = json.getJSONArray("weather").getJSONObject(0).getString("icon");

                String cityName = json.getString("name");
                String country = json.getJSONObject("sys").getString("country");
                String locationText = cityName + ", " + country;

                String fullDescription = LanguageManager.getLocalizedWeatherDesc(WeatherActivity.this, descEng);

                runOnUiThread(() -> {
                    if (layoutWeatherLoading != null) layoutWeatherLoading.setVisibility(View.GONE);

                    cardResult.setVisibility(View.VISIBLE);
                    cardFarmingAdvisory.setVisibility(View.VISIBLE);

                    tvTemperature.setText(Math.round(tempDouble) + "°C");
                    tvFeelsLike.setText("Feels like " + Math.round(feelsLikeDouble) + "°C");
                    tvDescription.setText(fullDescription);
                    tvHumidity.setText(humidityInt + "%");
                    tvWind.setText(windSpeedKmH + " km/h");
                    tvPressure.setText(pressureInt + " hPa");
                    tvVisibility.setText(Math.round(visibilityInt / 1000.0) + " km");
                    tvLocation.setText(locationText);

                    // Set dynamic gradient and icon
                    updateAtmosphericTheme(iconCode);
                    setWeatherIcon(iconCode);

                    // Compute and show farming advisory
                    updateFarmingAdvisory(tempDouble, humidityInt, windSpeedKmH, iconCode);

                    // Smooth scroll down to result
                    if (weatherScrollView != null && cardResult != null) {
                        weatherScrollView.post(() -> {
                            weatherScrollView.smoothScrollTo(0, cardResult.getTop() - 30);
                        });
                    }
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    if (layoutWeatherLoading != null) layoutWeatherLoading.setVisibility(View.GONE);
                    Toast.makeText(WeatherActivity.this, "Could not fetch weather for " + cleanCity, Toast.LENGTH_SHORT).show();
                });
                e.printStackTrace();
            }
        });
    }

    private void updateAtmosphericTheme(String iconCode) {
        if (heroWeatherContainer == null) return;

        if (iconCode.contains("01")) {
            if (iconCode.contains("n")) {
                heroWeatherContainer.setBackgroundResource(R.drawable.bg_weather_night);
            } else {
                heroWeatherContainer.setBackgroundResource(R.drawable.bg_weather_sunny);
            }
        } else if (iconCode.contains("09") || iconCode.contains("10") || iconCode.contains("11")) {
            heroWeatherContainer.setBackgroundResource(R.drawable.bg_weather_rainy);
        } else if (iconCode.contains("n")) {
            heroWeatherContainer.setBackgroundResource(R.drawable.bg_weather_night);
        } else {
            heroWeatherContainer.setBackgroundResource(R.drawable.bg_weather_cloudy);
        }
    }

    private void updateFarmingAdvisory(double temp, int humidity, double windKmH, String iconCode) {
        boolean isRainOrThunder = iconCode.contains("09") || iconCode.contains("10") || iconCode.contains("11");
        boolean isEnglish = LanguageManager.isEnglishOnly(this);

        // 1. Spraying Advice
        if (isRainOrThunder) {
            ivSprayingStatus.setImageResource(R.drawable.ic_close);
            ivSprayingStatus.setImageTintList(android.content.res.ColorStateList.valueOf(0xFFD32F2F)); // Red
            String en = "Postpone chemical spraying: rain will wash away pesticides.";
            String hi = "कीटनाशक छिड़काव टालें: बारिश से दवा बह जाएगी।";
            tvSprayingAdvice.setText(isEnglish ? en : LanguageManager.formatLines(this, en, LanguageManager.getRegional(this, "spray_rain", hi)));
        } else if (windKmH > 18.0) {
            ivSprayingStatus.setImageResource(R.drawable.ic_close);
            ivSprayingStatus.setImageTintList(android.content.res.ColorStateList.valueOf(0xFFF57C00)); // Orange
            String en = "Avoid spraying now: high wind (" + windKmH + " km/h) causes drift.";
            String hi = "तेज़ हवा (" + windKmH + " km/h) के कारण छिड़काव न करें, दवा उड़ जाएगी।";
            tvSprayingAdvice.setText(isEnglish ? en : LanguageManager.formatLines(this, en, LanguageManager.getRegional(this, "spray_wind", hi)));
        } else if (temp > 38.0) {
            ivSprayingStatus.setImageResource(R.drawable.ic_check_circle);
            ivSprayingStatus.setImageTintList(android.content.res.ColorStateList.valueOf(0xFFF57C00)); // Orange
            String en = "Spray early morning or evening: high heat degrades foliar sprays.";
            String hi = "सुबह जल्दी या शाम को ही छिड़काव करें: तेज़ धूप में दवा असर खो देती है।";
            tvSprayingAdvice.setText(isEnglish ? en : LanguageManager.formatLines(this, en, LanguageManager.getRegional(this, "spray_heat", hi)));
        } else {
            ivSprayingStatus.setImageResource(R.drawable.ic_check_circle);
            ivSprayingStatus.setImageTintList(android.content.res.ColorStateList.valueOf(0xFF2E7D32)); // Green
            String en = "Ideal conditions for pesticide and fertilizer spraying today.";
            String hi = "कीटनाशक एवं उर्वरक छिड़काव के लिए आज का मौसम अनुकूल है।";
            tvSprayingAdvice.setText(isEnglish ? en : LanguageManager.formatLines(this, en, LanguageManager.getRegional(this, "spray_good", hi)));
        }

        // 2. Irrigation Advice
        if (isRainOrThunder || humidity > 85) {
            ivIrrigationStatus.setImageResource(R.drawable.ic_close);
            ivIrrigationStatus.setImageTintList(android.content.res.ColorStateList.valueOf(0xFF1976D2));
            String en = "Rain or high humidity detected: postpone tubewell irrigation.";
            String hi = "बारिश या अत्यधिक नमी के आसार: सिंचाई टालें और बिजली/डीजल बचाएं।";
            tvIrrigationAdvice.setText(isEnglish ? en : LanguageManager.formatLines(this, en, LanguageManager.getRegional(this, "irrigation_rain", hi)));
        } else if (temp > 36.0 && humidity < 40) {
            ivIrrigationStatus.setImageResource(R.drawable.ic_water_drop);
            ivIrrigationStatus.setImageTintList(android.content.res.ColorStateList.valueOf(0xFF1976D2));
            String en = "High evaporation rate: irrigate in the evening to conserve water.";
            String hi = "अधिक वाष्पीकरण: पानी बचाने और फसल सुरक्षा हेतु शाम को सिंचाई करें।";
            tvIrrigationAdvice.setText(isEnglish ? en : LanguageManager.formatLines(this, en, LanguageManager.getRegional(this, "irrigation_heat", hi)));
        } else {
            ivIrrigationStatus.setImageResource(R.drawable.ic_water_drop);
            ivIrrigationStatus.setImageTintList(android.content.res.ColorStateList.valueOf(0xFF2E7D32));
            String en = "Standard moisture balance: maintain regular crop irrigation schedule.";
            String hi = "सामान्य नमी संतुलन: नियमित सिंचाई चक्र जारी रख सकते हैं।";
            tvIrrigationAdvice.setText(isEnglish ? en : LanguageManager.formatLines(this, en, LanguageManager.getRegional(this, "irrigation_normal", hi)));
        }
    }

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
        // 2. Few Clouds (02)
        else if (iconCode.contains("02")) {
            if (iconCode.contains("n")) {
                iconResId = R.drawable.ic_weather_partly_cloudy_night;
            } else {
                iconResId = R.drawable.ic_weather_partly_cloudy_day;
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
    protected void onDestroy() {
        super.onDestroy();
        if (iconFloatAnimator != null) {
            iconFloatAnimator.cancel();
            iconFloatAnimator = null;
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