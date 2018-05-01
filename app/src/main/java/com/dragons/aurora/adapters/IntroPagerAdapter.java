package com.dragons.aurora.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.dragons.aurora.fragment.intro.Page0Fragment;
import com.dragons.aurora.fragment.intro.Page1Fragment;
import com.dragons.aurora.fragment.intro.Page2Fragment;

public class IntroPagerAdapter extends FragmentPagerAdapter {

    static int count;

    public IntroPagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new Page0Fragment();
            case 1:
                return new Page1Fragment();
            case 2:
                return new Page2Fragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        count = 3;
        return 3;
    }

    public static int getCounter() {
        return count;
    }
}