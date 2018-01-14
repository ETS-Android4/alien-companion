package com.gDyejeekis.aliencompanion.activities;

import android.os.Build;
import android.support.v7.widget.Toolbar;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;

/**
 * Created by George on 2/24/2017.
 */

public abstract class ToolbarActivity extends BackNavActivity {

    public static final int TOOLBAR_ANIM_DURATION = 500;

    public Toolbar toolbar;
    public boolean toolbarVisible;

    public void hideToolbar() {
        if(toolbarVisible) {
            toolbarVisible = false;
            toolbar.animate().translationY(-toolbar.getHeight()).setInterpolator(new AccelerateInterpolator(2)).setDuration(TOOLBAR_ANIM_DURATION);
        }
    }

    public void showToolbar() {
        if(!toolbarVisible) {
            toolbarVisible = true;
            toolbar.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).setDuration(TOOLBAR_ANIM_DURATION);
        }
    }

    protected void updateToolbarColors() {
        toolbar.setBackgroundColor(MyApplication.currentPrimaryColor);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(MyApplication.colorPrimaryDark);
        }
    }

    protected void initToolbar() {
        toolbarVisible = true;
        toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        if(MyApplication.nightThemeEnabled) {
            toolbar.setPopupTheme(R.style.OverflowStyleDark);
        }
        updateToolbarColors();
        toolbar.setNavigationIcon(MyApplication.homeAsUpIndicator);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

}
