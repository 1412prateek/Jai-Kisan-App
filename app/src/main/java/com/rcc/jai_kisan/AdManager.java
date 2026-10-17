package com.rcc.jai_kisan;

import android.app.Activity;
import android.widget.FrameLayout;

public class AdManager {

    public static void loadBanner(Activity activity, FrameLayout container) {
        if (container == null) return;
        // Clean banner loader placeholder / AdMob integration
    }

    public static void onPdfExportClick(Activity activity, Runnable onComplete) {
        if (onComplete != null) {
            onComplete.run();
        }
    }
}
