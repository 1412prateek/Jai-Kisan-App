package com.rcc.jai_kisan;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import com.google.android.material.progressindicator.LinearProgressIndicator;

public class SplashScreenActivity extends BaseActivity {

    private LinearProgressIndicator progressBar;
    private ValueAnimator animator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        progressBar = findViewById(R.id.progressBar);

        // Smooth, normal continuous progress filling from 0 to 100% over 2 seconds
        animator = ValueAnimator.ofInt(0, 100);
        animator.setDuration(2000);
        animator.setInterpolator(new android.view.animation.LinearInterpolator());
        animator.addUpdateListener(animation -> {
            if (progressBar != null) {
                int progress = (int) animation.getAnimatedValue();
                progressBar.setProgressCompat(progress, false);
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (!isFinishing()) {
                    Intent intent = new Intent(SplashScreenActivity.this, HomePageActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
        progressBar.post(() -> {
            if (!isFinishing() && animator != null) {
                animator.start();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (animator != null) {
            animator.cancel();
            animator = null;
        }
    }
}
