package com.dragons.aurora.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dragons.aurora.fragment.details.ButtonDownload;
import com.dragons.aurora.playstoreapiv2.GooglePlayAPI;
import com.dragons.aurora.view.AdaptiveToolbar;
import com.percolate.caffeine.ViewUtils;

import java.util.regex.Pattern;

import com.dragons.aurora.ContextUtil;
import com.dragons.aurora.R;
import com.dragons.aurora.fragment.FilterMenu;
import com.dragons.aurora.model.App;
import com.dragons.aurora.task.playstore.DetailsTask;
import com.dragons.aurora.task.playstore.SearchTask;
import com.squareup.picasso.Picasso;

import static java.lang.Thread.sleep;

public class SearchActivity extends EndlessScrollActivity {

    public static final String PUB_PREFIX = "pub:";
    public static RelativeLayout bs_layout;
    public static TextView bs_title, bs_desc, bs_ads;
    public static ImageView bs_img;
    public static Button bs_get;

    private String query;

    static protected boolean actionIs(Intent intent, String action) {
        return null != intent && null != intent.getAction() && intent.getAction().equals(action);
    }

    public static void setBs_title(String str) {
        bs_title.setText(str);
    }

    public static void setBS_desc(String str) {
        bs_desc.setText(str);
    }

    public static void setBs_img(Activity activity, String str) {
        Picasso.with(activity).load(str).into(bs_img);
    }

    public static void setBs_getListener(View.OnClickListener onClickListener) {
        bs_get.setOnClickListener(onClickListener);
    }

    public static void containsAds(boolean bol) {
       if (bol)
           bs_ads.setText(R.string.details_contains_ads);
       else
           bs_ads.setText(R.string.details_no_ads);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewUtils.findViewById(this, R.id.category_tabs).setVisibility(View.GONE);
        bs_layout = findViewById(R.id.bs_layout);
        bs_title = findViewById(R.id.bs_title);
        bs_desc = findViewById(R.id.bs_desc);
        bs_ads = findViewById(R.id.bs_cointains_ads);
        bs_img = findViewById(R.id.bs_img);
        bs_get = findViewById(R.id.bs_get);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        bs_layout = findViewById(R.id.bs_layout);
        bs_title = findViewById(R.id.bs_title);
        bs_desc = findViewById(R.id.bs_desc);
        bs_ads = findViewById(R.id.bs_cointains_ads);
        bs_img = findViewById(R.id.bs_img);
        bs_get = findViewById(R.id.bs_get);
        AdaptiveToolbar dadtb = findViewById(R.id.d_adtb2);
        String newQuery = getQuery(intent);
        if (looksLikeAPackageId(newQuery)) {
            Log.i(getClass().getSimpleName(), "Following search suggestion to app page: " + newQuery);
            clearApps();
            appsTask(newQuery, dadtb);
            return;
        }
        Log.i(getClass().getSimpleName(), "Searching: " + newQuery);
        if (null != newQuery && !newQuery.equals(this.query)) {
            clearApps();
            appsTask(newQuery, dadtb);
        }
        dadtb.addQueryTextListener(this, dadtb.getSearchView());
        dadtb.getSearchView().setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int position) {
                return false;
            }

            @Override
            public boolean onSuggestionClick(int position) {
                Cursor cursor = dadtb.getSearchView().getSuggestionsAdapter().getCursor();
                cursor.moveToPosition(position);
                String suggestion = cursor.getString(2);
                dadtb.getSearchView().setQuery(suggestion, true);
                clearApps();
                appsTask(String.valueOf(dadtb.getSearchView().getQuery()), dadtb);
                return false;
            }
        });
    }

    protected void appsTask(String newQuery, AdaptiveToolbar dadtb) {
        this.query = newQuery;
        dadtb.setSQuery(newQuery);
        dadtb.getSearch_item().setText(getTitleString());
        if (looksLikeAPackageId(query)) {
            Log.i(getClass().getSimpleName(), query + " looks like a package id");
            try {
                sleep(700);
                checkPackageId(query);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            loadApps(subCategory);
            bs_layout.setVisibility(View.VISIBLE);
        } else {
            loadApps(subCategory);
            bs_layout.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean result = super.onCreateOptionsMenu(menu);
        menu.findItem(R.id.filter_category).setVisible(true);
        return result;
    }

    @Override
    protected SearchTask getTask() {
        SearchTask task = new SearchTask(iterator);
        task.setQuery(query);
        task.setFilter(new FilterMenu(this).getFilterPreferences());
        return task;
    }

    private String getTitleString() {
        return query.startsWith(PUB_PREFIX)
                ? getString(R.string.apps_by, query.substring(PUB_PREFIX.length()))
                : getString(R.string.activity_title_search, query)
                ;
    }

    private String getQuery(Intent intent) {
        if (intent.getScheme() != null
                && (intent.getScheme().equals("market")
                || intent.getScheme().equals("http")
                || intent.getScheme().equals("https")
        )
                ) {
            return intent.getData().getQueryParameter("q");
        }
        if (actionIs(intent, Intent.ACTION_SEARCH)) {
            return intent.getStringExtra(SearchManager.QUERY);
        } else if (actionIs(intent, Intent.ACTION_VIEW)) {
            return intent.getDataString();
        }
        return null;
    }

    private boolean looksLikeAPackageId(String query) {
        if (TextUtils.isEmpty(query)) {
            return false;
        }
        String pattern = "([\\p{L}_$][\\p{L}\\p{N}_$]*\\.)+[\\p{L}_$][\\p{L}\\p{N}_$]*";
        Pattern r = Pattern.compile(pattern);
        return r.matcher(query).matches();
    }

    private void checkPackageId(String packageId) {
        DetailsTask task = new CheckPackageIdTask(this);
        task.setContext(this);
        task.setPackageName(packageId);
        task.execute();
    }

    static private class CheckPackageIdTask extends DetailsTask {

        private SearchActivity activity;

        CheckPackageIdTask(SearchActivity activity) {
            this.activity = activity;
        }

        @Override
        protected void onPostExecute(App app) {
            super.onPostExecute(app);
            if (null != app && ContextUtil.isAlive(activity) && bs_title != null) {
                setBs_title(app.getDisplayName());
                setBS_desc(app.getDescription());
                setBs_getListener((v -> {
                    DetailsActivity.app = app;
                    new ButtonDownload((AuroraActivity) context, app).checkAndDownload();
                    activity.finish();
                }));
                containsAds(app.containsAds());
                setBs_img(activity, app.getIconInfo().getUrl());
            } else {
                activity.finish();
            }
        }
    }
}
