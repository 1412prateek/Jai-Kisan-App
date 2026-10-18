package com.rcc.jai_kisan;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class ShareHelper {

    public interface ShareCallback {
        void onShareText();
        void onSharePdf();
    }

    public static void showShareDialog(Activity activity, ShareCallback callback) {
        if (activity == null || activity.isFinishing()) return;

        BottomSheetDialog dialog = new BottomSheetDialog(activity);
        View view = LayoutInflater.from(activity).inflate(R.layout.dialog_share_options, null);
        dialog.setContentView(view);

        // Make window background transparent so rounded corners on dialog_share_options show cleanly
        dialog.setOnShowListener(dialogInterface -> {
            try {
                BottomSheetDialog d = (BottomSheetDialog) dialogInterface;
                View bottomSheet = d.findViewById(com.google.android.material.R.id.design_bottom_sheet);
                if (bottomSheet != null) {
                    bottomSheet.setBackground(new ColorDrawable(Color.TRANSPARENT));
                }
            } catch (Exception ignored) {}
        });
        if (dialog.getWindow() != null) {
            View bottomSheet = dialog.getWindow().findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                bottomSheet.setBackground(new ColorDrawable(Color.TRANSPARENT));
            }
        }

        TextView tvTitle = view.findViewById(R.id.tvShareDialogTitle);
        TextView tvShareTextTitle = view.findViewById(R.id.tvShareTextTitle);
        TextView tvShareTextDesc1 = view.findViewById(R.id.tvShareTextDesc1);
        TextView tvShareTextDesc2 = view.findViewById(R.id.tvShareTextDesc2);
        TextView tvSharePdfTitle = view.findViewById(R.id.tvSharePdfTitle);
        TextView tvSharePdfDesc1 = view.findViewById(R.id.tvSharePdfDesc1);
        TextView tvSharePdfDesc2 = view.findViewById(R.id.tvSharePdfDesc2);
        TextView btnCancel = view.findViewById(R.id.btnShareCancel);

        boolean isEngOnly = LanguageManager.isEnglishOnly(activity);

        tvTitle.setText(LanguageManager.getDual(activity, "Share Option", "share_option"));
        tvShareTextTitle.setText(LanguageManager.getDual(activity, "Share as Text", "share_text"));
        tvSharePdfTitle.setText(LanguageManager.getDual(activity, "Share as PDF Report", "share_pdf"));
        btnCancel.setText(LanguageManager.getDual(activity, "Cancel", "cancel"));

        if (isEngOnly) {
            tvShareTextDesc2.setVisibility(View.GONE);
            tvSharePdfDesc2.setVisibility(View.GONE);
        } else {
            tvShareTextDesc2.setText(LanguageManager.getRegional(activity, "share_text_desc", "रिपोर्ट को संदेश के रूप में साझा करें।"));
            tvShareTextDesc2.setVisibility(View.VISIBLE);

            tvSharePdfDesc2.setText(LanguageManager.getRegional(activity, "share_pdf_desc", "एक सुंदर पीडीएफ दस्तावेज साझा करें।"));
            tvSharePdfDesc2.setVisibility(View.VISIBLE);
        }

        view.findViewById(R.id.cardShareText).setOnClickListener(v -> {
            dialog.dismiss();
            if (callback != null) {
                callback.onShareText();
            }
        });

        view.findViewById(R.id.cardSharePdf).setOnClickListener(v -> {
            dialog.dismiss();
            if (callback != null) {
                callback.onSharePdf();
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
}
