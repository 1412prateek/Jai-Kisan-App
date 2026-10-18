package com.rcc.jai_kisan;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class AboutDeveloperActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_developer);

        // 1. Version Text
        TextView versionText = findViewById(R.id.appVersionText);
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            versionText.setText("Version " + packageInfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            versionText.setText("Version 1.0.3");
        }

        // 2. Email Support Button
        findViewById(R.id.btnEmail).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:prateek14pr@gmail.com"));
            intent.putExtra(Intent.EXTRA_SUBJECT, "Jai Kisan App Support / Inquiry");
            try {
                startActivity(Intent.createChooser(intent, "Send Email..."));
            } catch (android.content.ActivityNotFoundException ex) {
                // Handle case where no email app is installed
            }
        });

        // 3. LinkedIn Button
        findViewById(R.id.btnLinkedIn).setOnClickListener(v -> {
            String url = "https://www.linkedin.com/in/2169-prateek/";
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        });

        // 4. Rate App Button
        findViewById(R.id.btnRateApp).setOnClickListener(v -> {
            String url = "https://play.google.com/store/apps/details?id=" + getPackageName();
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        });

        // 5. Memory Button
        findViewById(R.id.btnMemory).setOnClickListener(v -> showTributeDialog());

        // 6. Circular Scroll Down Indicator
        android.widget.ScrollView aboutScrollView = findViewById(R.id.aboutScrollView);
        View btnScrollDown = findViewById(R.id.btnScrollDown);
        View cardTribute = findViewById(R.id.cardTribute);

        if (btnScrollDown != null && aboutScrollView != null) {
            btnScrollDown.setOnClickListener(v -> {
                int targetY = cardTribute != null ? cardTribute.getTop() - 20 : 600;
                aboutScrollView.smoothScrollTo(0, targetY);
                btnScrollDown.animate().alpha(0f).scaleX(0.7f).scaleY(0.7f).setDuration(250).withEndAction(() -> {
                    btnScrollDown.setVisibility(View.GONE);
                }).start();
            });

            // Automatically hide when user scrolls down, and reappear if at top
            aboutScrollView.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                if (scrollY > 150) {
                    if (btnScrollDown.getVisibility() == View.VISIBLE && btnScrollDown.getAlpha() > 0.5f) {
                        btnScrollDown.animate().alpha(0f).scaleX(0.7f).scaleY(0.7f).setDuration(200).withEndAction(() -> {
                            btnScrollDown.setVisibility(View.GONE);
                        }).start();
                    }
                } else if (scrollY < 50) {
                    if (btnScrollDown.getVisibility() != View.VISIBLE) {
                        btnScrollDown.setVisibility(View.VISIBLE);
                        btnScrollDown.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(200).start();
                    }
                }
            });
        }

        AdManager.loadAdaptiveBanner(this, findViewById(R.id.adBannerContainer));

        localizeUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        localizeUI();
    }

    private void localizeUI() {
        TextView tvAppMotto = findViewById(R.id.tvAppMotto);
        if (tvAppMotto != null) {
            if (LanguageManager.isEnglishOnly(this)) {
                tvAppMotto.setText("My Soil, My Farmers");
            } else {
                tvAppMotto.setText(LanguageManager.getRegional(this, "app_motto", "मेरी मिट्टी मेरे किसान"));
            }
        }

        TextView tvTransparencyTitle = findViewById(R.id.tvTransparencyTitle);
        if (tvTransparencyTitle != null) {
            tvTransparencyTitle.setText(LanguageManager.getDual(this, "Transparency & Support", "ad_transparency_title"));
        }

        TextView tvTransparencyDesc = findViewById(R.id.tvTransparencyDesc);
        if (tvTransparencyDesc != null) {
            String enMsg = "Jai Kisan is a free community service. We display minimal ads solely to cover AI server and API costs, keeping this tool completely free for all farmers.";
            if (LanguageManager.isEnglishOnly(this)) {
                tvTransparencyDesc.setText(enMsg);
            } else {
                String regMsg = LanguageManager.getRegional(this, "ad_transparency_msg", enMsg);
                tvTransparencyDesc.setText(LanguageManager.formatLines(this, enMsg, regMsg));
            }
        }
    }

    private void showTributeDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_tribute);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.getWindow().setDimAmount(0.6f);
        }

        final View card = dialog.findViewById(R.id.tributeCard);
        ImageButton closeBtn = dialog.findViewById(R.id.btnCloseTribute);

        // --- APPEAR ANIMATION ---
        if (card != null) {
            card.startAnimation(AnimationUtils.loadAnimation(this, R.anim.tribute_appear));
        }

        // --- CLOSE BUTTON LOGIC ---
        if (closeBtn != null) {
            closeBtn.setOnClickListener(v -> {
                Animation disappear = AnimationUtils.loadAnimation(AboutDeveloperActivity.this, R.anim.tribute_disappear);
                disappear.setAnimationListener(new Animation.AnimationListener() {
                    @Override public void onAnimationStart(Animation animation) {}
                    @Override public void onAnimationRepeat(Animation animation) {}
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        dialog.dismiss();
                    }
                });
                card.startAnimation(disappear);
            });
        }

        dialog.show();
    }
}