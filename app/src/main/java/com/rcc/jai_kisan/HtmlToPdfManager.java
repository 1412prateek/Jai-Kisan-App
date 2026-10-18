package com.rcc.jai_kisan;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.CancellationSignal;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;

/**
 * Headless HTML-to-PDF Generator & Direct Share Manager.
 * Renders HTML with strict UTF-8 charset directly to a PDF file in the cache directory,
 * then opens the Android Share Chooser directly (e.g. WhatsApp, Gmail) without requiring manual save.
 */
public class HtmlToPdfManager {

    public static void generatePdfFromHtml(Context context, String htmlContent, String fileName) {
        sharePdfDirectly(context, htmlContent, fileName);
    }

    public static void sharePdfDirectly(Context context, String htmlContent, String fileName) {
        if (context == null || htmlContent == null) return;

        new Handler(Looper.getMainLooper()).post(() -> {
            try {
                Toast.makeText(context, "Preparing PDF... / पीडीएफ तैयार हो रही है...", Toast.LENGTH_SHORT).show();

                WebView webView = new WebView(context);
                webView.getSettings().setJavaScriptEnabled(false);
                webView.getSettings().setDefaultTextEncodingName("UTF-8");

                // Ensure HTML has UTF-8 charset
                String finalHtml = htmlContent;
                if (!finalHtml.contains("charset=")) {
                    finalHtml = "<meta charset=\"UTF-8\">\n" + finalHtml;
                }

                webView.setWebViewClient(new WebViewClient() {
                    @Override
                    public void onPageFinished(WebView view, String url) {
                        try {
                            File reportsDir = new File(context.getCacheDir(), "shared_reports");
                            if (!reportsDir.exists()) reportsDir.mkdirs();

                            String safeFileName = (fileName != null ? fileName : "JaiKisan_Report") + ".pdf";

                            PrintDocumentAdapter adapter = view.createPrintDocumentAdapter(safeFileName);
                            PrintAttributes attributes = new PrintAttributes.Builder()
                                    .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                                    .setResolution(new PrintAttributes.Resolution("pdf", "pdf", 300, 300))
                                    .setMinMargins(new PrintAttributes.Margins(400, 500, 400, 500))
                                    .build();

                            new android.print.PdfPrint(attributes).print(adapter, reportsDir, safeFileName, new android.print.PdfPrint.Callback() {
                                @Override
                                public void onSuccess(File file) {
                                    new Handler(Looper.getMainLooper()).post(() -> {
                                        try {
                                            Uri contentUri = FileProvider.getUriForFile(
                                                    context,
                                                    context.getPackageName() + ".provider",
                                                    file
                                            );

                                            Intent shareIntent = new Intent(Intent.ACTION_SEND);
                                            shareIntent.setType("application/pdf");
                                            shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                                            shareIntent.putExtra(Intent.EXTRA_SUBJECT, fileName);
                                            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                                            Intent chooser = Intent.createChooser(
                                                    shareIntent,
                                                    LanguageManager.getDual(context, "Share PDF Report", "share_pdf")
                                            );
                                            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            
                                            // Handle global interstitial ad counter (every 3 PDFs)
                                            if (context instanceof Activity) {
                                                AdManager.getInstance().onPdfGenerated((Activity) context, () -> {
                                                    try {
                                                        context.startActivity(chooser);
                                                    } catch (Exception ex) {
                                                        Toast.makeText(context, "Failed to share PDF: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            } else {
                                                context.startActivity(chooser);
                                            }
                                        } catch (Exception e) {
                                            Toast.makeText(context, "Failed to share PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }

                                @Override
                                public void onFailure(String error) {
                                    new Handler(Looper.getMainLooper()).post(() -> {
                                        Toast.makeText(context, "Error generating PDF: " + error, Toast.LENGTH_SHORT).show();
                                    });
                                }
                            });
                        } catch (Exception e) {
                            Toast.makeText(context, "Error rendering PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                webView.loadDataWithBaseURL("file:///android_asset/", finalHtml, "text/html", "UTF-8", null);

            } catch (Exception e) {
                Toast.makeText(context, "Failed to create PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
