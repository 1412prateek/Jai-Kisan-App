package com.rcc.jai_kisan;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.io.IOException;

public class DetectDiseaseActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;

    private ImageView imagePreview;
    private TextView placeholderText;
    private MaterialCardView cardResult;
    private TextView tvDiseaseName, tvDiseaseInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detect_disease);

        imagePreview = findViewById(R.id.imagePreview);
        placeholderText = findViewById(R.id.placeholderText);
        cardResult = findViewById(R.id.cardResult);
        tvDiseaseName = findViewById(R.id.tvDiseaseName);
        tvDiseaseInfo = findViewById(R.id.tvDiseaseInfo);

        MaterialButton btnCaptureImage = findViewById(R.id.btnCaptureImage);
        MaterialButton btnUploadImage = findViewById(R.id.btnUploadImage);
        MaterialButton btnDetectDisease = findViewById(R.id.btnDetectDisease);

        // Capture from camera
        btnCaptureImage.setOnClickListener(v -> {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        });

        // Pick from gallery
        btnUploadImage.setOnClickListener(v -> {
            Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(pickPhoto, REQUEST_IMAGE_PICK);
        });

        // Detect Disease (Demo)
        btnDetectDisease.setOnClickListener(v -> {
            // For now, show demo result
            cardResult.setVisibility(View.VISIBLE);
            tvDiseaseName.setText("Disease: Leaf Spot\nबीमारी: पत्ती पर धब्बा");
            tvDiseaseInfo.setText("This is a demo result. The trained model will provide actual disease detection.\n"
                    + "यह एक डेमो परिणाम है। प्रशिक्षित मॉडल वास्तविक बीमारी का पता लगाएगा।");
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            Bitmap bitmap = null;

            try {
                if (requestCode == REQUEST_IMAGE_CAPTURE && data.getExtras() != null) {
                    bitmap = (Bitmap) data.getExtras().get("data");
                } else if (requestCode == REQUEST_IMAGE_PICK) {
                    Uri selectedImage = data.getData();
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                }

                if (bitmap != null) {
                    imagePreview.setImageBitmap(bitmap);
                    placeholderText.setVisibility(View.GONE);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
