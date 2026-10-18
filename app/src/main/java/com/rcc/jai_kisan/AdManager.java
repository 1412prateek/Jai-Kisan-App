package com.rcc.jai_kisan;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

/**
 * Production-ready centralized Ad Manager for Google AdMob.
 * Handles:
 * 1. Safe Ad IDs: Real production IDs for release builds, official Google Test IDs for debug builds.
 * 2. Adaptive Banners: Fits screen width perfectly on any device.
 * 3. Interstitial Ads: Triggered once every 3 PDF generations across the entire app.
 * 4. Rewarded Ads: Used in Photo Expert to unlock +3 analyses when free quota is used.
 */
public class AdManager {

    private static final String TAG = "AdManager";
    private static final String PREFS_NAME = "jai_kisan_ad_prefs";
    private static final String KEY_PDF_COUNT = "global_pdf_count";
    private static final String KEY_PHOTO_CREDITS = "photo_expert_credits";
    public static final int INITIAL_FREE_PHOTO_CREDITS = 3;
    public static final int REWARD_PHOTO_CREDITS = 3;
    public static final int PDF_AD_TRIGGER_INTERVAL = 3;

    // Google Official Test Ad Unit IDs (Safe for debug testing)
    private static final String TEST_BANNER_ID = "ca-app-pub-3940256099942544/6300978111";
    private static final String TEST_INTERSTITIAL_ID = "ca-app-pub-3940256099942544/1033173712";
    private static final String TEST_REWARDED_ID = "ca-app-pub-3940256099942544/5224354917";

    // User's Real Production Ad Unit IDs
    private static final String PROD_BANNER_ID = "ca-app-pub-5394244233679690/7794342415";
    private static final String PROD_INTERSTITIAL_ID = "ca-app-pub-5394244233679690/8352000751";
    private static final String PROD_REWARDED_ID = "ca-app-pub-5394244233679690/6895448853";

    private static AdManager instance;
    private InterstitialAd mInterstitialAd;
    private RewardedAd mRewardedAd;
    private boolean isInterstitialLoading = false;
    private boolean isRewardedLoading = false;

    private AdManager() {}

    public static synchronized AdManager getInstance() {
        if (instance == null) {
            instance = new AdManager();
        }
        return instance;
    }

    public static void initialize(Context context) {
        MobileAds.initialize(context, initializationStatus -> {
            Log.d(TAG, "Google Mobile Ads SDK initialized successfully.");
            getInstance().loadInterstitialAd(context.getApplicationContext());
            getInstance().loadRewardedAd(context.getApplicationContext());
        });
    }

    public static String getBannerAdUnitId() {
        return BuildConfig.DEBUG ? TEST_BANNER_ID : PROD_BANNER_ID;
    }

    public static String getInterstitialAdUnitId() {
        return BuildConfig.DEBUG ? TEST_INTERSTITIAL_ID : PROD_INTERSTITIAL_ID;
    }

    public static String getRewardedAdUnitId() {
        return BuildConfig.DEBUG ? TEST_REWARDED_ID : PROD_REWARDED_ID;
    }

    // =========================================================================
    // 1. ADAPTIVE BANNER AD
    // =========================================================================

