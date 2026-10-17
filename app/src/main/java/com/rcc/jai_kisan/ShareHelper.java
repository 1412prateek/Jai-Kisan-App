package com.rcc.jai_kisan;

import android.app.Activity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class ShareHelper {

    public interface ShareCallback {
        void onShareText();
        void onSharePdf();
    }

    public static void showShareDialog(Activity activity, ShareCallback callback) {
        if (activity == null || activity.isFinishing()) return;

        String[] options = {
                "📄 " + LanguageManager.getDual(activity, "Share as Text", "share_text"),
                "📑 " + LanguageManager.getDual(activity, "Share as PDF", "share_pdf")
        };

        new MaterialAlertDialogBuilder(activity)
                .setTitle(LanguageManager.getDual(activity, "Share Report", "share_report"))
                .setItems(options, (dialog, which) -> {
                    if (callback != null) {
                        if (which == 0) {
                            callback.onShareText();
                        } else if (which == 1) {
                            callback.onSharePdf();
                        }
                    }
                })
                .setNegativeButton(LanguageManager.getDual(activity, "Cancel", "cancel"), null)
                .show();
    }
}
