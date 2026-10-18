package com.rcc.jai_kisan;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.Keyframe;
import android.animation.PropertyValuesHolder;
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

        // Multi-stage 2-second animation:
        // Phase 1: 0% to 25% normally (0ms to 550ms)
        // Phase 2: Holds at 25% with wave active (550ms to 1250ms -> 700ms pause)
        // Phase 3: Fills remaining 75% fastly to 100% (1250ms to 2000ms -> 750ms fast fill)
        Keyframe k0 = Keyframe.ofInt(0f, 0);
        Keyframe k1 = Keyframe.ofInt(0.275f, 25);
        Keyframe k2 = Keyframe.ofInt(0.625f, 25);
        Keyframe k3 = Keyframe.ofInt(1.0f, 100);

        PropertyValuesHolder pvh = PropertyValuesHolder.ofKeyframe("progress", k0, k1, k2, k3);
        animator = ValueAnimator.ofPropertyValuesHolder(pvh);
        animator.setDuration(2000);
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
        animator.start();
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
