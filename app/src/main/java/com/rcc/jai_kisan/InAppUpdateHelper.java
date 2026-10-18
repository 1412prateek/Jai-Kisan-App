package com.rcc.jai_kisan;

import android.app.Activity;
import android.content.IntentSender;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.appupdate.AppUpdateOptions;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.gms.tasks.Task;

/**
 * Manages Google Play In-App Updates (Immediate / Mandatory update flow).
 * If a new update is released, prompts the user immediately.
 * If user rejects or cancels the mandatory update, the app exits as requested.
 */
public class InAppUpdateHelper {

    private static final String TAG = "InAppUpdateHelper";
    private final AppCompatActivity activity;
    private final AppUpdateManager appUpdateManager;
    private ActivityResultLauncher<IntentSenderRequest> updateLauncher;

    public InAppUpdateHelper(AppCompatActivity activity) {
        this.activity = activity;
        this.appUpdateManager = AppUpdateManagerFactory.create(activity);
        registerUpdateLauncher();
    }

    private void registerUpdateLauncher() {
        updateLauncher = activity.registerForActivityResult(
                new ActivityResultContracts.StartIntentSenderForResult(),
                result -> {
                    int resultCode = result.getResultCode();
                    Log.d(TAG, "Update flow finished with resultCode: " + resultCode);
                    if (resultCode == Activity.RESULT_OK) {
                        Log.i(TAG, "User accepted and completed the update.");
                    } else if (resultCode == Activity.RESULT_CANCELED) {
                        Log.w(TAG, "User cancelled or closed mandatory update. Closing app.");
                        Toast.makeText(activity, "Update is required to use Jai Kisan. Closing app...", Toast.LENGTH_LONG).show();
                        activity.finishAffinity();
                    } else {
                        Log.e(TAG, "Update flow failed with code: " + resultCode + ". Closing app.");
                        Toast.makeText(activity, "Update failed. Please update from Google Play Store.", Toast.LENGTH_LONG).show();
                        activity.finishAffinity();
                    }
                }
        );
    }

    /**
     * Checks for immediate updates and starts update flow if available.
     */
    public void checkForAppUpdate() {
        Log.d(TAG, "Checking for Google Play in-app updates...");
        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();

        appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
            int availability = appUpdateInfo.updateAvailability();
            int availableVersionCode = appUpdateInfo.availableVersionCode();
            Log.d(TAG, "Update availability status: " + availability + " (Available version code: " + availableVersionCode + ")");

            if (availability == UpdateAvailability.UPDATE_AVAILABLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                Log.i(TAG, "Mandatory IMMEDIATE update available! Launching Google Play update flow...");
                startImmediateUpdate(appUpdateInfo);
            } else if (availability == UpdateAvailability.UPDATE_NOT_AVAILABLE) {
                Log.d(TAG, "App is up to date (no new version on Google Play).");
            } else {
                Log.d(TAG, "Update status code: " + availability + ", isImmediateAllowed: " + appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE));
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Failed to check for Google Play updates (normal in local debug builds): " + e.getMessage(), e);
        });
    }

    /**
     * Resumes an immediate update if it was interrupted while app was in background.
     */
    public void onResume() {
        appUpdateManager.getAppUpdateInfo().addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                Log.i(TAG, "Resuming in-progress immediate update flow...");
                startImmediateUpdate(appUpdateInfo);
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "onResume getAppUpdateInfo failed: " + e.getMessage());
        });
    }

    private void startImmediateUpdate(AppUpdateInfo appUpdateInfo) {
        try {
            AppUpdateOptions options = AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build();
            appUpdateManager.startUpdateFlowForResult(appUpdateInfo, updateLauncher, options);
        } catch (Exception e) {
            Log.e(TAG, "Exception while starting update flow: " + e.getMessage(), e);
        }
    }
}
