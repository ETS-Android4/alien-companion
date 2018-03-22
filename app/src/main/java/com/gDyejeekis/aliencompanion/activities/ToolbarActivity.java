package com.gDyejeekis.aliencompanion.activities;

import android.os.Build;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import com.gDyejeekis.aliencompanion.AppConstants;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;

/**
 * Created by George on 2/24/2017.
 */

public abstract class ToolbarActivity extends BackNavActivity implements AppBarLayout.OnOffsetChangedListener {

    public static final String TAG = "ToolbarActivity";

    private AppBarLayout appBarLayout;
    public Toolbar toolbar;
    public boolean toolbarVisible; // true when toolbar is completely visible

    public void expandToolbar() {
        if (!toolbarVisible && appBarLayout!=null)
            appBarLayout.setExpanded(true);
    }

    public void collapseToolbar() {
        if (appBarLayout!=null) {
            appBarLayout.setExpanded(false);
        }
    }

    protected void updateToolbarColors() {
        toolbar.setBackgroundColor(MyApplication.currentPrimaryColor);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(MyApplication.colorPrimaryDark);
        }
    }

    protected void updateToolbarAutoHide() {
        if (appBarLayout != null) {
            AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams) toolbar.getLayoutParams();
            CoordinatorLayout.LayoutParams appBarLayoutParams = (CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams();
            if(!MyApplication.autoHideToolbar) {
                params.setScrollFlags(0);
                appBarLayoutParams.setBehavior(null);
            } else {
                params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL | AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS);
                appBarLayoutParams.setBehavior(new AppBarLayout.Behavior());
            }
            toolbar.setLayoutParams(params);
            appBarLayout.setLayoutParams(appBarLayoutParams);
        }
    }

    protected void initToolbar() {
        try {
            appBarLayout = findViewById(R.id.app_bar_layout);
            appBarLayout.addOnOffsetChangedListener(this);
        } catch (Exception e) {}

        toolbarVisible = true;
        toolbar = findViewById(R.id.my_toolbar);
        if(MyApplication.nightThemeEnabled) {
            toolbar.setPopupTheme(R.style.OverflowStyleDark);
        }
        updateToolbarAutoHide();
        updateToolbarColors();
        toolbar.setNavigationIcon(AppConstants.homeAsUpIndicator);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        toolbarVisible = (verticalOffset == 0);
    }

}
