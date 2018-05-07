package com.dragons.aurora.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.dragons.aurora.R;
import com.dragons.aurora.fragment.DetailsFragment;
import com.dragons.aurora.fragment.details.DownloadOrInstall;
import com.dragons.aurora.model.App;
import com.dragons.aurora.task.playstore.PurchaseCheckTask;
import com.dragons.aurora.view.AdaptiveToolbar;

import java.util.Timer;
import java.util.TimerTask;

public class ManualDownloadActivity extends DetailsActivity {

    private int latestVersionCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (null == DetailsFragment.app) {
            Log.e(getClass().getSimpleName(), "No app stored");
            finish();
            return;
        }
        latestVersionCode = DetailsFragment.app.getVersionCode();
        draw(DetailsFragment.app);
    }

    @Override
    protected void onPause() {
        super.onPause();
        DetailsFragment.app.setVersionCode(latestVersionCode);
    }

    private void draw(App app) {
        setTitle(app.getDisplayName());
        setContentView(R.layout.manual_download_activity_layout);
        if (app.getOfferType() == 0) {
            app.setOfferType(1);
        }
        ((TextView) findViewById(R.id.compatibility)).setText(
                app.getVersionCode() > 0
                        ? R.string.manual_download_compatible
                        : R.string.manual_download_incompatible
        );
        if (app.getVersionCode() > 0) {
            ((EditText) findViewById(R.id.version_code)).setHint(String.valueOf(latestVersionCode));
        }
        DownloadOrInstall downloadOrInstallFragment = new DownloadOrInstall(this, app);
        ManualDownloadTextWatcher textWatcher = new ManualDownloadTextWatcher(app,
                findViewById(R.id.download),
                findViewById(R.id.install),
                downloadOrInstallFragment
        );
        String versionCode = Integer.toString(app.getVersionCode());
        textWatcher.onTextChanged(versionCode, 0, 0, versionCode.length());
        ((EditText) findViewById(R.id.version_code)).addTextChangedListener(textWatcher);
        downloadOrInstallFragment.registerReceivers();
        downloadOrInstallFragment.draw();
    }

    static private class ManualDownloadTextWatcher implements TextWatcher {

        static private final int TIMEOUT = 1000;

        private final App app;
        private final Button downloadButton;
        private final Button installButton;
        private DownloadOrInstall downloadOrInstallFragment;
        private Timer timer;

        ManualDownloadTextWatcher(App app,
                                  Button downloadButton,
                                  Button installButton,
                                  DownloadOrInstall downloadOrInstallFragment) {
            this.app = app;
            this.downloadButton = downloadButton;
            this.installButton = installButton;
            this.downloadOrInstallFragment = downloadOrInstallFragment;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            try {
                app.setVersionCode(Integer.parseInt(s.toString()));
                installButton.setVisibility(View.GONE);
                downloadButton.setText(R.string.details_download_checking);
                downloadButton.setEnabled(false);
                downloadButton.setVisibility(View.VISIBLE);
                restartTimer();
            } catch (NumberFormatException e) {
                Log.w(getClass().getSimpleName(), s.toString() + " is not a number");
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }

        private void restartTimer() {
            if (null != timer) {
                timer.cancel();
            }
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    getTask(timer).execute();
                }
            }, TIMEOUT);
        }

        private PurchaseCheckTask getTask(Timer timer) {
            PurchaseCheckTask task = new PurchaseCheckTask();
            task.setContext(downloadButton.getContext());
            task.setTimer(timer);
            task.setApp(app);
            task.setDownloadOrInstallFragment(downloadOrInstallFragment);
            task.setDownloadButton(downloadButton);
            return task;
        }
    }
}
