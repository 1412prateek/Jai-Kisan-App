package com.rcc.jai_kisan;

import android.content.Context;
import androidx.appcompat.app.AppCompatActivity;

/**
 * BaseActivity wraps the base context with LanguageManager to ensure
 * dynamic multilingual locale support across every activity.
 */
public class BaseActivity extends AppCompatActivity {

    private String currentAttachedLanguage;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LanguageManager.wrapContext(newBase));
    }

    @Override
    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentAttachedLanguage = LanguageManager.getLanguage(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        String activeLang = LanguageManager.getLanguage(this);
        if (currentAttachedLanguage != null && !currentAttachedLanguage.equalsIgnoreCase(activeLang)) {
            currentAttachedLanguage = activeLang;
            recreate();
        }
    }
}
