package com.rcc.jai_kisan;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class SettingsActivity extends BaseActivity {

    private TextView tvCurrentLanguage;
    private MaterialToolbar toolbarSettings;
    private TextView tvSettingsAppName;
    private TextView tvSettingsAppMotto;
    private TextView tvHeaderPreferences;
    private TextView tvLanguageTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        toolbarSettings = findViewById(R.id.toolbarSettings);
        tvCurrentLanguage = findViewById(R.id.tvCurrentLanguage);
        tvSettingsAppName = findViewById(R.id.tvSettingsAppName);
        tvSettingsAppMotto = findViewById(R.id.tvSettingsAppMotto);
        tvHeaderPreferences = findViewById(R.id.tvHeaderPreferences);
        tvLanguageTitle = findViewById(R.id.tvLanguageTitle);

        CardView cardLanguageSetting = findViewById(R.id.cardLanguageSetting);

        toolbarSettings.setNavigationOnClickListener(v -> finish());

        refreshUiTexts();

        cardLanguageSetting.setOnClickListener(v -> showLanguageSelectionDialog());

        AdManager.loadAdaptiveBanner(this, findViewById(R.id.adBannerContainer));
    }

    private void refreshUiTexts() {
        toolbarSettings.setTitle(LanguageManager.getDual(this, "Settings", "settings"));
        tvSettingsAppName.setText(LanguageManager.getDual(this, "Jai Kisan", "jai_kisan"));
        tvSettingsAppMotto.setText(LanguageManager.getRegional(this, "app_motto", "मेरी मिट्टी, मेरे किसान"));
        tvHeaderPreferences.setText(LanguageManager.getDual(this, "Preferences", "settings"));
        tvLanguageTitle.setText(LanguageManager.getDual(this, "Language", "language"));
        tvCurrentLanguage.setText(LanguageManager.getActiveLanguageDisplayName(this));
    }

    private void showLanguageSelectionDialog() {
        String[] displayNames = LanguageManager.getLanguageDisplayNames();
        int currentIdx = LanguageManager.getLanguageIndex(this);

        new MaterialAlertDialogBuilder(this)
                .setTitle(LanguageManager.getDual(this, "Select Language", "language"))
                .setSingleChoiceItems(displayNames, currentIdx, (dialog, which) -> {
                    LanguageManager.LangItem selected = LanguageManager.SUPPORTED_LANGUAGES[which];
                    LanguageManager.setLanguage(this, selected.code);

                    Toast.makeText(this, "Language changed to " + selected.displayName, Toast.LENGTH_SHORT).show();
                    dialog.dismiss();

                    // Recreate this activity to refresh with new locale configuration
                    recreate();
                })
                .setNegativeButton(LanguageManager.getDual(this, "Cancel", "cancel"), null)
                .show();
    }
}
