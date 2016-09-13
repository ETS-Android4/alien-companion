package com.gDyejeekis.aliencompanion.Activities;

import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.gDyejeekis.aliencompanion.Fragments.SettingsFragments.SettingsFragment;
import com.gDyejeekis.aliencompanion.Fragments.SettingsFragments.AppearanceSettingsFragment;
import com.gDyejeekis.aliencompanion.Fragments.SettingsFragments.CommentsSettingsFragment;
import com.gDyejeekis.aliencompanion.Fragments.SettingsFragments.HeadersSettingsFragment;
import com.gDyejeekis.aliencompanion.Fragments.SettingsFragments.LinkHandlingSettingsFragment;
import com.gDyejeekis.aliencompanion.Fragments.SettingsFragments.NavigationSettingsFragment;
import com.gDyejeekis.aliencompanion.Fragments.SettingsFragments.OtherSettingsFragment;
import com.gDyejeekis.aliencompanion.Fragments.SettingsFragments.PostsSettingsFragment;
import com.gDyejeekis.aliencompanion.Fragments.SettingsFragments.SyncSettingsFragment;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.enums.SettingsMenuType;

/**
 * Created by George on 8/4/2015.
 */
public class SettingsActivity extends BackNavActivity {

    private Toolbar toolbar;

    private SettingsMenuType menuType;

    private boolean dualPaneActive;

    private boolean dualPaneInLandScape;
    private boolean dualPaneEverywhere;

    @Override
    public void onCreate(Bundle bundle) {
        MyApplication.applyCurrentTheme(this);
        super.onCreate(bundle);
        setContentView(R.layout.settings_dual_pane);

        if(MyApplication.nightThemeEnabled)
            getTheme().applyStyle(R.style.Theme_AppCompat_Dialog, true);

        toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        toolbar.setBackgroundColor(MyApplication.currentColor);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) getWindow().setStatusBarColor(MyApplication.colorPrimaryDark);
        toolbar.setNavigationIcon(MyApplication.homeAsUpIndicator);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        menuType = (SettingsMenuType) getIntent().getSerializableExtra("menuType");
        try {
            getSupportActionBar().setTitle(menuType.value());
        } catch (NullPointerException e) {
            getSupportActionBar().setTitle("Settings");
        }

        PreferenceFragment fragment;
        switch (menuType) {
            case headers:
                dualPaneEverywhere = MyApplication.getScreenSizeInches(this) > 9;
                dualPaneInLandScape = MyApplication.getScreenSizeInches(this) > 6.4;
                fragment = new HeadersSettingsFragment();
                break;
            case appearance:
                fragment = new AppearanceSettingsFragment();
                break;
            case navigation:
                fragment = new NavigationSettingsFragment();
                break;
            case posts:
                fragment = new PostsSettingsFragment();
                break;
            case comments:
                fragment = new CommentsSettingsFragment();
                break;
            case sync:
                fragment = new SyncSettingsFragment();
                break;
            case linkHandling:
                fragment = new LinkHandlingSettingsFragment();
                break;
            case other:
                fragment = new OtherSettingsFragment();
                break;
            default:
                fragment = new SettingsFragment();
                break;
        }

        if(dualPaneEverywhere) {
            dualPaneActive = true;
        }
        else if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE
                && dualPaneInLandScape) {
            dualPaneActive = true;
        }
        else {
            dualPaneActive = false;
        }

        getFragmentManager().beginTransaction()
                .replace(R.id.headers_holder, fragment)
                .commitAllowingStateLoss();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if(menuType == SettingsMenuType.headers) {
            if(dualPaneEverywhere) {
                dualPaneActive = true;
            }
            else if(dualPaneInLandScape && newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                dualPaneActive = true;
            }
            else {
                dualPaneActive = false;
                hideOptionsFragment();
            }
        }
        else {
            dualPaneActive = false;
        }
    }

    public void setupOptionsFragment(PreferenceFragment fragment, SettingsMenuType type) {
        if(type != SettingsMenuType.headers) {
            getSupportActionBar().setTitle(type.value());
            getFragmentManager().beginTransaction().replace(R.id.options_holder, fragment, "optionsFragment").commitAllowingStateLoss();
            findViewById(R.id.options_holder).setVisibility(View.VISIBLE);
        }
    }

    private void hideOptionsFragment() {
        getSupportActionBar().setTitle("Settings");
        findViewById(R.id.options_holder).setVisibility(View.GONE);
    }

    //private void showOptionsFragment() {
    //    if(getFragmentManager().findFragmentByTag("optionsFragment") != null) {
    //        findViewById(R.id.options_holder).setVisibility(View.VISIBLE);
    //    }
    //}

    @Override
    public void onBackPressed() {
        if(menuType == SettingsMenuType.headers) {
            MyApplication.getCurrentSettings();
        }
        super.onBackPressed();
    }

    public boolean isDualPaneActive() {
        return dualPaneActive;
    }

}
