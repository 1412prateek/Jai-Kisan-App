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

public class AboutDeveloperActivity extends AppCompatActivity {

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