package com.dragons.aurora.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dragons.aurora.CategoryManager;
import com.dragons.aurora.Util;
import com.dragons.aurora.fragment.details.ButtonDownload;
import com.dragons.aurora.model.Filter;
import com.dragons.aurora.playstoreapiv2.GooglePlayAPI;
import com.dragons.aurora.view.AdaptiveToolbar;
import com.percolate.caffeine.ViewUtils;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

import com.dragons.aurora.ContextUtil;
import com.dragons.aurora.R;
import com.dragons.aurora.fragment.FilterMenu;
import com.dragons.aurora.model.App;
import com.dragons.aurora.task.playstore.DetailsTask;
import com.dragons.aurora.task.playstore.SearchTask;
import com.squareup.picasso.Picasso;

import static com.dragons.aurora.fragment.FilterMenu.FILTER_APPS_WITH_ADS;
import static com.dragons.aurora.fragment.FilterMenu.FILTER_CATEGORY;
import static com.dragons.aurora.fragment.FilterMenu.FILTER_DOWNLOADS;
import static com.dragons.aurora.fragment.FilterMenu.FILTER_GSF_DEPENDENT_APPS;
import static com.dragons.aurora.fragment.FilterMenu.FILTER_PAID_APPS;
import static com.dragons.aurora.fragment.FilterMenu.FILTER_RATING;
import static com.dragons.aurora.fragment.FilterMenu.FILTER_SYSTEM_APPS;
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

    public static RelativeLayout getBs_layout() {
        return bs_layout;
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
        initFilters();
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
        dadtb.addQueryTextListener(SearchActivity.this, dadtb.getSearchView());
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
        initFilters();
    }

    protected void initFilters() {
        TextView filter_sysapps = findViewById(R.id.filter_systemapps);
        TextView filter_apps_ads = findViewById(R.id.filter_apps_ads);
        TextView filter_paidapps = findViewById(R.id.filter_paidapps);
        TextView filter_gfs = findViewById(R.id.filter_gsf);
        TextView filter_categories = findViewById(R.id.filter_categories);
        TextView filter_downs = findViewById(R.id.filter_downs);
        TextView filter_ratings = findViewById(R.id.filter_ratings);
        Map<Float, String> ratingLabels = new HashMap<>();
        Map<Integer, String> downloadsLabels = new HashMap<>();
        String[] ratingValues = getResources().getStringArray(R.array.filterRatingValues);
        for (int i = 0; i < ratingValues.length; i++) {
            ratingLabels.put(Float.parseFloat(ratingValues[i]), getResources().getStringArray(R.array.filterRatingLabels)[i]);
        }
        String[] downloadsValues = getResources().getStringArray(R.array.filterDownloadsValues);
        for (int i = 0; i < downloadsValues.length; i++) {
            downloadsLabels.put(Integer.parseInt(downloadsValues[i]), getResources().getStringArray(R.array.filterDownloadsLabels)[i]);
        }
        Filter filter = getFilterPreferences();
        filter_categories.setText(getString(
                R.string.action_filter_category,
                new CategoryManager(this).getCategoryName(filter.getCategory())
        ));
        filter_downs.setText(getString(
                R.string.action_filter_downloads,
                downloadsLabels.get(filter.getDownloads())
        ));
        filter_ratings.setText(getString(
                R.string.action_filter_rating,
                ratingLabels.get(filter.getRating())
        ));
        filter_sysapps.setOnClickListener(v ->  putBoolean(FILTER_SYSTEM_APPS, true));
        filter_apps_ads.setOnClickListener(v ->  putBoolean(FILTER_APPS_WITH_ADS, true));
        filter_paidapps.setOnClickListener(v ->  putBoolean(FILTER_PAID_APPS, true));
        filter_gfs.setOnClickListener(v ->  putBoolean(FILTER_GSF_DEPENDENT_APPS, true));
        filter_categories.setOnClickListener(v ->  getCategoryDialog().show());
        filter_ratings.setOnClickListener(v ->  getRatingDialog().show());
        filter_downs.setOnClickListener(v -> getDownloadsDialog().show());
    }

    public Filter getFilterPreferences() {
        Filter filter = new Filter();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        filter.setSystemApps(prefs.getBoolean(FILTER_SYSTEM_APPS, false));
        filter.setAppsWithAds(prefs.getBoolean(FILTER_APPS_WITH_ADS, true));
        filter.setPaidApps(prefs.getBoolean(FILTER_PAID_APPS, true));
        filter.setGsfDependentApps(prefs.getBoolean(FILTER_GSF_DEPENDENT_APPS, true));
        filter.setCategory(prefs.getString(FILTER_CATEGORY, CategoryManager.TOP));
        filter.setRating(prefs.getFloat(FILTER_RATING, 0.0f));
        filter.setDownloads(prefs.getInt(FILTER_DOWNLOADS, 0));
        return filter;
    }

    private void putBoolean(String key, boolean value) {
        PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(key, value).apply();
        restartActivity();
    }

    private void restartActivity() {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

    private AlertDialog getCategoryDialog() {
        final Map<String, String> categories = new CategoryManager(this).getCategoriesFromSharedPreferences();
        Util.addToStart((LinkedHashMap<String, String>) categories, CategoryManager.TOP, getString(R.string.search_filter));
        return getDialog(
                categories.values().toArray(new String[categories.size()]),
                new ConfirmOnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        PreferenceManager.getDefaultSharedPreferences(SearchActivity.this).edit().putString(
                                FILTER_CATEGORY,
                                categories.keySet().toArray(new String[categories.size()])[which]
                        ).apply();
                        super.onClick(dialog, which);
                    }
                }
        );
    }

    private AlertDialog getRatingDialog() {
        return getDialog(
                getResources().getStringArray(R.array.filterRatingLabels),
                new ConfirmOnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        PreferenceManager.getDefaultSharedPreferences(SearchActivity.this).edit().putFloat(
                                FILTER_RATING,
                                Float.parseFloat(getResources().getStringArray(R.array.filterRatingValues)[which])
                        ).apply();
                        super.onClick(dialog, which);
                    }
                }
        );
    }

    private AlertDialog getDownloadsDialog() {
        return getDialog(
                getResources().getStringArray(R.array.filterDownloadsLabels),
                new ConfirmOnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        PreferenceManager.getDefaultSharedPreferences(SearchActivity.this).edit().putInt(
                                FILTER_DOWNLOADS,
                                Integer.parseInt(getResources().getStringArray(R.array.filterDownloadsValues)[which])
                        ).apply();
                        super.onClick(dialog, which);
                    }
                }
        );
    }

    private AlertDialog getDialog(String[] labels, ConfirmOnClickListener listener) {
        return new AlertDialog.Builder(this)
                .setAdapter(new ArrayAdapter<>(this, android.R.layout.select_dialog_item, labels), listener).create();
    }

    private class ConfirmOnClickListener implements DialogInterface.OnClickListener {
        public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
            restartActivity();
        }
    }

    protected void appsTask(String newQuery, AdaptiveToolbar dadtb) {
        this.query = newQuery;
        dadtb.setSQuery(newQuery);
        dadtb.getSearch_item().setText(getTitleString());
        if (looksLikeAPackageId(query)) {
            checkPackageId(query);
            loadApps(subCategory);
            getBs_layout().setVisibility(View.VISIBLE);
        } else {
            loadApps(subCategory);
            getBs_layout().setVisibility(View.GONE);
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
