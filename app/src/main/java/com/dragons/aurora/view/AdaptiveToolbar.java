package com.dragons.aurora.view;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.design.widget.AppBarLayout;
import android.support.v7.widget.SearchView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dragons.aurora.R;
import com.dragons.aurora.activities.AuroraActivity;
import com.dragons.aurora.activities.SearchActivity;
import com.dragons.aurora.fragment.UpdatableAppsFragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class AdaptiveToolbar extends AppBarLayout {

    static int style;
    static String query;
    View root;
    LinearLayout adtoolbarlayout;
    ImageView action_icon, avatar_icon;
    ImageButton download_section;
    RelativeLayout bubble_layout, download_container, searchViewContainer;
    TextView title0, title1, updates_counter, search_item;
    SearchView searchView;
    ArrayList<String> listHistory = new ArrayList<>();
    Set<String> setHistory = new HashSet<>();


    public AdaptiveToolbar(Context context) {
        super(context);
        init(context, null);
    }

    public AdaptiveToolbar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    @ColorInt
    public static int getColorAttr(Context context, int attr) {
        TypedArray ta = context.obtainStyledAttributes(new int[]{attr});
        @ColorInt int colorAccent = ta.getColor(0, 0);
        ta.recycle();
        return colorAccent;
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.AdaptiveToolbar, 0, 0);
        try {
            style = a.getInteger(R.styleable.AdaptiveToolbar_ToolbarStyle, 0);
        } finally {
            a.recycle();
        }
        root = inflate(context, R.layout.adaptive_toolbar, this);
        adtoolbarlayout = root.findViewById(R.id.adtoolbar_layout);
        action_icon = root.findViewById(R.id.action_icon);
        avatar_icon = root.findViewById(R.id.account_avatar);
        download_section = root.findViewById(R.id.download_section);
        download_container = root.findViewById(R.id.dwn_container);
        title0 = root.findViewById(R.id.app_title0);
        title1 = root.findViewById(R.id.app_title1);
        bubble_layout = root.findViewById(R.id.updates_bubble);
        updates_counter = root.findViewById(R.id.updates_counter);
        searchView = root.findViewById(R.id.adtb_searchbar);
        searchViewContainer = root.findViewById(R.id.search_v_container);
        search_item = root.findViewById(R.id.search_item);
        switch (getStyle()) {
            case 0:
                homeToolbar(context);
                break;
            case 1:
                detailsToolbar(context);
                break;
            case 2:
                searchToolbar(context);
        }
    }

    private void searchToolbar(Context context) {
        if (avatar_icon.getVisibility() != GONE) {
            avatar_icon.setVisibility(GONE);
        }
        download_container.setVisibility(VISIBLE);
        download_section.setOnClickListener((v -> {
            context.startActivity(new Intent(context, AuroraActivity.class));
            AuroraActivity.setPosition(2);
        }));
        if (UpdatableAppsFragment.updates > 0) {
            bubble_layout.setVisibility(VISIBLE);
            updates_counter.setText(String.valueOf(UpdatableAppsFragment.updates));
        } else {
            bubble_layout.setVisibility(GONE);
        }
        action_icon.setImageResource(R.drawable.ic_chevron_left);
        action_icon.setPadding(6, 6, 6, 6);
        action_icon.setContentDescription("details");
        searchViewContainer.setVisibility(VISIBLE);
        searchViewContainer.setOnClickListener((v -> {
            searchView.setFocusable(true);
            searchView.setIconified(false);
            searchView.requestFocusFromTouch();
            searchView.setQuery(query, false);
            search_item.setVisibility(GONE);
        }));
        if (searchView.isIconified() && !searchView.isFocused()) {
            search_item.setVisibility(VISIBLE);
        }

        title0.setVisibility(GONE);
        title1.setVisibility(GONE);
    }

    private void homeToolbar(Context context) {
        if (avatar_icon.getVisibility() != VISIBLE) {
            avatar_icon.setVisibility(VISIBLE);
        }
        download_container.setVisibility(GONE);
        action_icon.setImageResource(R.mipmap.ic_launcher);
        action_icon.setPadding(0, 0, 0, 0);
        action_icon.setContentDescription("home");
        setBackgroundColor(getColorAttr(context, android.R.attr.colorPrimary));
        title0.setText("Aurora");
        title1.setText("Store");
        title0.setVisibility(VISIBLE);
        title1.setVisibility(VISIBLE);
        searchViewContainer.setVisibility(GONE);
    }

    private void detailsToolbar(Context context) {
        if (avatar_icon.getVisibility() != GONE) {
            avatar_icon.setVisibility(GONE);
        }
        download_container.setVisibility(VISIBLE);
        download_section.setOnClickListener((v -> {
            context.startActivity(new Intent(context, AuroraActivity.class));
            AuroraActivity.setPosition(2);
        }));
        if (UpdatableAppsFragment.updates > 0) {
            bubble_layout.setVisibility(VISIBLE);
            updates_counter.setText(String.valueOf(UpdatableAppsFragment.updates));
        } else {
            bubble_layout.setVisibility(GONE);
        }
        action_icon.setImageResource(R.drawable.ic_chevron_left);
        action_icon.setPadding(6, 6, 6, 6);
        action_icon.setContentDescription("details");
        searchViewContainer.setVisibility(GONE);
        title0.setVisibility(VISIBLE);
        title1.setVisibility(VISIBLE);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void addQueryTextListener(Activity activity, SearchView searchView) {
        SearchManager searchManager = (SearchManager) getContext().getSystemService(Context.SEARCH_SERVICE);
        if (null != searchManager) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(activity.getComponentName()));
        }
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextChange(String query) {
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.setQuery("", false);
                searchView.setIconified(true);
                searchView.clearFocus();
                setQuery(query);
                return false;
            }
        });
    }

    protected void setQuery(String query) {
        Intent i = new Intent(getContext(), SearchActivity.class);
        i.setAction(Intent.ACTION_SEARCH);
        i.putExtra(SearchManager.QUERY, query);
    }

    public SearchView getSearchView() {
        return searchView;
    }

    public void setSQuery(String query) {
        AdaptiveToolbar.query = query;
    }

    public TextView getSearch_item() {
        return search_item;
    }

    public ImageButton getDownload_section() {
        return download_section;
    }

    public TextView getTitle0() {
        return title0;
    }

    public TextView getTitle1() {
        return title1;
    }

    public ImageView getAction_icon() {
        return action_icon;
    }

    public ImageView getAvatar_icon() {
        return avatar_icon;
    }

    public int getStyle() {
        return style;
    }

    public void setStyle(int style) {
        AdaptiveToolbar.style = style;
    }
}
