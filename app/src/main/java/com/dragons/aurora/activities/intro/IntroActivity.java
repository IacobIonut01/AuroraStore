package com.dragons.aurora.activities.intro;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.RelativeLayout;

import com.dragons.aurora.R;
import com.dragons.aurora.Util;
import com.dragons.aurora.activities.AuroraActivity;
import com.dragons.aurora.adapters.IntroPagerAdapter;
import com.dragons.custom.pageindicator.PageIndicatorView;

public class IntroActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.intro_layout);
        ViewPager pager = findViewById(R.id.intro_pager);
        pager.setAdapter(new IntroPagerAdapter(getSupportFragmentManager()));
        PageIndicatorView pageIndicatorView = findViewById(R.id.pageIndicatorView);
        pageIndicatorView.setViewPager(pager);
        FloatingActionButton action_bt = findViewById(R.id.intro_action);
        FloatingActionButton action_bt2 = findViewById(R.id.intro_action2);
        action_bt.setOnClickListener((v -> {
            if (action_bt.getContentDescription() == "state1"){
                pager.setCurrentItem(pager.getCurrentItem() - 1);
                action_bt.setContentDescription("state0");
            }
            else {
                pager.setCurrentItem(pager.getCurrentItem() + 1);
                action_bt.setContentDescription("state1");
            }
        }));
        action_bt2.setOnClickListener(v -> {
            if (action_bt2.getVisibility() == View.VISIBLE) {
                finishIntro();
                startMain();
            }
        });
        if (isIntroFinished()) {
            startMain();
        }
        RelativeLayout head = findViewById(R.id.head_intro_layout);
        pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        Util.moveAndAnimate(action_bt, "translationX", -head.getWidth()+250, 0, 1000);
                        Util.rotate(action_bt, 180, 0, 600);
                        action_bt.setContentDescription("state0");
                        Util.moveAndAnimate(action_bt2, "alpha", 1, 0);
                        Util.moveAndAnimate(action_bt2, "translationX", head.getWidth()-250, 0, 500);
                        action_bt2.setClickable(false);
                        break;
                    case 1:
                        Util.moveAndAnimate(action_bt, "translationX", 0, -head.getWidth()+250, 1000);
                        Util.rotate(action_bt, 0, 180, 600);
                        action_bt.setContentDescription("state1");
                        action_bt2.setVisibility(View.VISIBLE);
                        action_bt2.setClickable(true);
                        Util.moveAndAnimate(action_bt2, "alpha", 0, 1);
                        Util.moveAndAnimate(action_bt2, "translationX", 0, head.getWidth()-250, 500);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    public void startMain() {
        finish();
        startActivity(new Intent(this, AuroraActivity.class));
    }

    public void finishIntro() {
        SharedPreferences prefs = getSharedPreferences("intro_state", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("intro_finished", true);
        editor.commit();
    }

    public boolean isIntroFinished() {
        SharedPreferences prefs = getSharedPreferences("intro_state", MODE_PRIVATE);
        return prefs.getBoolean("intro_finished" ,false);
    }
}
