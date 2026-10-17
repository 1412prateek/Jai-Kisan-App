package com.rcc.jai_kisan;

import android.content.Context;
import androidx.appcompat.app.AppCompatActivity;

/**
 * BaseActivity wraps the base context with LanguageManager to ensure
 * dynamic multilingual locale support across every activity.
 */
public class BaseActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LanguageManager.wrapContext(newBase));
    }
}
