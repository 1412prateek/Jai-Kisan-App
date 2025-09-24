package com.rcc.jai_kisan;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ProgressBar;
import androidx.appcompat.app.AppCompatActivity;

public class SplashScreenActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private int progressStatus = 0;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        progressBar = findViewById(R.id.progressBar);

        // Animate progress bar from 0 to 100 over 2 seconds
        new Thread(() -> {
            while (progressStatus < 100) {
                progressStatus += 1;

                // Update the progress bar on the main thread
                handler.post(() -> progressBar.setProgress(progressStatus));

                try {
                    Thread.sleep(20); // 100 steps * 20ms = 2000ms (2 sec)
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            // After animation, go to HomePage
            handler.post(() -> {
                Intent intent = new Intent(SplashScreenActivity.this, HomePageActivity.class);
                startActivity(intent);
                finish();
            });

        }).start();
    }
}