    public static void loadAdaptiveBanner(Activity activity, ViewGroup adContainer) {
        if (activity == null || adContainer == null) return;
        try {
            adContainer.removeAllViews();
            AdView adView = new AdView(activity);
            adView.setAdUnitId(getBannerAdUnitId());
            adContainer.addView(adView);

            AdSize adSize = getAdaptiveAdSize(activity);
            adView.setAdSize(adSize);

            adView.setAdListener(new com.google.android.gms.ads.AdListener() {
                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    super.onAdFailedToLoad(loadAdError);
                    checkIsAdBlockedByDnsAsync(activity, isBlocked -> {
                        if (isBlocked && activity != null && !activity.isFinishing()) {
                            showDnsNoticeInBanner(activity, adContainer);
                        }
                    });
                }
            });

            AdRequest adRequest = new AdRequest.Builder().build();
            adView.loadAd(adRequest);
            Log.d(TAG, "Loading adaptive banner with AdUnit: " + getBannerAdUnitId());
        } catch (Exception e) {
            Log.e(TAG, "Error loading adaptive banner: " + e.getMessage(), e);
        }
    }

    private static void showDnsNoticeInBanner(Activity activity, ViewGroup adContainer) {
        try {
            adContainer.removeAllViews();
            android.widget.LinearLayout strip = new android.widget.LinearLayout(activity);
            strip.setTag("adblock_dns_strip");
            strip.setOrientation(android.widget.LinearLayout.HORIZONTAL);
            strip.setGravity(android.view.Gravity.CENTER_VERTICAL);
            strip.setBackgroundColor(android.graphics.Color.parseColor("#FFF3E0")); // Warm light orange/amber
            strip.setPadding(32, 16, 32, 16);

            android.widget.TextView tv = new android.widget.TextView(activity);
            String enDefault = "Ad-blocker active. Please disable Private DNS to keep Jai Kisan free.";
            String noticeText;
            if (LanguageManager.isEnglishOnly(activity)) {
                noticeText = enDefault;
            } else {
                noticeText = LanguageManager.getRegional(activity, "adblock_banner_strip", enDefault);
            }
            tv.setText("🛡️ " + noticeText);
            tv.setTextColor(android.graphics.Color.parseColor("#E65100"));
            tv.setTextSize(11f);
            tv.setLayoutParams(new android.widget.LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

            android.widget.TextView btn = new android.widget.TextView(activity);
            btn.setText("⚙️");
            btn.setTextSize(16f);
            btn.setPadding(16, 8, 8, 8);

            strip.addView(tv);
            strip.addView(btn);

            strip.setOnClickListener(v -> {
                try {
                    activity.startActivity(new android.content.Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
                } catch (Exception e) {
                    try {
                        activity.startActivity(new android.content.Intent(android.provider.Settings.ACTION_SETTINGS));
                    } catch (Exception ignored) {}
                }
            });

            adContainer.addView(strip);
        } catch (Exception ignored) {}
    }

    public static void loadBanner(Activity activity, ViewGroup adContainer) {
        loadAdaptiveBanner(activity, adContainer);
    }

    public static void onPdfExportClick(Activity activity, Runnable onComplete) {
        getInstance().onPdfGenerated(activity, onComplete);
    }

    private static AdSize getAdaptiveAdSize(Activity activity) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float density = outMetrics.density;
        float adWidthPixels = outMetrics.widthPixels;
        int adWidth = (int) (adWidthPixels / density);
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, adWidth);
    }

    // =========================================================================
    // 2. INTERSTITIAL AD (EVERY 3 PDF GENERATIONS)
    // =========================================================================

    public void loadInterstitialAd(Context context) {
        if (mInterstitialAd != null || isInterstitialLoading) return;
        isInterstitialLoading = true;
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(context, getInterstitialAdUnitId(), adRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                mInterstitialAd = interstitialAd;
                isInterstitialLoading = false;
                Log.d(TAG, "Interstitial Ad loaded.");
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                mInterstitialAd = null;
                isInterstitialLoading = false;
                Log.e(TAG, "Interstitial Ad failed to load: " + loadAdError.getMessage());
            }
        });
    }

    /**
     * Checks if ads can be shown before exporting PDF.
     * If blocked by Private DNS, intercepts with friendly guidance dialog (with Settings and Close buttons, NO Continue)
     * so that Private DNS must be disabled to support free community services.
     * If an ad is ready or can be loaded, shows the ad before proceeding with onComplete.
     */
    public void onPdfGenerated(Activity activity, Runnable onComplete) {
        if (activity == null) {
            if (onComplete != null) onComplete.run();
            return;
        }

        // First, verify whether Google Ad servers are being blocked by Private DNS / AdGuard
        checkIsAdBlockedByDnsAsync(activity, isBlockedByDns -> {
            if (isBlockedByDns && activity != null && !activity.isFinishing()) {
                showPdfAdBlockerDialog(activity);
                return;
            }

            // Private DNS is not blocking. Check interstitial ad.
            if (mInterstitialAd != null) {
                mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdDismissedFullScreenContent() {
                        mInterstitialAd = null;
                        loadInterstitialAd(activity.getApplicationContext());
                        if (onComplete != null) onComplete.run();
                    }

                    @Override
                    public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                        mInterstitialAd = null;
                        loadInterstitialAd(activity.getApplicationContext());
                        if (onComplete != null) onComplete.run();
                    }
                });
                Log.i(TAG, "Showing Interstitial Ad for PDF export.");
                mInterstitialAd.show(activity);
            } else {
                // If ad is currently loading or not ready yet, load it now and allow the user through
                loadInterstitialAd(activity.getApplicationContext());
                if (onComplete != null) {
                    onComplete.run();
                }
            }
        });
    }

    private static void showPdfAdBlockerDialog(Activity activity) {
        String title = LanguageManager.getDual(activity, "Ad-Blocker / Private DNS Detected", "adblock_dns_title");
        String enDesc = "Private DNS (such as dns.adguard.com) is active on your device, blocking advertisements.\n\nJai Kisan is a free community service for farmers supported by brief ads. To export your PDF report, please temporarily disable Private DNS in your phone settings.";
        String message;
        if (LanguageManager.isEnglishOnly(activity)) {
            message = enDesc;
        } else {
            String regDesc = LanguageManager.getRegional(activity, "adblock_pdf_desc", enDesc);
            message = LanguageManager.formatLines(activity, enDesc, regDesc);
        }
        String settingsBtn = LanguageManager.getDual(activity, "Open Settings", "open_settings");
        String closeBtn = LanguageManager.getDual(activity, "Close", "cancel");

        new com.google.android.material.dialog.MaterialAlertDialogBuilder(activity)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton(settingsBtn, (dialog, which) -> {
                    try {
                        activity.startActivity(new android.content.Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
                    } catch (Exception e) {
                        try {
                            activity.startActivity(new android.content.Intent(android.provider.Settings.ACTION_SETTINGS));
                        } catch (Exception ignored) {}
                    }
                })
                .setNegativeButton(closeBtn, (dialog, which) -> dialog.dismiss())
                .show();
    }

    // =========================================================================
    // 3. REWARDED AD (PHOTO EXPERT CREDITS)
    // =========================================================================

    public void loadRewardedAd(Context context) {
        if (mRewardedAd != null || isRewardedLoading) return;
        isRewardedLoading = true;
        AdRequest adRequest = new AdRequest.Builder().build();
        RewardedAd.load(context, getRewardedAdUnitId(), adRequest, new RewardedAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                mRewardedAd = rewardedAd;
                isRewardedLoading = false;
                Log.d(TAG, "Rewarded Ad loaded.");
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                mRewardedAd = null;
                isRewardedLoading = false;
                Log.e(TAG, "Rewarded Ad failed to load: " + loadAdError.getMessage());
            }
        });
    }

    public static int getPhotoCredits(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_PHOTO_CREDITS, INITIAL_FREE_PHOTO_CREDITS);
    }

    public static void usePhotoCredit(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int current = prefs.getInt(KEY_PHOTO_CREDITS, INITIAL_FREE_PHOTO_CREDITS);
        if (current > 0) {
            prefs.edit().putInt(KEY_PHOTO_CREDITS, current - 1).apply();
        }
    }

    public static void addPhotoCredits(Context context, int count) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int current = prefs.getInt(KEY_PHOTO_CREDITS, INITIAL_FREE_PHOTO_CREDITS);
        prefs.edit().putInt(KEY_PHOTO_CREDITS, current + count).apply();
    }

    public interface RewardedAdCallback {
        void onUserEarnedReward(int addedCredits);
        void onAdFailed();
        default void onAdBlockedByDns() {
            onAdFailed();
        }
    }

    /**
     * Checks in a background thread whether Google ad servers are being sinkholed/blocked
     * by private DNS (like dns.adguard.com) while general internet works.
     */
    public static void checkIsAdBlockedByDnsAsync(Context context, DnsCheckCallback callback) {
        new Thread(() -> {
            boolean isBlocked = false;
            try {
                // Try resolving Google's ad syndication server
                java.net.InetAddress adAddress = java.net.InetAddress.getByName("pagead2.googlesyndication.com");
                String ip = adAddress.getHostAddress();
                // AdGuard/PiHole returns 0.0.0.0 or loopback
                if (ip == null || "0.0.0.0".equals(ip) || "127.0.0.1".equals(ip)) {
                    isBlocked = true;
                }
            } catch (Exception e) {
                // Resolution failed (NXDOMAIN / blocked by DNS)
                // Verify if general internet works (e.g. google.com)
                try {
                    java.net.InetAddress general = java.net.InetAddress.getByName("google.com");
                    if (general != null && !general.getHostAddress().isEmpty()) {
                        isBlocked = true; // Google works, but ad server failed -> definitely blocked by DNS!
                    }
                } catch (Exception ex) {
                    // Entire internet is down
                    isBlocked = false;
                }
            }

            final boolean blocked = isBlocked;
            new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                if (callback != null) {
                    callback.onResult(blocked);
                }
            });
        }).start();
    }

    public interface DnsCheckCallback {
        void onResult(boolean isBlockedByDns);
    }

    public void showRewardedAd(Activity activity, RewardedAdCallback callback) {
        if (activity == null) {
            if (callback != null) callback.onAdFailed();
            return;
        }

        if (mRewardedAd != null) {
            final boolean[] earned = {false};
            mRewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdDismissedFullScreenContent() {
                    mRewardedAd = null;
                    loadRewardedAd(activity.getApplicationContext());
                    if (earned[0]) {
                        addPhotoCredits(activity, REWARD_PHOTO_CREDITS);
                        if (callback != null) callback.onUserEarnedReward(REWARD_PHOTO_CREDITS);
                    } else {
                        if (callback != null) callback.onAdFailed();
                    }
                }

                @Override
                public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                    mRewardedAd = null;
                    loadRewardedAd(activity.getApplicationContext());
                    handleFailureWithDnsCheck(activity, callback);
                }
            });

            mRewardedAd.show(activity, rewardItem -> {
                earned[0] = true;
                Log.i(TAG, "User earned reward: " + rewardItem.getAmount() + " " + rewardItem.getType());
            });
        } else {
            Log.w(TAG, "Rewarded Ad not ready, checking DNS and reloading...");
            loadRewardedAd(activity.getApplicationContext());
            handleFailureWithDnsCheck(activity, callback);
        }
    }

    private void handleFailureWithDnsCheck(Activity activity, RewardedAdCallback callback) {
        checkIsAdBlockedByDnsAsync(activity, isBlockedByDns -> {
            if (isBlockedByDns) {
                if (callback != null) callback.onAdBlockedByDns();
            } else {
                if (callback != null) callback.onAdFailed();
            }
        });
    }
}
