package com.dragons.aurora.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.dragons.aurora.PlayStoreApiAuthenticator;
import com.dragons.aurora.R;
import com.dragons.aurora.activities.AuroraActivity;
import com.dragons.aurora.fragment.details.AppLists;
import com.dragons.aurora.fragment.details.BackToPlayStore;
import com.dragons.aurora.fragment.details.Beta;
import com.dragons.aurora.fragment.details.DownloadOptions;
import com.dragons.aurora.fragment.details.DownloadOrInstall;
import com.dragons.aurora.fragment.details.ExodusPrivacy;
import com.dragons.aurora.fragment.details.GeneralDetails;
import com.dragons.aurora.fragment.details.Permissions;
import com.dragons.aurora.fragment.details.Review;
import com.dragons.aurora.fragment.details.Screenshot;
import com.dragons.aurora.fragment.details.Share;
import com.dragons.aurora.fragment.details.SystemAppPage;
import com.dragons.aurora.fragment.details.Video;
import com.dragons.aurora.model.App;
import com.dragons.aurora.task.playstore.ForegroundDetailsAppsTaskHelper;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class DetailsFragment extends ForegroundDetailsAppsTaskHelper {

    public static App app;

    protected View v;
    protected DownloadOrInstall downloadOrInstallFragment;
    protected String packageName;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.details_activity_layout, container, false);
        v.findViewById(R.id.fab_finish).setOnClickListener(v -> getActivity().finish());
        return v;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle arguments = getArguments();
        if (arguments != null) {
            packageName = arguments.getString("PackageName");
            fetchDetails();
        }
    }

    @Override
    public void onPause() {
        if (null != downloadOrInstallFragment) {
            downloadOrInstallFragment.unregisterReceivers();
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        redrawButtons();
        super.onResume();
    }

    public void fetchDetails() {
        Observable.fromCallable(() -> getResult(new PlayStoreApiAuthenticator(this.getActivity()).getApi(), packageName))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(app -> {
                    DetailsFragment.app = app;
                    this.redrawDetails(app);

                }, this::processException);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        new DownloadOptions((AuroraActivity) this.getActivity(), app).inflate(menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        return new DownloadOptions((AuroraActivity) this.getActivity(), app).onContextItemSelected(item);
    }

    private void redrawDetails(App app) {
        new GeneralDetails(this, app).draw();
        new ExodusPrivacy(this, app).draw();
        new Permissions(this, app).draw();
        new Screenshot(this, app).draw();
        new Review(this, app).draw();
        new AppLists(this, app).draw();
        new BackToPlayStore(this, app).draw();
        new Share(this, app).draw();
        new SystemAppPage(this, app).draw();
        new Video(this, app).draw();

        if (isGoogle())
            new Beta(this, app).draw();

        if (null != downloadOrInstallFragment) {
            downloadOrInstallFragment.unregisterReceivers();
        }

        downloadOrInstallFragment = new DownloadOrInstall((AuroraActivity) this.getActivity(), app);
        redrawButtons();
        new DownloadOptions((AuroraActivity) this.getActivity(), app).draw();
    }

    private void redrawButtons() {
        if (null != downloadOrInstallFragment) {
            downloadOrInstallFragment.unregisterReceivers();
            downloadOrInstallFragment.registerReceivers();
            downloadOrInstallFragment.draw();
        }
    }
}